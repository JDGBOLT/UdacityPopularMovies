package com.example.judge.popularmovies.api;

import com.example.judge.popularmovies.model.MovieResults;
import com.example.judge.popularmovies.model.ReviewResults;
import com.example.judge.popularmovies.model.VideoResults;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TmdbApiInterface {
    @GET("/discover/movie")
    void getMovieResults(@Query("sort_by") String sort, @Query("api_key") String key, Callback<MovieResults> callback);

    @GET("/movie/{id}/videos")
    void getVideos(@Path("id") int id, @Query("api_key") String key, Callback<VideoResults> callback);

    @GET("/movie/{id}/reviews")
    void getReviews(@Path("id") int id, @Query("api_key") String key, Callback<ReviewResults> callback);
}
