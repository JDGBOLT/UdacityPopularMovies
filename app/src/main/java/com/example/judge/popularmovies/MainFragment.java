package com.example.judge.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private final String LOG_TAG = MainFragment.class.getSimpleName();
    public MoviePosterAdaptor mAdaptor;
    private ArrayList<Movie> movies;
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

    private void loadMovieData() {
        if (movies.size() == 0) {
            String sort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                    getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
            String url = getString(R.string.api_json_base_path) + sort + ".desc";
            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray movieData = response.getJSONArray(getString(R.string.api_movie_array));
                        if (movieData.length() > 0) {
                            movies.clear();
                            for (int i = 0; i < movieData.length(); i++) {
                                movies.add(new Movie(movieData.getJSONObject(i)));
                            }
                            mAdaptor.notifyDataSetChanged();
                        }
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
                imageView.setDefaultImageResId(R.drawable.noposter);
                imageView.setErrorImageResId(R.drawable.noposter);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (NetworkImageView) convertView;
            }
            if (!movies.get(position).posterPath.equals("null")) {
                String imageUrl = getString(R.string.api_poster_base_path) + movies.get(position).posterPath;
                imageView.setImageUrl(imageUrl, VolleySingleton.getInstance(mContext).getImageLoader());
            }
            return imageView;
        }
    }
}
