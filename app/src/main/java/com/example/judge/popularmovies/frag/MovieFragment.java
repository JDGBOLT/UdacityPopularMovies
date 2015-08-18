/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.api.TmdbApiHandler;
import com.example.judge.popularmovies.api.VolleySingleton;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.ReviewEntry;
import com.example.judge.popularmovies.data.MovieContract.TrailerEntry;


/**
 * Fragment for the Detailed Movie information window, contains various movie information, including
 * movie poster, year of release, synopsis, average rating, and movie title.
 */

public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private static final int REVIEW_LOADER = 1;
    private static final int TRAILER_LOADER = 2;
    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.ORIGINAL_TITLE.COLUMN,
            MovieEntry.POSTER_PATH.COLUMN,
            MovieEntry.RELEASE_DATE.COLUMN,
            MovieEntry.OVERVIEW.COLUMN,
            MovieEntry.RATING.COLUMN
    };
    private static final String[] REVIEW_COLUMNS = {
            ReviewEntry.AUTHOR.COLUMN,
            ReviewEntry.REVIEW.COLUMN
    };
    private static final String[] TRAILER_COLUMNS = {
            TrailerEntry.NAME.COLUMN,
            TrailerEntry.SITE.COLUMN,
            TrailerEntry.KEY.COLUMN
    };
    private static final String LOG_TAG = MovieFragment.class.getSimpleName();
    private int mMovieId;
    private View mLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_movie, container, false);
        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        mMovieId = intent.getIntExtra(Intent.EXTRA_SUBJECT, 0);
        TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE_DETAIL, Integer.toString(mMovieId), getActivity());
        TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE_REVIEW, Integer.toString(mMovieId), getActivity());
        TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE_TRAILER, Integer.toString(mMovieId), getActivity());

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DETAIL_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        MovieEntry.CONTENT_URI,
                        DETAIL_COLUMNS,
                        MovieEntry.selectId(),
                        new String[]{Integer.toString(mMovieId)},
                        null);
            }
            case REVIEW_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        ReviewEntry.CONTENT_URI,
                        REVIEW_COLUMNS,
                        ReviewEntry.selectId(),
                        new String[]{Integer.toString(mMovieId)},
                        null);
            }
            case TRAILER_LOADER: {
                return new CursorLoader(
                        getActivity(),
                        TrailerEntry.CONTENT_URI,
                        TRAILER_COLUMNS,
                        TrailerEntry.selectId(),
                        new String[]{Integer.toString(mMovieId)},
                        null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DETAIL_LOADER: {
                data.moveToFirst();
                int titleColumn = data.getColumnIndex(MovieEntry.ORIGINAL_TITLE.COLUMN);
                int dateColumn = data.getColumnIndex(MovieEntry.RELEASE_DATE.COLUMN);
                int overviewColumn = data.getColumnIndex(MovieEntry.OVERVIEW.COLUMN);
                int ratingColumn = data.getColumnIndex(MovieEntry.RATING.COLUMN);
                int posterColumn = data.getColumnIndex(MovieEntry.POSTER_PATH.COLUMN);
                String imageUrl = getString(R.string.api_poster_base_path) + data.getString(posterColumn);
                ((TextView) mLayout.findViewById(R.id.movie_title)).setText(data.getString(titleColumn));
                ((TextView) mLayout.findViewById(R.id.movie_year)).setText("Release Date: " + data.getString(dateColumn));
                ((TextView) mLayout.findViewById(R.id.movie_synopsis)).setText("Overview: " + data.getString(overviewColumn));
                ((TextView) mLayout.findViewById(R.id.movie_rating)).setText("Rating: " + data.getDouble(ratingColumn) + "/10.0");
                ((NetworkImageView) mLayout.findViewById(R.id.movie_poster)).setImageUrl(imageUrl, VolleySingleton.getInstance(getActivity()).getImageLoader());
            }
            case REVIEW_LOADER: {
                break;
            }
            case TRAILER_LOADER: {
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()) {
            case DETAIL_LOADER: {
                break;
            }
            case REVIEW_LOADER: {
                break;
            }
            case TRAILER_LOADER: {
                break;
            }
        }
    }
}
