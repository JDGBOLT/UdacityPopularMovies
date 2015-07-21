/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
    private MoviePosterAdaptor mAdaptor;
    private ArrayList<MovieResults.Movie> movies;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("movies", Parcels.wrap(movies));
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            movies = Parcels.unwrap(savedInstanceState.getParcelable("movies"));
        }
        return inflater.inflate(R.layout.fragment_main, container, false);
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
        if (movies == null) {
            movies = new ArrayList<>();
        }
        mAdaptor = new MoviePosterAdaptor(getActivity());
        loadMovieData();
        GridView gridView = (GridView) getActivity().findViewById(R.id.gridview_moviepost);
        gridView.setAdapter(mAdaptor);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent movieIntent = new Intent(getActivity(), MovieActivity.class);
                movieIntent.putExtra("movie", Parcels.wrap(movies.get(position)));
                startActivity(movieIntent);
            }
        });
    }

    /**
     * This function actually has volley pull the data from theMovieDB, has it, once it retrieves the json
     * data, to parse it into a JSONObject, we then pull the array of movie listings out of that object
     * and use it to populate the arraylist of Movie objects.
     */

    private void loadMovieData() {
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

    private class MoviePosterAdaptor extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater = null;

        public MoviePosterAdaptor(Context c) {
            mContext = c;
            mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return movies.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Actually creates the ImageView, which is filled with picasso the image to be used
         * to provide a dynamically cached on-demand image view.
         *
         * @param position    The position within the grid to grab which movie to pull poster for
         * @param convertView Contains an already existing view, if present
         * @param parent      The parent view that will contain the view
         * @return Returns the NetworkImageView
         */

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;

            // Do we need to create the view first? If so, create it using the xml, and set default posters and sizing tags
            if (convertView == null) {
                imageView = (ImageView) mInflater.inflate(R.layout.grid_item_movieposter, null);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }
            String imageUrl = getString(R.string.api_poster_base_path) + movies.get(position).posterPath;

            Picasso.with(mContext).load(imageUrl).placeholder(R.drawable.noposter).error(R.drawable.noposter).into(imageView);
            return imageView;
        }
    }
}
