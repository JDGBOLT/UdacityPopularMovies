/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;


/**
 * Fragment for the Detailed Movie information window, contains various movie information, including
 * movie poster, year of release, synopsis, average rating, and movie title.
 */

public class MovieFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_movie, container, false);
        Intent intent = getActivity().getIntent();

        setPoster(layout, intent);
        setViewText(layout, intent);

        return layout;
    }

    /**
     * Function to set the movie poster image of the layout
     *
     * @param layout Layout to modify
     * @param intent Intent to get poster url from an extra string sent with the intent
     */
    private void setPoster(View layout, Intent intent) {

        NetworkImageView image = (NetworkImageView) layout.findViewById(R.id.movie_poster);
        image.setErrorImageResId(R.drawable.noposter);
        image.setDefaultImageResId(R.drawable.noposter);
        image.setImageUrl(getString(R.string.api_poster_base_path) +
                        intent.getStringExtra(getString(R.string.api_movie_poster_path)),
                VolleySingleton.getInstance(getActivity()).getImageLoader());
    }

    /**
     * Function to set the textual information contained within the Movie detail view
     *
     * @param layout Layout to modify
     * @param intent Intent in order to get the movie data sent with the intent
     */
    private void setViewText(View layout, Intent intent) {

        ((TextView) layout.findViewById(R.id.movie_title)).setText(intent.getStringExtra(
                getString(R.string.api_movie_original_title)));
        ((TextView) layout.findViewById(R.id.movie_year)).setText("Release Date: " + intent.getStringExtra(
                getString(R.string.api_movie_release_date)));
        ((TextView) layout.findViewById(R.id.movie_synopsis)).setText("Overview: " + intent.getStringExtra(
                getString(R.string.api_movie_overview)));
        ((TextView) layout.findViewById(R.id.movie_rating)).setText("Rating: " + intent.getDoubleExtra(
                getString(R.string.api_movie_rating), 0.0) + "/10.0");
    }
}
