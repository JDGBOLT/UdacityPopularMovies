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

public class TmdbApiHandler {

    public static final int SYNC_MOVIE = 0;
    public static final int SYNC_MOVIE_REVIEW = 1;
    public static final int SYNC_MOVIE_TRAILER = 2;
    public static final int SYNC_MOVIE_DETAIL = 3;
    public static final int SYNC_MOVIE_SEARCH = 4;
    public static final int SYNC_TV = 5;
    public static final int SYNC_TV_TRAILER = 6;
    public static final int SYNC_TV_DETAIL = 7;
    public static final int SYNC_TV_SEARCH = 8;
    private static final Uri API_BASE_URI = Uri.parse("http://api.themoviedb.org/3");
    private static final String MOVIE_PATH = "movie";
    private static final String TV_PATH = "tv";
    private static final String AIRING_TODAY_PATH = "airing_today";
    private static final String ON_THE_AIR_PATH = "on_the_air";
    private static final String NOW_PLAYING_PATH = "now_playing";
    private static final String POPULAR_PATH = "popular";
    private static final String RATING_PATH = "top_rated";
    private static final String UPCOMING_PATH = "upcoming";
    private static final String REVIEW_PATH = "reviews";
    private static final String SEARCH_PATH = "search";
    private static final String TRAILER_PATH = "videos";
    private static final String API_KEY_PARAM = "api_key";
    private static final String API_QUERY_PARAM = "query";
    private static final String API_RESULT_ARRAY_KEY = "results";
    private static final String LOG_TAG = TmdbApiHandler.class.getSimpleName();

    public static void sync(final @SyncType int syncType, final String source, final Context context) {
        sync(syncType, source, context, false);
    }

    public static void sync(final @SyncType int syncType, final String source, final Context context, final boolean force) {

        final MovieContract.DatabaseColumn[] apiColumns, nonApiColumns;
        final boolean update;
        final String[] key;
        final String type, selection;
        final Uri uri, contentUri;

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
                    default:
                        throw new UnsupportedOperationException("Source not known: " + source);
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

                key = new String[]{source};
                contentUri = TrailerEntry.CONTENT_URI;
                apiColumns = TrailerEntry.API_COLUMNS;
                nonApiColumns = TrailerEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TRAILER;
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
                    default:
                        throw new UnsupportedOperationException("Source not known: " + source);
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

                key = new String[]{source};
                contentUri = TrailerEntry.CONTENT_URI;
                apiColumns = TrailerEntry.API_COLUMNS;
                nonApiColumns = TrailerEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TRAILER;
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

        final SharedPreferences pref = context.getSharedPreferences(context.getPackageName() + "_updates", Context.MODE_PRIVATE);
        final String updateKey = String.format("%s_%s_updated", type, source);
        final long updateInterval = 60000 * Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_update_interval_key), context.getString(R.string.pref_update_interval_default)));
        final long updateTime = pref.getLong(updateKey, 0);
        final long currentTime = (new Date()).getTime();

        if ((currentTime - updateTime) > updateInterval || force) {

            JsonObjectRequest request = new JsonObjectRequest(uri.toString(), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray data = (response.has(API_RESULT_ARRAY_KEY)) ?
                                response.getJSONArray(API_RESULT_ARRAY_KEY) : new JSONArray().put(response);

                        int dataLength = data.length();
                        int inserted;
                        if (dataLength > 0) {
                            ContentValues[] values = new ContentValues[dataLength];

                            for (int i = 0; i < dataLength; i++) {
                                JSONObject object = data.getJSONObject(i);
                                values[i] = new ContentValues(apiColumns.length + nonApiColumns.length);
                                if (!update) {
                                    for (int j = 0; j < nonApiColumns.length; j++) {
                                        values[i].put(nonApiColumns[j].COLUMN, key[j]);
                                    }
                                }

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

                            if (update) {
                                inserted = 0;
                                for (ContentValues value : values) {
                                    inserted += context.getContentResolver().update(contentUri, value, selection, key);
                                }
                            } else {
                                context.getContentResolver().delete(contentUri, selection, key);
                                inserted = context.getContentResolver().bulkInsert(contentUri, values);
                            }
                            Log.e(LOG_TAG, String.format("Inserted %d %s's into the %s ContentProvider", inserted, type, contentUri));
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