/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.ReviewEntry;
import com.example.judge.popularmovies.data.MovieContract.TVEntry;
import com.example.judge.popularmovies.data.MovieContract.TrailerEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * This is the class that is responsible for all syncing to the database from TheMovieDB, this is
 * what interacts with the database the most in order to insert and update the data when called to sync.
 * It does require a class not within the repository, TmdbApiKey with a simple KEY constant for the
 * TheMovieDB key. The functionality is somewhat complex, it basically has a number of sync modes that
 * are passed in to the single sync method. This then sets up various parameters needed for the syncing
 * process, it then calls volley to perform the json download and parsing, and in the callback it
 * uses the set parameters such as column data and database/content provider names in order to correctly
 * sync the database. It first performs the download, then once it has the JSONObject it pulls the results
 * array outside of it. It then uses this array in order to populate an array of ContentValues. Once all
 * of the values are setup correctly it will then do removal of the required rows in the database and a bulk
 * insert of the content values.
 */
public class TmdbApiHandler {

    // The various sync modes that are supported by the syncer
    public static final int SYNC_MOVIE = 0;
    public static final int SYNC_MOVIE_REVIEW = 1;
    public static final int SYNC_MOVIE_TRAILER = 2;
    public static final int SYNC_MOVIE_DETAIL = 3;
    public static final int SYNC_MOVIE_SEARCH = 4;
    public static final int SYNC_TV = 5;
    public static final int SYNC_TV_TRAILER = 6;
    public static final int SYNC_TV_DETAIL = 7;
    public static final int SYNC_TV_SEARCH = 8;

    // The base uri and paths required for the various calls
    private static final Uri API_BASE_URI = Uri.parse("http://api.themoviedb.org/3");
    private static final String MOVIE_PATH = "movie";
    private static final String TV_PATH = "tv";
    private static final String REVIEW_PATH = "reviews";
    private static final String TRAILER_PATH = "videos";

    // These paths are mostly related to the type of source being pulled from, such as most popular
    private static final String AIRING_TODAY_PATH = "airing_today";
    private static final String ON_THE_AIR_PATH = "on_the_air";
    private static final String NOW_PLAYING_PATH = "now_playing";
    private static final String POPULAR_PATH = "popular";
    private static final String RATING_PATH = "top_rated";
    private static final String UPCOMING_PATH = "upcoming";
    private static final String SEARCH_PATH = "search";

    // These are the various key parameters and other parameters needed for the syncing.
    private static final String API_KEY_PARAM = "api_key";
    private static final String API_QUERY_PARAM = "query";
    private static final String API_RESULT_ARRAY_KEY = "results";
    private static final String LOG_TAG = TmdbApiHandler.class.getSimpleName();

    /**
     * This is the most often used sync function, and doesn't force the syncing of the data
     * @param syncType The type of syncing to perform
     * @param source The source to sync, such as most popular
     * @param context The context for the application
     */
    public static void sync(final @SyncType int syncType, final String source, final Context context) {
        sync(syncType, source, context, false);
    }

