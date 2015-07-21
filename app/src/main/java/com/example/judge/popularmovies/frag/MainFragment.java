/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.act.MovieActivity;
import com.example.judge.popularmovies.api.TmdbApiInterface;
import com.example.judge.popularmovies.api.TmdbSingleton;
import com.example.judge.popularmovies.model.MovieResults;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Main fragment of the Popular Movies app, provides a grid view of movie poster thumbnails that can
 * then be touched on in order to provide more detailed movie information in a separate view.
 */

public class MainFragment extends Fragment {

    private final String LOG_TAG = MainFragment.class.getSimpleName();
    private RecyclerView.Adapter mAdaptor;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<MovieResults.Movie> movies;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("movies", Parcels.wrap(movies));
        outState.putParcelable("layout", mLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mAdaptor = new MoviePosterAdaptor(getActivity());
        mLayoutManager = new GridLayoutManager(getActivity(), 2);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_moviepost);
        mRecyclerView.setAdapter(mAdaptor);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (savedInstanceState != null) {
            movies = Parcels.unwrap(savedInstanceState.getParcelable("movies"));
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("layout"));
        }
        return rootView;
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

        if (movies == null) {
            movies = new ArrayList<>();
        }

        String sort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
        TmdbApiInterface apiInterface = TmdbSingleton.getRestAdapter(getActivity()).create(TmdbApiInterface.class);
        apiInterface.getMovieResults(sort + ".desc", getString(R.string.api_movie_api), new Callback<MovieResults>() {

            @Override
            public void success(MovieResults movieResults, Response response) {
                movies = movieResults.results;
                mAdaptor.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, "Could not get json: " + error.getLocalizedMessage());
            }
        });
    }

    /**
     * Adaptor in order to feed the gridview movie posters, uses volley in order to provide
     * dynamically cached images for use in the view
     */

    private class MoviePosterAdaptor extends RecyclerView.Adapter<MoviePosterAdaptor.ViewHolder> {

        private Context mContext;

        public MoviePosterAdaptor(Context c) {
            mContext = c;
        }

        @Override
        public int getItemCount() {
            return movies.size();
        }

        @Override
        public MoviePosterAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movieposter, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MoviePosterAdaptor.ViewHolder holder, int position) {
            String imageUrl = getString(R.string.api_poster_base_path) + movies.get(position).posterPath;
            Picasso.with(mContext).load(imageUrl).placeholder(R.drawable.noposter).error(R.drawable.noposter).into(holder.mImageView);
            holder.mTextView.setText(movies.get(position).originalTitle);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ImageView mImageView;
            private TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                mImageView = (ImageView) v.findViewById(R.id.grid_item_moviepost_imageview);
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
                movieIntent.putExtra("movie", Parcels.wrap(movies.get(getAdapterPosition())));
                startActivity(movieIntent);
            }
        }
    }
}
