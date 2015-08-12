package com.example.judge.popularmovies.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.ReviewEntry;
import com.example.judge.popularmovies.data.MovieContract.TrailerEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class TmdbApiHandler {

    public static final int SYNC_MOVIE = 300;
    public static final int SYNC_REVIEW = 400;
    public static final int SYNC_TRAILER = 500;


    private static final Uri API_BASE_URI = Uri.parse("http://api.themoviedb.org/3/movie");
    private static final String POPULAR_PATH = "popular";
    private static final String RATING_PATH = "top_rated";
    private static final String REVIEW_PATH = "reviews";
    private static final String TRAILER_PATH = "videos";
    private static final String API_KEY_PARAM = "api_key";
    private static final String API_RESULT_ARRAY_KEY = "results";
    private static final String LOG_TAG = TmdbApiHandler.class.getSimpleName();

    public static void sync(final int syncType, final String source, final Context context) {
        sync(syncType, source, context, false);
    }

    public static void sync(final int syncType, final String source, final Context context, boolean force) {

        final MovieContract.DatabaseColumn[] apiColumns, nonApiColumns;
        final String[] key;
        final String type, selection;
        final Uri uri, contentUri;

        switch (syncType) {
            case SYNC_MOVIE: {

                switch (source) {
                    case MovieEntry.SOURCE_POPULAR: {
                        uri = API_BASE_URI.buildUpon().appendPath(POPULAR_PATH).appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
                        break;
                    }
                    case MovieEntry.SOURCE_RATING: {
                        uri = API_BASE_URI.buildUpon().appendPath(RATING_PATH).appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();
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
                break;
            }
            case SYNC_REVIEW: {

                uri = API_BASE_URI.buildUpon()
                        .appendPath(source).appendPath(REVIEW_PATH)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source};
                contentUri = ReviewEntry.CONTENT_URI;
                apiColumns = ReviewEntry.API_COLUMNS;
                nonApiColumns = ReviewEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_REVIEW;
                selection = ReviewEntry.selectId();
                break;
            }
            case SYNC_TRAILER: {
                uri = API_BASE_URI.buildUpon()
                        .appendPath(source).appendPath(TRAILER_PATH)
                        .appendQueryParameter(API_KEY_PARAM, TmdbApiKey.KEY).build();

                key = new String[]{source};
                contentUri = TrailerEntry.CONTENT_URI;
                apiColumns = TrailerEntry.API_COLUMNS;
                nonApiColumns = TrailerEntry.NONAPI_COLUMNS;
                type = MovieContract.PATH_TRAILER;
                selection = TrailerEntry.selectId();
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
                        JSONArray data = response.getJSONArray(API_RESULT_ARRAY_KEY);

                        int dataLength = data.length();
                        int inserted;
                        if (dataLength > 0) {
                            ContentValues[] values = new ContentValues[dataLength];

                            for (int i = 0; i < dataLength; i++) {
                                JSONObject object = data.getJSONObject(i);
                                values[i] = new ContentValues(apiColumns.length + nonApiColumns.length);

                                for (int j = 0; j < nonApiColumns.length; j++) {
                                    values[i].put(nonApiColumns[j].COLUMN, key[j]);
                                }

                                for (MovieContract.DatabaseColumn apiColumn : apiColumns) {
                                    switch (apiColumn.TYPE) {
                                        case "integer": {
                                            values[i].put(apiColumn.COLUMN, object.getInt(apiColumn.API));
                                            break;
                                        }
                                        case "text": {
                                            values[i].put(apiColumn.COLUMN, object.getString(apiColumn.API));
                                            break;
                                        }
                                        case "real": {
                                            values[i].put(apiColumn.COLUMN, object.getDouble(apiColumn.API));
                                            break;
                                        }
                                    }
                                }
                            }

                            context.getContentResolver().delete(contentUri, selection, key);

                            inserted = context.getContentResolver().bulkInsert(contentUri, values);
                            Log.e(LOG_TAG, String.format("Inserted %d %s's into the %s ContentProvider", inserted, type, contentUri));
                            pref.edit().putLong(updateKey, (new Date()).getTime()).commit();
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
}