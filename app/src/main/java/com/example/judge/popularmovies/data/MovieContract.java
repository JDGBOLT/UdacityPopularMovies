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
    public static final String PATH_TV = "tv";

    // Shared Column Names
    public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
    public static final String COLUMN_MEDIA_ID = "media_id";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_ORIGINAL_TITLE = "original_title";

    public static final class MovieEntry implements BaseColumns {


        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MOVIE;
        public static final String TABLE_NAME = "movie";

        public static final String SOURCE_FAVORITE = "favorite";
        public static final String SOURCE_NOW_PLAYING = "now_playing";
        public static final String SOURCE_POPULAR = "popular";
        public static final String SOURCE_RATING = "top_rated";
        public static final String SOURCE_UPCOMING = "upcoming";
        public static final String SOURCE_SEARCH = "search";


        public static final DatabaseColumn BACKDROP_PATH = new DatabaseColumn("backdrop_path", COLUMN_BACKDROP_PATH, "text", "not null");
        public static final DatabaseColumn HOMEPAGE = new DatabaseColumn("homepage", "homepage", "text", "not null");
        public static final DatabaseColumn IMDB_ID = new DatabaseColumn("imdb_id", "imdb_id", "text", "not null");
        public static final DatabaseColumn MEDIA_ID = new DatabaseColumn("id", COLUMN_MEDIA_ID, "integer", "not null");
        public static final DatabaseColumn ORIGINAL_LANGUAGE = new DatabaseColumn("original_language", "original_language", "text", "not null");
        public static final DatabaseColumn ORIGINAL_TITLE = new DatabaseColumn("original_title", COLUMN_ORIGINAL_TITLE, "text", "not null");
        public static final DatabaseColumn OVERVIEW = new DatabaseColumn("overview", "overview", "text", "not null");
        public static final DatabaseColumn POPULARITY = new DatabaseColumn("popularity", "popularity", "real", "not null");
        public static final DatabaseColumn RELEASE_DATE = new DatabaseColumn("release_date", "release_date", "text", "not null");
        public static final DatabaseColumn POSTER_PATH = new DatabaseColumn("poster_path", COLUMN_POSTER_PATH, "text", "not null");
        public static final DatabaseColumn RUNTIME = new DatabaseColumn("runtime", "runtime", "integer", "not null");
        public static final DatabaseColumn TITLE = new DatabaseColumn("title", "title", "text", "not null");
        public static final DatabaseColumn RATING = new DatabaseColumn("vote_average", "rating", "real", "not null");
        public static final DatabaseColumn SOURCE = new DatabaseColumn("", "source", "text", "not null");
        public static final DatabaseColumn STATUS = new DatabaseColumn("status", "status", "text", "not null");
        public static final DatabaseColumn TAGLINE = new DatabaseColumn("tagline", "tagline", "text", "not null");
        public static final DatabaseColumn VOTE_COUNT = new DatabaseColumn("vote_count", "votes", "integer", "not null");

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                SOURCE
        };


        public static final DatabaseColumn[] API_COLUMNS = {
                BACKDROP_PATH,
                HOMEPAGE,
                IMDB_ID,
                MEDIA_ID,
                ORIGINAL_LANGUAGE,
                ORIGINAL_TITLE,
                OVERVIEW,
                POPULARITY,
                POSTER_PATH,
                RATING,
                RELEASE_DATE,
                RUNTIME,
                STATUS,
                TAGLINE,
                TITLE,
                VOTE_COUNT
        };
        public static final DatabaseColumn[] COLUMNS = DatabaseColumn.concat(NONAPI_COLUMNS, API_COLUMNS);

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String selectId() {
            return MEDIA_ID.COLUMN + " = ?";
        }

        public static String selectSource() {
            return SOURCE.COLUMN + " = ?";
        }
    }

    public static final class TVEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TV).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_TV;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_TV;
        public static final String TABLE_NAME = "tv";

        public static final String SOURCE_AIRING_TODAY = "airing_today";
        public static final String SOURCE_FAVORITE = "favorite";
        public static final String SOURCE_ON_THE_AIR = "on_the_air";
        public static final String SOURCE_POPULAR = "popular";
        public static final String SOURCE_RATING = "top_rated";
        public static final String SOURCE_SEARCH = "search";

        public static final DatabaseColumn BACKDROP_PATH = new DatabaseColumn("backdrop_path", COLUMN_BACKDROP_PATH, "text", "not null");
        public static final DatabaseColumn FIRST_AIR_DATE = new DatabaseColumn("first_air_date", "first_air_date", "text", "not null");
        public static final DatabaseColumn HOMEPAGE = new DatabaseColumn("homepage", "homepage", "text", "not null");
        public static final DatabaseColumn LAST_AIR_DATE = new DatabaseColumn("last_air_date", "last_air_date", "text", "not null");
        public static final DatabaseColumn MEDIA_ID = new DatabaseColumn("id", COLUMN_MEDIA_ID, "integer", "not null");
        public static final DatabaseColumn NUMBER_OF_EPISODES = new DatabaseColumn("number_of_episodes", "number_of_episodes", "integer", "not null");
        public static final DatabaseColumn NUMBER_OF_SEASONS = new DatabaseColumn("number_of_seasons", "number_of_seasons", "integer", "not null");
        public static final DatabaseColumn ORIGINAL_LANGUAGE = new DatabaseColumn("original_language", "original_language", "text", "not null");
        public static final DatabaseColumn ORIGINAL_TITLE = new DatabaseColumn("original_name", "original_title", "text", "not null");
        public static final DatabaseColumn OVERVIEW = new DatabaseColumn("overview", "overview", "text", "not null");
        public static final DatabaseColumn POPULARITY = new DatabaseColumn("popularity", "popularity", "real", "not null");
        public static final DatabaseColumn POSTER_PATH = new DatabaseColumn("poster_path", COLUMN_POSTER_PATH, "text", "not null");
        public static final DatabaseColumn TITLE = new DatabaseColumn("name", "title", "text", "not null");
        public static final DatabaseColumn RATING = new DatabaseColumn("vote_average", "rating", "real", "not null");
        public static final DatabaseColumn SOURCE = new DatabaseColumn("", "source", "text", "not null");
        public static final DatabaseColumn STATUS = new DatabaseColumn("status", "status", "text", "not null");
        public static final DatabaseColumn VOTE_COUNT = new DatabaseColumn("vote_count", "votes", "integer", "not null");

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                SOURCE
        };


        public static final DatabaseColumn[] API_COLUMNS = {
                BACKDROP_PATH,
                FIRST_AIR_DATE,
                HOMEPAGE,
                LAST_AIR_DATE,
                MEDIA_ID,
                NUMBER_OF_EPISODES,
                NUMBER_OF_SEASONS,
                ORIGINAL_LANGUAGE,
                ORIGINAL_TITLE,
                OVERVIEW,
                POPULARITY,
                POSTER_PATH,
                RATING,
                STATUS,
                TITLE,
                VOTE_COUNT
        };
        public static final DatabaseColumn[] COLUMNS = DatabaseColumn.concat(NONAPI_COLUMNS, API_COLUMNS);

        public static Uri buildTVUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String selectId() {
            return MEDIA_ID.COLUMN + " = ?";
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

        public static final DatabaseColumn MEDIA_ID = new DatabaseColumn("", COLUMN_MEDIA_ID, "integer", "not null");
        public static final DatabaseColumn AUTHOR = new DatabaseColumn("author", "author", "text", "not null");
        public static final DatabaseColumn REVIEW = new DatabaseColumn("content", "review", "text", "not null");

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                MEDIA_ID
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
            return MEDIA_ID.COLUMN + " = ?";
        }
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_TRAILER;
        public static final String TABLE_NAME = "trailer";

        public static final DatabaseColumn MEDIA_ID = new DatabaseColumn("", COLUMN_MEDIA_ID, "integer", "not null");
        public static final DatabaseColumn KEY = new DatabaseColumn("key", "key", "text", "not null");
        public static final DatabaseColumn NAME = new DatabaseColumn("name", "name", "text", "not null");
        public static final DatabaseColumn SITE = new DatabaseColumn("site", "site", "text", "not null");

        public static final DatabaseColumn[] NONAPI_COLUMNS = {
                MEDIA_ID
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
            return MEDIA_ID.COLUMN + " = ?";
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
