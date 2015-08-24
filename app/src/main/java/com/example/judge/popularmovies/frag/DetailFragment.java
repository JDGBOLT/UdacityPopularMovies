/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.act.MainActivity;
import com.example.judge.popularmovies.adap.DetailAdaptor;
import com.example.judge.popularmovies.api.TmdbApiHandler;
import com.example.judge.popularmovies.api.VolleySingleton;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.ReviewEntry;
import com.example.judge.popularmovies.data.MovieContract.TVEntry;
import com.example.judge.popularmovies.data.MovieContract.TrailerEntry;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Fragment for the Detailed Movie information window, contains various movie information, including
 * movie poster, year of release, synopsis, average rating, and movie title.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final int COLUMN_BACKDROP_PATH = 0;
    public static final int COLUMN_HOMEPAGE = 1;
    public static final int COLUMN_MEDIA_ID = 2;
    public static final int COLUMN_ORIGINAL_LANGUAGE = 3;
    public static final int COLUMN_ORIGINAL_TITLE = 4;
    public static final int COLUMN_OVERVIEW = 5;
    public static final int COLUMN_POPULARITY = 6;
    public static final int COLUMN_POSTER_PATH = 7;
    public static final int COLUMN_RATING = 8;
    public static final int COLUMN_STATUS = 9;
    public static final int COLUMN_TITLE = 10;
    public static final int COLUMN_VOTE_COUNT = 11;
    public static final int COLUMN_IMDB_ID = 12;
    public static final int COLUMN_RELEASE_DATE = 13;
    public static final int COLUMN_RUNTIME = 14;
    public static final int COLUMN_TAGLINE = 15;
    public static final int COLUMN_FIRST_AIR_DATE = 12;
    public static final int COLUMN_LAST_AIR_DATE = 13;
    public static final int COLUMN_NUMBER_OF_SEASONS = 14;
    public static final int COLUMN_NUMBER_OF_EPISODES = 15;
    public static final int COLUMN_AUTHOR = 0;
    public static final int COLUMN_REVIEW = 1;
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_SITE = 1;
    public static final int COLUMN_KEY = 2;
    // Loader ID's
    private static final int DETAIL_LOADER = 0;
    private static final int REVIEW_LOADER = 1;
    private static final int TRAILER_LOADER = 2;
    // Column data for cursors shared by both Movie and TV media types
    private static final String[] DETAIL_SHARED_COLUMNS = {
            MovieEntry.BACKDROP_PATH.COLUMN,
            MovieEntry.HOMEPAGE.COLUMN,
            MovieEntry.MEDIA_ID.COLUMN,
            MovieEntry.ORIGINAL_LANGUAGE.COLUMN,
            MovieEntry.ORIGINAL_TITLE.COLUMN,
            MovieEntry.OVERVIEW.COLUMN,
            MovieEntry.POPULARITY.COLUMN,
            MovieEntry.POSTER_PATH.COLUMN,
            MovieEntry.RATING.COLUMN,
            MovieEntry.STATUS.COLUMN,
            MovieEntry.TITLE.COLUMN,
            MovieEntry.VOTE_COUNT.COLUMN
    };
    // Columns only used by the Movie media type
    private static final String[] DETAIL_MOVIE_COLUMNS = {
            MovieEntry.IMDB_ID.COLUMN,
            MovieEntry.RELEASE_DATE.COLUMN,
            MovieEntry.RUNTIME.COLUMN,
            MovieEntry.TAGLINE.COLUMN
    };
    // Columns only used by the TV media type
    private static final String[] DETAIL_TV_COLUMNS = {
            TVEntry.FIRST_AIR_DATE.COLUMN,
            TVEntry.LAST_AIR_DATE.COLUMN,
            TVEntry.NUMBER_OF_SEASONS.COLUMN,
            TVEntry.NUMBER_OF_EPISODES.COLUMN
    };
    // Review cursor column information
    private static final String[] REVIEW_COLUMNS = {
            ReviewEntry.AUTHOR.COLUMN,
            ReviewEntry.REVIEW.COLUMN
    };
    // Trailer cursor column information
    private static final String[] TRAILER_COLUMNS = {
            TrailerEntry.NAME.COLUMN,
            TrailerEntry.SITE.COLUMN,
            TrailerEntry.KEY.COLUMN
    };
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    // Global Views, populated by ButterKnife
    @Bind(R.id.detail_recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.detail_backdrop)
    NetworkImageView mBackdrop;
    @Bind(R.id.detail_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.detail_favorite_fab)
    FloatingActionButton mFab;
    @Bind(R.id.detail_collapsing_toolbar)
    CollapsingToolbarLayout mCollapsing;
    private DetailAdaptor mAdaptor;

    // Selection settings for the cursor initialization
    private Uri mDetailUri;
    private String[] mColumns;
    private String mMediaSelection;
    private Cursor mDetailCursor;

    // Detail on the media type and whether it is favorited
    private int mMediaId;
    private boolean mFavorited;
    private String mType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set the arguments that are passed in when we create the instance of the fragment
        if (getArguments() != null) {
            mMediaId = getArguments().getInt(MainActivity.BUNDLE_DETAIL_MEDIAID);
            mType = getArguments().getString(MainActivity.BUNDLE_DETAIL_TYPE);
        }

        View mLayout = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, mLayout);

        // Setup the RecyclerView
        mAdaptor = new DetailAdaptor(getActivity(), mType);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setAdapter(mAdaptor);
        mRecyclerView.setLayoutManager(manager);

        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        // Sync the data an initialize the loaders, this fragment supports both TV and Movie media types, so separate logic is needed
        switch (mType) {
            case MovieContract.PATH_MOVIE: {

                mDetailUri = MovieEntry.CONTENT_URI;
                mColumns = MovieContract.DatabaseColumn.concat(DETAIL_SHARED_COLUMNS, DETAIL_MOVIE_COLUMNS);
                mMediaSelection = MovieEntry.selectSource() + " and " + MovieEntry.selectId();

                // Setup up the Syncing
                TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE_DETAIL, Integer.toString(mMediaId), getActivity(), true);
                TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE_REVIEW, Integer.toString(mMediaId), getActivity());
                TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE_TRAILER, Integer.toString(mMediaId), getActivity());

                // Setup the Loaders
                getLoaderManager().initLoader(DETAIL_LOADER, null, this);
                getLoaderManager().initLoader(TRAILER_LOADER, null, this);
                getLoaderManager().initLoader(REVIEW_LOADER, null, this);
                break;
            }
            case MovieContract.PATH_TV: {

                mDetailUri = TVEntry.CONTENT_URI;
                mColumns = MovieContract.DatabaseColumn.concat(DETAIL_SHARED_COLUMNS, DETAIL_TV_COLUMNS);
                mMediaSelection = TVEntry.selectSource() + " and " + TVEntry.selectId();

                // Setup the Syncing
                TmdbApiHandler.sync(TmdbApiHandler.SYNC_TV_DETAIL, Integer.toString(mMediaId), getActivity());
                TmdbApiHandler.sync(TmdbApiHandler.SYNC_TV_TRAILER, Integer.toString(mMediaId), getActivity());

                // Setup the Loaders
                getLoaderManager().initLoader(DETAIL_LOADER, null, this);
                getLoaderManager().initLoader(TRAILER_LOADER, null, this);
                break;
            }
        }

        // Check whether this is a favorited movie, and if so set the fab drawable, and setup a click listener on the fab
        if (isFavorite()) {
            mFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_white_24dp));
        }
        mFab.setOnClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }

    // Setup method for the 3 loaders
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DETAIL_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        mDetailUri,
                        mColumns,
                        MovieEntry.selectId(),
                        new String[]{Integer.toString(mMediaId)},
                        null);
            }
            case REVIEW_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        ReviewEntry.CONTENT_URI,
                        REVIEW_COLUMNS,
                        ReviewEntry.selectId(),
                        new String[]{Integer.toString(mMediaId)},
                        null);
            }
            case TRAILER_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        TrailerEntry.CONTENT_URI,
                        TRAILER_COLUMNS,
                        TrailerEntry.selectId(),
                        new String[]{Integer.toString(mMediaId), mType},
                        null);
            }
        }
        return null;
    }

    /*
    Once we get the cursor data back from the loaders swap the cursors into the RecyclerView, and also
    set the backdrop and check whether we have manually synced and cleared out the movie/tv data in the meantime,
    and if so resync the more detailed data.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DETAIL_LOADER: {
                data.moveToFirst();
                if (data.getString(COLUMN_STATUS).equals("null")) {
                    int syncType = (mType.equals(MovieContract.PATH_TV)) ? TmdbApiHandler.SYNC_TV_DETAIL : TmdbApiHandler.SYNC_MOVIE_DETAIL;
                    TmdbApiHandler.sync(syncType, Integer.toString(mMediaId), getActivity(), true);
                }
                mDetailCursor = data;
                mBackdrop.setImageUrl(getString(R.string.api_backdrop_base_path) + data.getString(COLUMN_BACKDROP_PATH),
                        VolleySingleton.getInstance(getActivity()).getImageLoader());
                mToolbar.setTitle(data.getString(COLUMN_TITLE));
                mAdaptor.swapMediaCursor(data);
                break;
            }
            case REVIEW_LOADER: {
                mAdaptor.swapReviewCursor(data);
                break;
            }
            case TRAILER_LOADER: {
                mAdaptor.swapTrailerCursor(data);
                break;
            }
        }
    }

    // Set the cursors as null in the RecyclerView when they are reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()) {
            case DETAIL_LOADER: {
                mAdaptor.swapMediaCursor(null);
                break;
            }
            case REVIEW_LOADER: {
                mAdaptor.swapReviewCursor(null);
                break;
            }
            case TRAILER_LOADER: {
                mAdaptor.swapTrailerCursor(null);
                break;
            }
        }
    }

    // ButterKnife recommends calling this within fragments
    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    // Method to check the database if we have the current mediaid favorited
    private boolean isFavorite() {
        Cursor cursor = getActivity().getContentResolver().query(mDetailUri, new String[]{MovieEntry._ID},
                mMediaSelection, new String[]{MovieEntry.SOURCE_FAVORITE, Integer.toString(mMediaId)}, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            mFavorited = false;
            return false;
        }
        cursor.close();
        mFavorited = true;
        return true;
    }

    // OnClick method for the fab in order to change the favorited status
    @Override
    public void onClick(View v) {
        if (!mFavorited) {
            mDetailCursor.moveToFirst();
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(mDetailCursor, values);
            values.put(MovieEntry.SOURCE.COLUMN, MovieEntry.SOURCE_FAVORITE);
            getActivity().getContentResolver().insert(mDetailUri, values);
            mFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_white_24dp));
            mFavorited = true;
        } else {
            getActivity().getContentResolver().delete(mDetailUri, mMediaSelection, new String[]{MovieEntry.SOURCE_FAVORITE, Integer.toString(mMediaId)});
            mFab.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_border_white_24dp));
            mFavorited = false;
        }
    }
}
