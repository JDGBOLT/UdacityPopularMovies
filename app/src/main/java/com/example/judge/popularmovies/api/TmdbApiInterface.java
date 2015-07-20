package com.example.judge.popularmovies.api;

import com.example.judge.popularmovies.model.MovieResults;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface TmdbApiInterface {
    @GET("/discover/movie")
    void getMovieResults(@Query("sort_by") String sort, @Query("api_key") String key, Callback<MovieResults> callback);
}
