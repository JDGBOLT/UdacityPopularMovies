package com.example.judge.popularmovies.adap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.judge.popularmovies.frag.PosterFragment;

import java.util.ArrayList;

public class PosterPagerAdaptor extends FragmentStatePagerAdapter {
    private final ArrayList<String> sources;
    private final ArrayList<String> types;

    public PosterPagerAdaptor(FragmentManager fm) {
        super(fm);
        sources = new ArrayList<>();
        types = new ArrayList<>();
    }

    public void addFragment(String source, String type) {
        sources.add(source);
        types.add(type);
    }

    public void clear() {
        sources.clear();
        types.clear();
    }

    @Override
    public Fragment getItem(int position) {
        return PosterFragment.newInstance(sources.get(position), types.get(position));
    }

    @Override
    public int getCount() {
        return sources.size();
    }
}
