/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.adap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.judge.popularmovies.frag.PosterFragment;

import java.util.ArrayList;

/**
 * This is a simple adaptor for the ViewPager of the main view, which has tabs for each source type
 */
public class PosterPagerAdaptor extends FragmentStatePagerAdapter {
    private final ArrayList<String> sources;
    private final ArrayList<String> types;

    public PosterPagerAdaptor(FragmentManager fm) {
        super(fm);
        sources = new ArrayList<>();
        types = new ArrayList<>();
    }

    // Adds a fragment to the pager
    public void addFragment(String source, String type) {
        sources.add(source);
        types.add(type);
    }

    // Clear the pager
    public void clear() {
        sources.clear();
        types.clear();
    }

    // Returns a new PosterFragment with the given source and type
    @Override
    public Fragment getItem(int position) {
        return PosterFragment.newInstance(sources.get(position), types.get(position));
    }

    @Override
    public int getCount() {
        return sources.size();
    }
}
