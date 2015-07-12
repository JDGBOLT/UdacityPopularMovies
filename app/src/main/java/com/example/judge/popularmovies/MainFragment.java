/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies;

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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Main fragment of the Popular Movies app, provides a grid view of movie poster thumbnails that can
 * then be touched on in order to provide more detailed movie information in a separate view.
 */

public class MainFragment extends Fragment {

    private final String LOG_TAG = MainFragment.class.getSimpleName();
    private MoviePosterAdaptor mAdaptor;
    private ArrayList<Movie> movies;
    private RequestQueue mQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        movies = new ArrayList<>();
        mAdaptor = new MoviePosterAdaptor(getActivity());
        mQueue = VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue();
        loadMovieData();
        GridView gridView = (GridView) getActivity().findViewById(R.id.gridview_moviepost);
        gridView.setAdapter(mAdaptor);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent movieIntent = new Intent(getActivity(), MovieActivity.class);
                movieIntent.putExtra(getString(R.string.api_movie_original_title),
                        movies.get(position).originalTitle);
                movieIntent.putExtra(getString(R.string.api_movie_poster_path),
                        movies.get(position).posterPath);
                movieIntent.putExtra(getString(R.string.api_movie_release_date),
                        movies.get(position).releaseDate);
                movieIntent.putExtra(getString(R.string.api_movie_rating),
                        movies.get(position).rating);
                movieIntent.putExtra(getString(R.string.api_movie_overview),
                        movies.get(position).overView);
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
        if (movies.size() == 0) {
            String sort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                    getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
            String url = getString(R.string.api_json_base_path) + sort + ".desc";
            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // Parse the results array from the json, using the movie data contained within to populate movies
                        JSONArray movieData = response.getJSONArray(getString(R.string.api_movie_array));
                        if (movieData.length() > 0) {
                            movies.clear();
                            for (int i = 0; i < movieData.length(); i++) {
                                movies.add(new Movie(movieData.getJSONObject(i)));
                            }
                            mAdaptor.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error parsing JSON object: " + e.getLocalizedMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(LOG_TAG, "Error retrieving json data from theMovieDB: " + error.getLocalizedMessage());
                }
            });
            mQueue.add(request);
        }
    }

    /**
     * A helper class which contains all the movie information that was pulled from the json data,
     * put it into a class that contains just the values in order to not have to handle json parsing all
     * the time for all the data, and also have static checking of all the calls to the data.
     */

    public class Movie {
        public final String originalTitle, overView, releaseDate, posterPath, title;
        public final double rating;

        public Movie(JSONObject movie) throws JSONException {
            originalTitle = movie.getString(getString(R.string.api_movie_original_title));
            overView = movie.getString(getString(R.string.api_movie_overview));
            releaseDate = movie.getString(getString(R.string.api_movie_release_date));
            posterPath = movie.getString(getString(R.string.api_movie_poster_path));
            title = movie.getString(getString(R.string.api_movie_title));
            rating = movie.getDouble(getString(R.string.api_movie_rating));
        }
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
         * Actually creates the NetworkImageView, which is a modification of the ImageView class in order
         * to provide a dynamically cached on-demand image view.
         * @param position The position within the grid to grab which movie to pull poster for
         * @param convertView Contains an already existing view, if present
         * @param parent The parent view that will contain the view
         * @return Returns the NetworkImageView
         */

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            NetworkImageView imageView;

            // Do we need to create the view first? If so, create it using the xml, and set default posters and sizing tags
            if (convertView == null) {
                imageView = (NetworkImageView) mInflater.inflate(R.layout.grid_item_movieposter, null);
                imageView.setDefaultImageResId(R.drawable.noposter);
                imageView.setErrorImageResId(R.drawable.noposter);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (NetworkImageView) convertView;
            }
            if (!movies.get(position).posterPath.equals("null")) {
                // If we have an actual poster to have it fetch, fetch it, otherwise it will use the default poster image
                String imageUrl = getString(R.string.api_poster_base_path) + movies.get(position).posterPath;
                imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(mContext).getImageLoader());
            }
            return imageView;
        }
    }
}
