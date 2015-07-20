package com.example.judge.popularmovies.model;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;

import java.util.ArrayList;

public class VideoResults {
    @Expose
    public ArrayList<Video> results;

    @Parcel
    public static class Video {

        @Expose
        public String key;

        @Expose
        public String name;

        @Expose
        public String site;

        @Expose
        public String type;

    }
}
