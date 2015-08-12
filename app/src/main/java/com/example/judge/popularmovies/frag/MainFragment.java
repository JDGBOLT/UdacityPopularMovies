/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Context;
import android.content.Intent;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.act.MovieActivity;
import com.example.judge.popularmovies.api.TmdbApiHandler;
import com.example.judge.popularmovies.api.VolleySingleton;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;


/**
 * Main fragment of the Popular Movies app, provides a grid view of movie poster thumbnails that can
 * then be touched on in order to provide more detailed movie information in a separate view.
 */

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_LOADER = 0;
    private final String LOG_TAG = MainFragment.class.getSimpleName();
    private final String BUNDLE_LAYOUT_TAG = "layout";

    private SharedPreferences mPref;

    private String mSource = MovieEntry.SOURCE_POPULAR;
    private MoviePosterAdaptor mAdaptor;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_LAYOUT_TAG, mLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_moviepost);
        mRecyclerView.setAdapter(mAdaptor);
        mRecyclerView.setLayoutManager(mLayoutManager);
        return rootView;
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

    private void loadMovieData() {
        String oldSource = mSource;
        mSource = mPref.getString(getString(R.string.pref_source_key), getString(R.string.pref_source_default));
        TmdbApiHandler.sync(TmdbApiHandler.SYNC_MOVIE, mSource, getActivity());
        if (!mSource.equals(oldSource)) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String source = mPref.getString(getString(R.string.pref_source_key), getString(R.string.pref_source_default));
        return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI,
                new String[]{MovieEntry.POSTER_PATH.COLUMN, MovieEntry.ORIGINAL_TITLE.COLUMN, MovieEntry.MOVIE_ID.COLUMN},
                MovieEntry.selectSource(), new String[]{source}, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdaptor.swapCursor(null);
    }

    /**
     * Adaptor in order to feed the gridview movie posters, uses volley in order to provide
     * dynamically cached images for use in the view
     */

    private class MoviePosterAdaptor extends RecyclerView.Adapter<MoviePosterAdaptor.ViewHolder> {

        private Context mContext;
        private Cursor mCursor;
        private int mColumnPosterPath;
        private int mColumnTitle;
        private int mColumnMovieId;

        public MoviePosterAdaptor(Context c) {
            mContext = c;
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            if (cursor != null) {
                mColumnPosterPath = mCursor.getColumnIndex(MovieContract.MovieEntry.POSTER_PATH.COLUMN);
                mColumnTitle = mCursor.getColumnIndex(MovieContract.MovieEntry.ORIGINAL_TITLE.COLUMN);
                mColumnMovieId = mCursor.getColumnIndex(MovieEntry.MOVIE_ID.COLUMN);
            }
            notifyDataSetChanged();
        }

        public Cursor getCursor() {
            return mCursor;
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) return mCursor.getCount();
            else return 0;
        }

        @Override
        public MoviePosterAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movieposter, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MoviePosterAdaptor.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            String imageUrl = getString(R.string.api_poster_base_path) + mCursor.getString(mColumnPosterPath);
            holder.mImageView.setImageUrl(imageUrl, VolleySingleton.getInstance(getActivity()).getImageLoader());
            holder.mImageView.setDefaultImageResId(R.drawable.noposter);
            holder.mImageView.setErrorImageResId(R.drawable.noposter);
            holder.mTextView.setText(mCursor.getString(mColumnTitle));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private NetworkImageView mImageView;
            private TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                mImageView = (NetworkImageView) v.findViewById(R.id.grid_item_moviepost_imageview);
                mTextView = (TextView) v.findViewById(R.id.grid_item_moviepost_textview);
            }

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent movieIntent = new Intent(getActivity(), MovieActivity.class);
                mCursor.moveToPosition(getAdapterPosition());
                movieIntent.putExtra(Intent.EXTRA_SUBJECT, mCursor.getInt(mColumnMovieId));

                startActivity(movieIntent);
            }
        }
    }
}
