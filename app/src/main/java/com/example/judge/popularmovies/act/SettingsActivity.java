/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.act;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.judge.popularmovies.frag.SettingsFragment;


/**
 * Activity to hold the settings fragment, which contains the settings for the application.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

}
