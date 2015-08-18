/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.act;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.adap.PosterPagerAdaptor;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.TVEntry;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * The main activity of the application, which holds the PosterFragment, which contains the grid of
 * thumbnails of movie posters which can be clicked to get more detailed movie information.
 */

public class MainActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener,
        NavigationView.OnNavigationItemSelectedListener {

    private final static String BUNDLE_POSTER_ADAPTOR = "poster_adaptor";

    private final static String[] MOVIE_SOURCE_TABS = {
            MovieEntry.SOURCE_POPULAR,
            MovieEntry.SOURCE_RATING,
            MovieEntry.SOURCE_NOW_PLAYING,
            MovieEntry.SOURCE_UPCOMING,
            MovieEntry.SOURCE_FAVORITE,
            MovieEntry.SOURCE_SEARCH
    };
    private final static int[] MOVIE_SOURCE_TAB_ICONS = {
            R.drawable.ic_share_white_24dp,
            R.drawable.ic_poll_white_24dp,
            R.drawable.ic_theaters_white_24dp,
            R.drawable.ic_schedule_white_24dp,
            R.drawable.ic_favorite_white_24dp,
            R.drawable.ic_search_white_24dp
    };
    private final static String[] TV_SOURCE_TABS = {
            TVEntry.SOURCE_POPULAR,
            TVEntry.SOURCE_RATING,
            TVEntry.SOURCE_ON_THE_AIR,
            TVEntry.SOURCE_AIRING_TODAY,
            TVEntry.SOURCE_FAVORITE,
            TVEntry.SOURCE_SEARCH
    };
    private final static int[] TV_SOURCE_NAMES = {
            R.string.tv_source_popular,
            R.string.tv_source_top_rated,
            R.string.tv_source_on_the_air,
            R.string.tv_source_airing_today,
            R.string.tv_source_favorite,
            R.string.tv_source_search
    };
    private final static int[] TV_SOURCE_TAB_ICONS = {
            R.drawable.ic_share_white_24dp,
            R.drawable.ic_poll_white_24dp,
            R.drawable.ic_tv_white_24dp,
            R.drawable.ic_live_tv_white_24dp,
            R.drawable.ic_favorite_white_24dp,
            R.drawable.ic_search_white_24dp
    };
    private final int[] MOVIE_SOURCE_NAMES = {
            R.string.movie_source_popular,
            R.string.movie_source_top_rated,
            R.string.movie_source_now_playing,
            R.string.movie_source_upcoming,
            R.string.movie_source_favorite,
            R.string.movie_source_search
    };
    @Bind(R.id.main_nav_view)
    NavigationView mNavigationView;
    @Bind(R.id.main_drawer)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.poster_pager)
    ViewPager mPosterPager;
    @Bind(R.id.source_tabs)
    TabLayout mTabLayout;
    @Bind(R.id.main_toolbar)
    Toolbar mToolbar;

    private SharedPreferences mPref;
    private PosterPagerAdaptor mAdaptor;
    private String mMediaSource;

    private int[] sourceNames;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_POSTER_ADAPTOR, mAdaptor.saveState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        mAdaptor = new PosterPagerAdaptor(getSupportFragmentManager());
        mPosterPager.setAdapter(mAdaptor);
        mPosterPager.addOnPageChangeListener(this);

        mNavigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            mAdaptor.restoreState(savedInstanceState.getBundle(BUNDLE_POSTER_ADAPTOR), getClassLoader());
        }
        setSource(mPref.getString(getString(R.string.pref_source_key), MovieContract.PATH_MOVIE));
    }


    private void setSource(String type) {
        switch (type) {
            case MovieContract.PATH_MOVIE: {
                setPages(type, MOVIE_SOURCE_TABS, MOVIE_SOURCE_TAB_ICONS, MOVIE_SOURCE_NAMES);
                mNavigationView.getMenu().findItem(R.id.nav_media_movie).setChecked(true);
                break;
            }
            case MovieContract.PATH_TV: {
                setPages(type, TV_SOURCE_TABS, TV_SOURCE_TAB_ICONS, TV_SOURCE_NAMES);
                mNavigationView.getMenu().findItem(R.id.nav_media_tv).setChecked(true);
                break;
            }
        }

    }

    private void setPages(String type, String[] sources, int[] icons, int[] titles) {
        if (mMediaSource == null || !mMediaSource.equals(type)) {
            if (mAdaptor != null) {
                mAdaptor.clear();
                for (String source : sources) {
                    mAdaptor.addFragment(source, type);
                }
                mAdaptor.notifyDataSetChanged();
            }

            if (mTabLayout != null && mPosterPager != null) {
                mTabLayout.setupWithViewPager(mPosterPager);

                int length = icons.length;
                for (int i = 0; i < length; i++) {
                    TabLayout.Tab tab = mTabLayout.getTabAt(i);
                    if (tab != null) tab.setIcon(icons[i]);
                }
            }

            sourceNames = titles;
            mMediaSource = type;
            mPref.edit().putString(getString(R.string.pref_source_key), type).apply();
        }
    }



    /**
     * Overrided in order to provide a working Drawer Toggle
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }


    @Override
    public void onPageSelected(int position) {
        setTitle(sourceNames[position]);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_media_movie: {
                setSource(MovieContract.PATH_MOVIE);
                break;
            }
            case R.id.nav_media_tv: {
                setSource(MovieContract.PATH_TV);
                break;
            }
            case R.id.nav_settings: {
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
