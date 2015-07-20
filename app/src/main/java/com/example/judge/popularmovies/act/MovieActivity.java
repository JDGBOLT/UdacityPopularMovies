/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.act;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.judge.popularmovies.R;

/**
 * Contains the activity which holds the fragment for shwoing the detailed movie information.
 */

public class MovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
    }
}
