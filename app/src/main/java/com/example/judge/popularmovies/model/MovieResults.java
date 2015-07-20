package com.example.judge.popularmovies.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

public class MovieResults {
    @Expose
    public ArrayList<Movie> results;

    @Parcel
    public static class Movie {
        @Expose
        public Integer id;
        @SerializedName("original_title")
        @Expose
        public String originalTitle;
        @Expose
        public String overview;
        @SerializedName("release_date")
        @Expose
        public String releaseDate;
        @SerializedName("poster_path")
        @Expose
        public String posterPath;
        @Expose
        public String title;
        @SerializedName("vote_average")
        @Expose
        public Double rating;

    }
}
