package com.example.judge.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Arrays;

public class MovieContract {
    // Authority and Base Content URI's for the content provider
    public static final String CONTENT_AUTHORITY = "com.example.judge.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Paths available within the Content Provider
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_TRAILER = "trailer";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MOVIE;
        public static final String TABLE_NAME = "movie";

        public static final String SOURCE_FAVORITE = "favorite";
        public static final String SOURCE_POPULAR = "popular";
        public static final String SOURCE_RATING = "top_rated";

        public static final DatabaseColumn MOVIE_ID = new DatabaseColumn("id", "movie_id", "integer", "not null");
        public static final DatabaseColumn ORIGINAL_TITLE = new DatabaseColumn("original_title", "original_title", "text", "not null");
        public static final DatabaseColumn OVERVIEW = new DatabaseColumn("overview", "overview", "text", "");
        public static final DatabaseColumn RELEASE_DATE = new DatabaseColumn("release_date", "release_date", "text", "not null");
        public static final DatabaseColumn POSTER_PATH = new DatabaseColumn("poster_path", "poster_path", "text", "");
        public static final DatabaseColumn TITLE = new DatabaseColumn("title", "title", "text", "not null");
        public static final DatabaseColumn RATING = new DatabaseColumn("vote_average", "rating", "real", "not null");
        public static final DatabaseColumn SOURCE = new DatabaseColumn("", "source", "text",
                String.format("check(source = \"%s\" or source = \"%s\" or source = \"%s\")", SOURCE_FAVORITE, SOURCE_POPULAR, SOURCE_RATING));

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                SOURCE
        };


        public static final DatabaseColumn[] API_COLUMNS = {
                MOVIE_ID,
                ORIGINAL_TITLE,
                OVERVIEW,
                RELEASE_DATE,
                POSTER_PATH,
                TITLE,
                RATING
        };
        public static final DatabaseColumn[] COLUMNS = DatabaseColumn.concat(NONAPI_COLUMNS, API_COLUMNS);

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String selectId() {
            return MOVIE_ID.COLUMN + " = ?";
        }

        public static String selectSource() {
            return SOURCE.COLUMN + " = ?";
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_REVIEW;
        public static final String TABLE_NAME = "review";

        public static final DatabaseColumn MOVIE_ID = new DatabaseColumn("", "movie_id", "integer", "not null");
        public static final DatabaseColumn AUTHOR = new DatabaseColumn("author", "author", "text", "not null");
        public static final DatabaseColumn REVIEW = new DatabaseColumn("content", "review", "text", "not null");

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                MOVIE_ID
        };

        public static final DatabaseColumn[] API_COLUMNS = {
                AUTHOR,
                REVIEW
        };

        public static final DatabaseColumn[] COLUMNS = DatabaseColumn.concat(NONAPI_COLUMNS, API_COLUMNS);

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String selectId() {
            return MOVIE_ID.COLUMN + " = ?";
        }
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_TRAILER;
        public static final String TABLE_NAME = "trailer";

        public static final DatabaseColumn MOVIE_ID = new DatabaseColumn("", "movie_id", "integer", "not null");
        public static final DatabaseColumn KEY = new DatabaseColumn("key", "key", "text", "not null");
        public static final DatabaseColumn NAME = new DatabaseColumn("name", "name", "text", "not null");
        public static final DatabaseColumn SITE = new DatabaseColumn("site", "site", "text", "not null");

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                MOVIE_ID
        };

        public static final DatabaseColumn[] API_COLUMNS = {
                NAME,
                SITE,
                KEY
        };

        public static final DatabaseColumn[] COLUMNS = DatabaseColumn.concat(NONAPI_COLUMNS, API_COLUMNS);

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String selectId() {
            return MOVIE_ID.COLUMN + " = ?";
        }
    }

    public static final class DatabaseColumn {
        public final String API;
        public final String ARGS;
        public final String COLUMN;
        public final String TYPE;

        public DatabaseColumn(String api, String column, String type, String args) {
            API = api;
            ARGS = args;
            COLUMN = column;
            TYPE = type;
        }

        public static <T> T[] concat(T[] first, T[] second) {
            T[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }

    }

}
