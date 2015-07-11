package com.example.judge.popularmovies;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    //   private JSONArray movies;
    public ArrayList<Movie> movies;
    public MoviePosterAdaptor mAdaptor;
    private RequestQueue mQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        movies = new ArrayList<>();
        mAdaptor = new MoviePosterAdaptor(getActivity());
        mQueue = VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue();
        loadMovieData();
        GridView gridView = (GridView) getActivity().findViewById(R.id.gridview_moviepost);
        gridView.setAdapter(mAdaptor);
        Log.v(LOG_TAG, "" + movies.size());
    }

    private void loadMovieData() {
        if (movies.size() == 0) {
            final String MOVIE_BASEURL = "http://api.themoviedb.org/3/discover/movie?api_key=apikey&sort_by=";
            String sort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                    getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
            String url = MOVIE_BASEURL + sort + ".desc";
            Log.v(LOG_TAG, "URL is " + url);
            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray movieData = response.getJSONArray("results");
                        for (int i = 0; i < movieData.length(); i++) {
                            movies.add(new Movie(movieData.getJSONObject(i)));
                            Log.v(LOG_TAG, movies.get(i).originalTitle);
                        }
                        mAdaptor.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            mQueue.add(request);
        }
    }

    public class Movie {
        public final String originalTitle, overView, releaseDate, posterPath, title;

        public Movie(JSONObject movie) throws JSONException {
            originalTitle = movie.getString("original_title");
            overView = movie.getString("overview");
            releaseDate = movie.getString("release_date");
            posterPath = movie.getString("poster_path");
            title = movie.getString("title");
        }
    }

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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            NetworkImageView imageView;
            if (convertView == null) {
                imageView = (NetworkImageView) mInflater.inflate(R.layout.grid_item_movieposter, null);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (NetworkImageView) convertView;
            }
            String imageUrl = "http://image.tmdb.org/t/p/w185" + movies.get(position).posterPath;
            imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(mContext).getImageLoader());
            return imageView;
        }
    }
}
