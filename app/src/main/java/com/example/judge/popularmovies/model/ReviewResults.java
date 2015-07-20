package com.example.judge.popularmovies.model;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;

import java.util.ArrayList;

public class ReviewResults {
    @Expose
    ArrayList<Review> results;

    @Parcel
    public static class Review {
        @Expose
        public String author;

        @Expose
        public String content;

        @Expose
        public String url;
    }
}
