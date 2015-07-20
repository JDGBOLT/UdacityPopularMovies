/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.model.MovieResults;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;


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
        MovieResults.Movie movie = Parcels.unwrap(intent.getParcelableExtra("movie"));

        setPoster(layout, movie);
        setViewText(layout, movie);

        return layout;
    }

    /**
     * Function to set the movie poster image of the layout
     *
     * @param layout Layout to modify
     * @param movie  Movie object in order to get the poster path
     */
    private void setPoster(View layout, MovieResults.Movie movie) {

        ImageView image = (ImageView) layout.findViewById(R.id.movie_poster);
        Picasso.with(getActivity()).load(getString(R.string.api_poster_base_path) + movie.posterPath)
                .placeholder(R.drawable.noposter)
                .error(R.drawable.noposter)
                .into(image);
    }

    /**
     * Function to set the textual information contained within the Movie detail view
     *
     * @param layout Layout to modify
     * @param movie  Movie data in order to fill in the text fields
     */
    private void setViewText(View layout, MovieResults.Movie movie) {

        ((TextView) layout.findViewById(R.id.movie_title)).setText(movie.originalTitle);
        ((TextView) layout.findViewById(R.id.movie_year)).setText("Release Date: " + movie.releaseDate);
        ((TextView) layout.findViewById(R.id.movie_synopsis)).setText("Overview: " + movie.overview);
        ((TextView) layout.findViewById(R.id.movie_rating)).setText("Rating: " + movie.rating + "/10.0");
    }
}