    /**
     * This is the sync function including forcing functionality, as stated above it can force whether
     * a sync should take place ignoring the minimum amount of time between syncs. Takes in a sync type,
     * context, source, and whether to force the syncing.
     * @param syncType The type of syncing to perform
     * @param source The source to sync, such as most popular
     * @param context The context for the application
     * @param force Whether to force syncing even if the minimum time between syncs hasn't elapsed
     */
    public static void sync(final @SyncType int syncType, final String source, final Context context, final boolean force) {

        // The various parameters required for the sync process, final in order to be accessible within the callback

        // This parameter is the database columns required, which also include the api key to pull from
        final MovieContract.DatabaseColumn[] apiColumns, nonApiColumns;

        // Whether to update the rows in the database rather than drop them completely, used for updating the media details
        final boolean update;

        // The non api key values to insert into the database
        final String[] key;

        // The media type and selection, mostly used for the update functionality and what to put into the logging
        final String type, selection;

        // The URI's used, uri is the TheMovieDB url to sync against, and contentUri is which Content Provider uri to access.
        final Uri uri, contentUri;

        // Massive switch statement which for each sync type sets the parameters
        switch (syncType) {
            case SYNC_MOVIE: {

                switch (source) {
                    case MovieEntry.SOURCE_FAVORITE: {
                        Log.v(LOG_TAG, "Attempted to sync offline source " + source);
                        return;
                    }
                    case MovieEntry.SOURCE_NOW_PLAYING: {
                        uri = API_BASE_URI.buildUpon().appendPath(MOVIE_PATH).appendPath(NOW_PLAYING_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case MovieEntry.SOURCE_POPULAR: {
                        uri = API_BASE_URI.buildUpon().appendPath(MOVIE_PATH).appendPath(POPULAR_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case MovieEntry.SOURCE_RATING: {
                        uri = API_BASE_URI.buildUpon().appendPath(MOVIE_PATH).appendPath(RATING_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case MovieEntry.SOURCE_SEARCH: {
                        return;
                    }
                    case MovieEntry.SOURCE_UPCOMING: {
                        uri = API_BASE_URI.buildUpon().appendPath(MOVIE_PATH).appendPath(UPCOMING_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    default: {
                        Log.e(LOG_TAG, "Unknown source: " + source);
                        return;
                    }
                }

                key = new String[]{source};
                contentUri = MovieEntry.CONTENT_URI;
                apiColumns = MovieEntry.API_COLUMNS;
                nonApiColumns = MovieEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_MOVIE;
                selection = MovieEntry.selectSource();
                update = false;
                break;
            }
            case SYNC_MOVIE_REVIEW: {

                uri = API_BASE_URI.buildUpon()
                        .appendPath(MOVIE_PATH)
                        .appendPath(source).appendPath(REVIEW_PATH)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source};
                contentUri = ReviewEntry.CONTENT_URI;
                apiColumns = ReviewEntry.API_COLUMNS;
                nonApiColumns = ReviewEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_REVIEW;
                selection = ReviewEntry.selectId();
                update = false;
                break;
            }
            case SYNC_MOVIE_SEARCH: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(SEARCH_PATH)
                        .appendPath(MOVIE_PATH)
                        .appendQueryParameter(API_QUERY_PARAM, source)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                key = new String[]{MovieEntry.SOURCE_SEARCH};
                contentUri = MovieEntry.CONTENT_URI;
                apiColumns = MovieEntry.API_COLUMNS;
                nonApiColumns = MovieEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_MOVIE;
                selection = MovieEntry.selectSource();
                update = false;
                break;
            }
            case SYNC_MOVIE_TRAILER: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(MOVIE_PATH)
                        .appendPath(source).appendPath(TRAILER_PATH)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source, MovieContract.PATH_MOVIE};
                contentUri = TrailerEntry.CONTENT_URI;
                apiColumns = TrailerEntry.API_COLUMNS;
                nonApiColumns = TrailerEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_MOVIE + "_" + MovieContract.PATH_TRAILER;
                selection = TrailerEntry.selectId();
                update = false;
                break;
            }
            case SYNC_MOVIE_DETAIL: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(MOVIE_PATH)
                        .appendPath(source)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source};
                contentUri = MovieEntry.CONTENT_URI;
                apiColumns = MovieEntry.API_COLUMNS;
                nonApiColumns = MovieEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_MOVIE;
                selection = MovieEntry.selectId();
                update = true;
                break;
            }
            case SYNC_TV: {

                switch (source) {
                    case TVEntry.SOURCE_FAVORITE: {
                        Log.v(LOG_TAG, "Attempted to sync offline source " + source);
                        return;
                    }
                    case TVEntry.SOURCE_AIRING_TODAY: {
                        uri = API_BASE_URI.buildUpon().appendPath(TV_PATH).appendPath(AIRING_TODAY_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case TVEntry.SOURCE_ON_THE_AIR: {
                        uri = API_BASE_URI.buildUpon().appendPath(TV_PATH).appendPath(ON_THE_AIR_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case TVEntry.SOURCE_POPULAR: {
                        uri = API_BASE_URI.buildUpon().appendPath(TV_PATH).appendPath(POPULAR_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case TVEntry.SOURCE_RATING: {
                        uri = API_BASE_URI.buildUpon().appendPath(TV_PATH).appendPath(RATING_PATH)
                                .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case TVEntry.SOURCE_SEARCH: {
                        return;
                    }
                    default: {
                        Log.e(LOG_TAG, "Unknown source: " + source);
                        return;
                    }
                }

                key = new String[]{source};
                contentUri = TVEntry.CONTENT_URI;
                apiColumns = TVEntry.API_COLUMNS;
                nonApiColumns = TVEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TV;
                selection = TVEntry.selectSource();
                update = false;
                break;
            }
            case SYNC_TV_SEARCH: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(SEARCH_PATH)
                        .appendPath(TV_PATH)
                        .appendQueryParameter(API_QUERY_PARAM, source)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                key = new String[]{TVEntry.SOURCE_SEARCH};
                contentUri = TVEntry.CONTENT_URI;
                apiColumns = TVEntry.API_COLUMNS;
                nonApiColumns = TVEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TV;
                selection = TVEntry.selectSource();
                update = false;
                break;
            }
            case SYNC_TV_TRAILER: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(TV_PATH)
                        .appendPath(source).appendPath(TRAILER_PATH)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source, MovieContract.PATH_TV};
                contentUri = TrailerEntry.CONTENT_URI;
                apiColumns = TrailerEntry.API_COLUMNS;
                nonApiColumns = TrailerEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TV + "_" + MovieContract.PATH_TRAILER;
                selection = TrailerEntry.selectId();
                update = false;
                break;
            }
            case SYNC_TV_DETAIL: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(TV_PATH)
                        .appendPath(source)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source};
                contentUri = TVEntry.CONTENT_URI;
                apiColumns = TVEntry.API_COLUMNS;
                nonApiColumns = TVEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TV;
                selection = TVEntry.selectId();
                update = true;
                break;
            }
            default:
                throw new UnsupportedOperationException("Sync type not supported!");
        }

        /**
         * This section uses a shared preference file in order to store the last sync time, and checks it against a minimum
         * time set in the settings panel.
         */
        final SharedPreferences pref = context.getSharedPreferences(context.getPackageName() + "_updates", Context.MODE_PRIVATE);
        final String updateKey = String.format("%s_%s_updated", type, source);
        final long updateInterval = 60000 * Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_update_interval_key), context.getString(R.string.pref_update_interval_default)));
        final long updateTime = pref.getLong(updateKey, 0);
        final long currentTime = (new Date()).getTime();

        // Check if we are past the minimum sync time or we are force updating
        if ((currentTime - updateTime) > updateInterval || force) {

            // Creates the JSON request object to pass to volley.
            JsonObjectRequest request = new JsonObjectRequest(uri.toString(), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // After we get the response try to get the results array, inserting the data in a new one if not present
                        JSONArray data = (response.has(API_RESULT_ARRAY_KEY)) ?
                                response.getJSONArray(API_RESULT_ARRAY_KEY) : new JSONArray().put(response);

                        // Check the length of the data received, and only do an update if there is actually data to use
                        int dataLength = data.length();
                        int inserted;
                        if (dataLength > 0) {
                            ContentValues[] values = new ContentValues[dataLength];

                            // For loop that goes through each element in the data array
                            for (int i = 0; i < dataLength; i++) {
                                JSONObject object = data.getJSONObject(i);
                                values[i] = new ContentValues(apiColumns.length + nonApiColumns.length);

                                // Add our non api columns with their values
                                if (!update) {
                                    for (int j = 0; j < nonApiColumns.length; j++) {
                                        values[i].put(nonApiColumns[j].COLUMN, key[j]);
                                    }
                                }

                                // Go through each of the api columns and pull the data from the response into a content value column
                                // We have it getting optional values in order to ensure that there are no nulls in the database
                                for (MovieContract.DatabaseColumn apiColumn : apiColumns) {
                                    switch (apiColumn.TYPE) {
                                        case "integer": {
                                            values[i].put(apiColumn.COLUMN, object.optInt(apiColumn.API, -1));
                                            break;
                                        }
                                        case "text": {
                                            values[i].put(apiColumn.COLUMN, object.optString(apiColumn.API, "null"));
                                            break;
                                        }
                                        case "real": {
                                            values[i].put(apiColumn.COLUMN, object.optDouble(apiColumn.API, -1));
                                            break;
                                        }
                                    }
                                }
                            }

                            // If we have to update, update the rows, otherwise we delete the required rows and do a bulk insert
                            if (update) {
                                inserted = 0;
                                for (ContentValues value : values) {
                                    inserted += context.getContentResolver().update(contentUri, value, selection, key);
                                }
                            } else {
                                context.getContentResolver().delete(contentUri, selection, key);
                                inserted = context.getContentResolver().bulkInsert(contentUri, values);
                            }
                            // Simple message to print out how much the database was altered.
                            Log.e(LOG_TAG, String.format("Inserted %d %s's into the %s ContentProvider", inserted, type, contentUri));

                            // Update the last sync time in the shared preferences
                            pref.edit().putLong(updateKey, (new Date()).getTime()).apply();
                        }

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, String.format("Error parsing the %s JSON Object: %s", type, e.getLocalizedMessage()));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(LOG_TAG, String.format("Error retrieving the %s data from theMovieDB: %s", type, error.getLocalizedMessage()));
                }
            });

            VolleySingleton.getInstance(context).getRequestQueue().add(request);
        }
    }

    // Simple Definition for all of the synctypes, for static checking of the sync type
    @IntDef({
            SYNC_MOVIE,
            SYNC_MOVIE_REVIEW,
            SYNC_MOVIE_TRAILER,
            SYNC_MOVIE_DETAIL,
            SYNC_MOVIE_SEARCH,
            SYNC_TV,
            SYNC_TV_TRAILER,
            SYNC_TV_DETAIL,
            SYNC_TV_SEARCH
    })
    public @interface SyncType {
    }
}