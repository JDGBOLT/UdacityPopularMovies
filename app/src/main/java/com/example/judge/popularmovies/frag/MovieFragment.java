/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.frag;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.api.TmdbApiInterface;
import com.example.judge.popularmovies.api.TmdbSingleton;
import com.example.judge.popularmovies.model.MovieResults;
import com.example.judge.popularmovies.model.ReviewResults;
import com.example.judge.popularmovies.model.VideoResults;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Fragment for the Detailed Movie information window, contains various movie information, including
 * movie poster, year of release, synopsis, average rating, and movie title.
 */

public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private LinearLayout mReviews, mTrailers;
    private LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_movie, container, false);
        mInflater = inflater;
        Intent intent = getActivity().getIntent();
        MovieResults.Movie movie = Parcels.unwrap(intent.getParcelableExtra("movie"));

        addTrailers(layout, movie);
        addReviews(layout, movie);
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

    private void addTrailers(View layout, MovieResults.Movie movie) {

        mTrailers = (LinearLayout) layout.findViewById(R.id.movie_trailers);
        TmdbApiInterface apiInterface = TmdbSingleton.getRestAdapter(getActivity()).create(TmdbApiInterface.class);
        apiInterface.getVideos(movie.id, getString(R.string.api_movie_api), new Callback<VideoResults>() {
            @Override
            public void success(VideoResults videoResults, Response response) {
                fillTrailers(videoResults);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG, "Could not get trailers: " + error.getLocalizedMessage());
            }

        });
    }

    private void fillTrailers(VideoResults videoResults) {
        int size = videoResults.results.size();
        if (size > 0) {
            ((TextView) mTrailers.findViewById(R.id.trailer_title)).setText("Trailers:");
            mTrailers.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING);
            for (int i = 0; i < size; i++) {
                CardView item = (CardView) mInflater.inflate(R.layout.list_item_trailer, mTrailers, false);
                ((TextView) item.findViewById(R.id.trailer_title)).setText(
                        videoResults.results.get(i).name);

                String url;
                switch (videoResults.results.get(i).site) {
                    case "YouTube": {
                        url = "http://www.youtube.com/watch?v=" + videoResults.results.get(i).key;
                        break;
                    }
                    default:
                        url = videoResults.results.get(i).key;
                }
                final Uri uri = Uri.parse(url);

                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });

                mTrailers.addView(item);
            }
        }
    }

    private void addReviews(View layout, MovieResults.Movie movie) {

        mReviews = (LinearLayout) layout.findViewById(R.id.movie_reviews);
        TmdbApiInterface apiInterface = TmdbSingleton.getRestAdapter(getActivity()).create(TmdbApiInterface.class);
        apiInterface.getReviews(movie.id, getString(R.string.api_movie_api), new Callback<ReviewResults>() {
            @Override
            public void success(ReviewResults reviewResults, Response response) {
                fillReviews(reviewResults);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG, "Could not get reviews: " + error.getLocalizedMessage());
            }
        });
    }

    private void fillReviews(ReviewResults reviewResults) {
        int size = reviewResults.results.size();
        if (size > 0) {
            ((TextView) mReviews.findViewById(R.id.review_title)).setText("Reviews:");
            mReviews.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING);
            for (int i = 0; i < size; i++) {
                CardView item = (CardView) mInflater.inflate(R.layout.list_item_review, mReviews, false);
                ((TextView) item.findViewById(R.id.author_textview)).setText(
                        reviewResults.results.get(i).author);
                ((TextView) item.findViewById(R.id.review_textview)).setText(
                        reviewResults.results.get(i).content);
                mReviews.addView(item);
            }
        }
    }
}
