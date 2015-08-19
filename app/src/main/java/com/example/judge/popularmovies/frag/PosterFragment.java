/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.adap.PosterAdaptor;
import com.example.judge.popularmovies.api.TmdbApiHandler;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.TVEntry;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Main fragment of the Popular Movies app, provides a grid view of movie poster thumbnails that can
 * then be touched on in order to provide more detailed movie information in a separate view.
 */

public class PosterFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    // Movie Loader
    private static final int POSTER_LOADER = 0;
    private static final String[] POSTER_COLUMNS = {
            MovieEntry.MEDIA_ID.COLUMN,
            MovieEntry.ORIGINAL_TITLE.COLUMN,
            MovieEntry.POSTER_PATH.COLUMN,
    };
    private static final int COLUMN_MEDIA_ID = 0;
    private static final int COLUMN_ORIGINAL_TITLE = 1;
    private static final int COLUMN_POSTER_PATH = 2;

    private static final String LOG_TAG = PosterFragment.class.getSimpleName();

    private static final String BUNDLE_SOURCE_TAG = "source";
    private static final String BUNDLE_TYPE_TAG = "type";
    @Bind(R.id.main_swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.recyclerview_moviepost)
    RecyclerView mRecyclerView;
    private String mSource, mType;
    private SharedPreferences mPref;
    private PosterAdaptor mAdaptor;

    public static PosterFragment newInstance(String source, String type) {

        Bundle args = new Bundle();

        PosterFragment fragment = new PosterFragment();
        args.putString(BUNDLE_SOURCE_TAG, source);
        args.putString(BUNDLE_TYPE_TAG, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ButterKnife.bind(this, rootView);

        if (getArguments() != null) {
            mSource = getArguments().getString(BUNDLE_SOURCE_TAG);
            mType = getArguments().getString(BUNDLE_TYPE_TAG);
        }

        setupRecycler();
        setupSwipeToRefresh();

        return rootView;
    }

    private void setupSwipeToRefresh() {
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (mType) {
                    case MovieContract.PATH_MOVIE: {
                        TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE, mSource, getActivity(), true);
                        break;
                    }
                    case MovieContract.PATH_TV: {
                        TmdbApiHandler.sync(TmdbApiHandler.SYNC_TV, mSource, getActivity(), true);
                        break;
                    }
                }
            }
        });
    }

    private void setupRecycler() {

        int numColumns = Integer.parseInt(mPref.getString(
                getString(R.string.pref_column_portrait_key),
                getString(R.string.pref_column_portrait_default)));

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numColumns = Integer.parseInt(mPref.getString(
                    getString(R.string.pref_column_landscape_key),
                    getString(R.string.pref_column_landscape_default)));
        }

        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), numColumns);
        mAdaptor = new PosterAdaptor(getActivity());
        mRecyclerView.setAdapter(mAdaptor);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(POSTER_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Within onStart, we override it to provide the custom BasicAdaptor for the gridview, we also
     * register the callback function in order to launch the movie detail activity in order to show
     * more detailed movie data. We provide the movie data needed for the movie detail view as extra
     * tags sent along with the intent.
     */

    @Override
    public void onStart() {
        super.onStart();
        mPref.registerOnSharedPreferenceChangeListener(this);
        loadMovieData();
    }

    /**
     * This function actually has volley pull the data from theMovieDB, has it, once it retrieves the json
     * data, to parse it into a JSONObject, we then pull the array of movie listings out of that object
     * and use it to populate the arraylist of Movie objects.
     */

    private void loadMovieData() {
        if (mType.equals(MovieContract.PATH_MOVIE)) {
            TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE, mSource, getActivity());
        } else if (mType.equals(MovieContract.PATH_TV)) {
            TmdbApiHandler.sync(TmdbApiHandler.SYNC_TV, mSource, getActivity());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (mType) {
            case MovieContract.PATH_MOVIE: {
                return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI,
                        POSTER_COLUMNS, MovieEntry.selectSource(), new String[]{mSource}, null);
            }
            case MovieContract.PATH_TV: {
                return new CursorLoader(getActivity(), TVEntry.CONTENT_URI,
                        POSTER_COLUMNS, TVEntry.selectSource(), new String[]{mSource}, null);
            }
            default: {
                return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI,
                        POSTER_COLUMNS, MovieEntry.selectSource(), new String[]{mSource}, null);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onStop() {
        mPref.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdaptor.swapCursor(data);
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdaptor.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_source_key))) {
            mType = sharedPreferences.getString(key, MovieContract.PATH_MOVIE);
            getLoaderManager().restartLoader(POSTER_LOADER, null, this);
        }
    }
}
