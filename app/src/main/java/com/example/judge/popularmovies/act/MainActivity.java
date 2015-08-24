/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.act;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.adap.PosterPagerAdaptor;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.data.MovieContract.MovieEntry;
import com.example.judge.popularmovies.data.MovieContract.TVEntry;
import com.example.judge.popularmovies.frag.DetailFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * This is the main activity, it has a fair amount of functionality contained within it, as it uses
 * drawers for the navigation panel, and if on a phone, it opens the movie detail fragment within
 * it's own drawer. It was felt that being able to quickly swipe the detail pane away and get back to
 * the view list was a very quick operation that was quite user friendly. Also the implementation
 * details are simplified as it is using the same code for both tablets and phones.
 */

public class MainActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener,
        NavigationView.OnNavigationItemSelectedListener {

    public final static String BUNDLE_DETAIL_TYPE = "detail_type";
    public final static String BUNDLE_DETAIL_MEDIAID = "detail_id";
    // These are all the tags used for the fragments and bundle arguments
    private final static String BUNDLE_POSTER_ADAPTOR = "poster_adaptor";
    private final static String DETAILFRAGMENT_TAG = "DFTAG";

    // The tab sources, tabs, and names for each media type are specified within these next arrays
    private final static String[] MOVIE_SOURCE_TABS = {
            MovieEntry.SOURCE_POPULAR,
            MovieEntry.SOURCE_RATING,
            MovieEntry.SOURCE_NOW_PLAYING,
            MovieEntry.SOURCE_UPCOMING,
            MovieEntry.SOURCE_FAVORITE,
    };
    private final static int[] MOVIE_SOURCE_TAB_ICONS = {
            R.drawable.ic_share_white_24dp,
            R.drawable.ic_poll_white_24dp,
            R.drawable.ic_theaters_white_24dp,
            R.drawable.ic_schedule_white_24dp,
            R.drawable.ic_favorite_white_24dp
    };
    private final static String[] TV_SOURCE_TABS = {
            TVEntry.SOURCE_POPULAR,
            TVEntry.SOURCE_RATING,
            TVEntry.SOURCE_ON_THE_AIR,
            TVEntry.SOURCE_AIRING_TODAY,
            TVEntry.SOURCE_FAVORITE,
    };
    private final static int[] TV_SOURCE_NAMES = {
            R.string.tv_source_popular,
            R.string.tv_source_top_rated,
            R.string.tv_source_on_the_air,
            R.string.tv_source_airing_today,
            R.string.tv_source_favorite,
    };
    private final static int[] TV_SOURCE_TAB_ICONS = {
            R.drawable.ic_share_white_24dp,
            R.drawable.ic_poll_white_24dp,
            R.drawable.ic_tv_white_24dp,
            R.drawable.ic_live_tv_white_24dp,
            R.drawable.ic_favorite_white_24dp,
    };
    private static final int[] MOVIE_SOURCE_NAMES = {
            R.string.movie_source_popular,
            R.string.movie_source_top_rated,
            R.string.movie_source_now_playing,
            R.string.movie_source_upcoming,
            R.string.movie_source_favorite,
    };

    // Global view declarations that are filled by butterknife
    @Bind(R.id.detail_container)
    FrameLayout mDetailDrawer;
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

    // A global preferences and also the adaptor and current media type being used
    private SharedPreferences mPref;
    private PosterPagerAdaptor mAdaptor;
    private String mMediaSource;

    // Simple boolean to check whether we are on a phone or not
    private boolean isPhone;

    // A variable used to point to the tab names for each source, which is used for changing the name on page switch
    private int[] sourceNames;

    // Save the state of the adaptor into the saved instance state bundle

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_POSTER_ADAPTOR, mAdaptor.saveState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup our content view and bind it using ButterKnife
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Setup our toolbar, including having a button used for opening the navigation panel
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup a global preferences and setup our ViewPager
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        mAdaptor = new PosterPagerAdaptor(getSupportFragmentManager());
        mPosterPager.setAdapter(mAdaptor);
        mPosterPager.addOnPageChangeListener(this);

        // Setup our navigation panel and setup the right panel correctly if we are on a phone
        mNavigationView.setNavigationItemSelectedListener(this);
        if (mDetailDrawer.getLayoutParams() instanceof DrawerLayout.LayoutParams) {
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDetailDrawer.getLayoutParams();
            params.width = getResources().getDisplayMetrics().widthPixels;
            mDetailDrawer.setLayoutParams(params);
            isPhone = true;
        }

        // Have our tabs fill the entire width of the view size
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Restore the state of our ViewPager
        if (savedInstanceState != null) {
            mAdaptor.restoreState(savedInstanceState.getBundle(BUNDLE_POSTER_ADAPTOR), getClassLoader());
        }

        // Set our source in order to fill the view pager and setup the poster fragments
        setSource(mPref.getString(getString(R.string.pref_source_key), MovieContract.PATH_MOVIE));
    }

    /**
     * Function to set our source, either TV or Movie, in order to populate our ViewPager with PosterFragments
     * We set the pages then set the item within the navigation panel to be checked. During the setup we
     * give the tab names, icons, and source names for the given source.
     *
     * @param type The media type to use
     */

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

    /**
     * Function in order to set the pages of our ViewPager with the correct tabs of the given source.
     *
     * @param type    Media Type ot use, either movie or tv
     * @param sources The sources for the given media type
     * @param icons   The icons to use for the tabs
     * @param titles  The titles to set in the action bar after switching to a tab.
     */

    private void setPages(String type, String[] sources, int[] icons, int[] titles) {

        // Check to make sure we are changing the media type
        if (mMediaSource == null || !mMediaSource.equals(type)) {
            // Clear our adaptor and add PosterFragments for the given media type
            if (mAdaptor != null) {
                mAdaptor.clear();
                for (String source : sources) {
                    mAdaptor.addFragment(source, type);
                }
                mAdaptor.notifyDataSetChanged();
            }

            // Setup our tab layout with the contents of the ViewPager
            if (mTabLayout != null && mPosterPager != null) {
                mTabLayout.setupWithViewPager(mPosterPager);

                // Set the icons for the tabs to the given ones
                int length = icons.length;
                for (int i = 0; i < length; i++) {
                    TabLayout.Tab tab = mTabLayout.getTabAt(i);
                    if (tab != null) tab.setIcon(icons[i]);
                }
            }

            // Set our titles to be used and set our type in the preferences
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
            // If we press the button on the action bar we open the navigation drawer
            case android.R.id.home: {
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * We override this method in order to be able to have the drawers close when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (isPhone && mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }


    // This function is called when the page is changed, so we can update the title of the actionbar
    @Override
    public void onPageSelected(int position) {
        setTitle(sourceNames[position]);
    }

    /**
     * This function is called when we press an item within the navigation panel, and can be used
     * to either switch sources or to access the settings panel.
     * @param menuItem The menu item pressed
     * @return Return whether we successfully operated on the menu item
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            // Switch our media type to movie
            case R.id.nav_media_movie: {
                setSource(MovieContract.PATH_MOVIE);
                break;
            }

            // Switch our media type to tv
            case R.id.nav_media_tv: {
                setSource(MovieContract.PATH_TV);
                break;
            }

            // Open the settings activity
            case R.id.nav_settings: {
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    /**
     * Function that is called from the PosterAdapter which opens the detail fragment for the given mediaId,
     * and if we are a phone open up the drawer to make the content visible.
     *
     * @param mediaId The media id to pass to the detail fragment
     * @param type    What media type the media id is
     */

    public void openDetail(int mediaId, String type) {

        // Create a new detail fragment and set it's arguments
        Fragment detailFragment = new DetailFragment();

        Bundle arguments = new Bundle();
        arguments.putString(BUNDLE_DETAIL_TYPE, type);
        arguments.putInt(BUNDLE_DETAIL_MEDIAID, mediaId);
        detailFragment.setArguments(arguments);

        // Replace the detail container with the created fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, detailFragment, DETAILFRAGMENT_TAG).commit();

        // if we are on a phone open the drawer to view the content
        if (isPhone) mDrawerLayout.openDrawer(GravityCompat.END);
    }

    // These next 2 functions are required for the ViewPager events, but we don't do anything in them.

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    /**
     * Workaround to try to fix a bug with version 23 of the Android Design support library which causes
     * a NPE from the Coordinator Layout
     * https://code.google.com/p/android/issues/detail?id=183166
     */
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }
}
