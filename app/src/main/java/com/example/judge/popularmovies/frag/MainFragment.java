/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.act.SettingsActivity;
import com.example.judge.popularmovies.adap.MoviePosterAdaptor;
import com.example.judge.popularmovies.api.TmdbApiHandler;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;


/**
 * Main fragment of the Popular Movies app, provides a grid view of movie poster thumbnails that can
 * then be touched on in order to provide more detailed movie information in a separate view.
 */

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Movie Loader
    private static final int MOVIE_LOADER = 0;
    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.MOVIE_ID.COLUMN,
            MovieEntry.ORIGINAL_TITLE.COLUMN,
            MovieEntry.POSTER_PATH.COLUMN,
            MovieEntry.BACKDROP_PATH.COLUMN,
    };
    private static final int COLUMN_MOVIE_ID = 0;
    private static final int COLUMN_ORIGINAL_TITLE = 1;
    private static final int COLUMN_POSTER_PATH = 2;
    private static final int COLUMN_BACKDROP_PATH = 3;
    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String BUNDLE_LAYOUT_TAG = "layout";
    private Cursor mMovies;
    private NetworkImageView mNavHeader;
    private SharedPreferences mPref;
    private String mSource = MovieEntry.SOURCE_POPULAR;
    private SwipeRefreshLayout mSwipeRefresh;
    private MoviePosterAdaptor mAdaptor;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_LAYOUT_TAG, mLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mDrawerLayout = (DrawerLayout) rootView.findViewById(R.id.main_drawer);
        mNavHeader = (NetworkImageView) rootView.findViewById(R.id.main_nav_header_image);

        setupRecycler(rootView);
        setupToolbar(rootView);
        setupNav(rootView);
        setupSwipeToRefresh(rootView);
        setHasOptionsMenu(true);

        return rootView;
    }

    private void setupSwipeToRefresh(View view) {
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.main_swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE, mSource, getActivity(), true);
            }
        });
    }

    private void setupRecycler(View view) {

        int numColumns = Integer.parseInt(mPref.getString(
                getString(R.string.pref_column_portrait_key),
                getString(R.string.pref_column_portrait_default)));

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numColumns = Integer.parseInt(mPref.getString(
                    getString(R.string.pref_column_landscape_key),
                    getString(R.string.pref_column_landscape_default)));
        }

        mAdaptor = new MoviePosterAdaptor(getActivity());
        mLayoutManager = new GridLayoutManager(getActivity(), numColumns);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_moviepost);
        recyclerView.setAdapter(mAdaptor);
        recyclerView.setLayoutManager(mLayoutManager);

    }

    private void setupToolbar(View view) {

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.access_drawer_open, R.string.access_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.syncState();
    }

    private void setupNav(View view) {
        NavigationView nav = (NavigationView) view.findViewById(R.id.main_nav_view);
        nav.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_source_favorite: {
                                getActivity().setTitle(menuItem.getTitle() + " Movies");
                                changeSource(MovieEntry.SOURCE_FAVORITE);
                                menuItem.setChecked(true);
                                break;
                            }
                            case R.id.nav_source_now_playing: {
                                getActivity().setTitle(menuItem.getTitle() + " Movies");
                                changeSource(MovieEntry.SOURCE_NOW_PLAYING);
                                menuItem.setChecked(true);
                                break;
                            }
                            case R.id.nav_source_popular: {
                                getActivity().setTitle(menuItem.getTitle() + " Movies");
                                changeSource(MovieEntry.SOURCE_POPULAR);
                                menuItem.setChecked(true);
                                break;
                            }
                            case R.id.nav_source_search: {
                                getActivity().setTitle(menuItem.getTitle());
                                changeSource(MovieEntry.SOURCE_SEARCH);
                                menuItem.setChecked(true);
                                break;
                            }
                            case R.id.nav_source_top_rated: {
                                getActivity().setTitle(menuItem.getTitle() + " Movies");
                                changeSource(MovieEntry.SOURCE_RATING);
                                menuItem.setChecked(true);
                                break;
                            }
                            case R.id.nav_source_upcoming: {
                                getActivity().setTitle(menuItem.getTitle() + " Movies");
                                changeSource(MovieEntry.SOURCE_UPCOMING);
                                menuItem.setChecked(true);
                                break;
                            }
                            case R.id.nav_settings: {
                                mDrawerLayout.closeDrawers();
                                startActivity(new Intent(getActivity(), SettingsActivity.class));
                            }
                        }
                        mDrawerLayout.closeDrawers();
                        return false;
                    }
                }
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_LAYOUT_TAG));
        }
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
        loadMovieData();
    }

    /**
     * This function actually has volley pull the data from theMovieDB, has it, once it retrieves the json
     * data, to parse it into a JSONObject, we then pull the array of movie listings out of that object
     * and use it to populate the arraylist of Movie objects.
     */

    private void changeSource(String source) {
        mPref.edit().putString(getString(R.string.pref_source_key), source).apply();
        loadMovieData();
    }

    private void loadMovieData() {
        String oldSource = mSource;
        mSource = mPref.getString(getString(R.string.pref_source_key), MovieEntry.SOURCE_POPULAR);
        TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE, mSource, getActivity());
        if (!mSource.equals(oldSource)) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String source = mPref.getString(getString(R.string.pref_source_key), MovieEntry.SOURCE_POPULAR);
        return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS, MovieEntry.selectSource(), new String[]{source}, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovies = data;
        mAdaptor.swapCursor(mMovies);
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovies = null;
        mAdaptor.swapCursor(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
