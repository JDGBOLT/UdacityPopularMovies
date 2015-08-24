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
import com.example.judge.popularmovies.act.MainActivity;
import com.example.judge.popularmovies.adap.PosterAdaptor;
import com.example.judge.popularmovies.api.TmdbApiHandler;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.TVEntry;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Poster fragment of the Popular Movies app, provides a grid view of movie poster thumbnails that can
 * then be touched on in order to provide more detailed movie information in a separate view.
 */

public class PosterFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int COLUMN_MEDIA_ID = 0;
    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_POSTER_PATH = 2;
    // Movie Loader
    private static final int POSTER_LOADER = 0;
    // Column information required for the loader
    private static final String[] POSTER_COLUMNS = {
            MovieEntry.MEDIA_ID.COLUMN,
            MovieEntry.TITLE.COLUMN,
            MovieEntry.POSTER_PATH.COLUMN,
    };
    private static final String LOG_TAG = PosterFragment.class.getSimpleName();

    // Tags used for the arguments
    private static final String BUNDLE_SOURCE_TAG = "source";
    private static final String BUNDLE_TYPE_TAG = "type";

    // Global view data populated by ButterKnife
    @Bind(R.id.main_swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.recyclerview_moviepost)
    RecyclerView mRecyclerView;

    // Variabled for storing the source and media type
    private String mSource, mType;
    private SharedPreferences mPref;

    // Adaptor for the recyclerview
    private PosterAdaptor mAdaptor;

    // This is required by the ViewPager in order to create the instance with the correct information
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

        // Read the arguments that were written in from the pager
        if (getArguments() != null) {
            mSource = getArguments().getString(BUNDLE_SOURCE_TAG);
            mType = getArguments().getString(BUNDLE_TYPE_TAG);
        }

        setupRecycler();
        setupSwipeToRefresh();

        return rootView;
    }

    // Method to setup the swipe to refresh functionality, including syncing the right data
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
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    // Method to setup the recycler, including reading the column preferences and using them when creating the Grid layout
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
        mAdaptor = new PosterAdaptor((MainActivity) getActivity());
        mRecyclerView.setAdapter(mAdaptor);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }


    // Initialize the loader
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(POSTER_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }


    // Setup the fragment to listen for preference changes in order for source changes to work
    @Override
    public void onStart() {
        super.onStart();
        mPref.registerOnSharedPreferenceChangeListener(this);
        loadMovieData();
    }

    /**
     * This function starts the handler to load the movie data from TheMovieDB using volley, we do have it
     * start every single time the fragment starts, but there is functionality in the syncer to do timestamps
     * on last sync, and only sync if the configured period of time has elapsed.
     */

    private void loadMovieData() {
        if (mType.equals(MovieContract.PATH_MOVIE)) {
            TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE, mSource, getActivity());
        } else if (mType.equals(MovieContract.PATH_TV)) {
            TmdbApiHandler.sync(TmdbApiHandler.SYNC_TV, mSource, getActivity());
        }
    }

    // Create the loaders, we need different ones depending on if we are using a Movie or TV media source
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

    // ButterKnife recommends to do this in fragments
    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    // Unregister our preference listener
    @Override
    public void onStop() {
        mPref.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    // Swap cursor data into the recycler after load
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdaptor.swapCursor(data, mType);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdaptor.swapCursor(null, null);
    }

    // Listen for preference changes so we can correctly set the source after it being changed in the nav pane.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_source_key))) {
            String newType = sharedPreferences.getString(key, MovieContract.PATH_MOVIE);

            if (!newType.equals(mType)) {
                mType = newType;
                loadMovieData();
                getLoaderManager().restartLoader(POSTER_LOADER, null, this);
            }
        }
    }
}
