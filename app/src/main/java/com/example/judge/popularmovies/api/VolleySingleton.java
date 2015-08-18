package com.example.judge.popularmovies.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

/**
 * Custom implementation of Volley Request Queue, serves to contain volley data for the lifetime of
 * the application.
 * Sourced from http://www.truiton.com/2015/03/android-volley-imageloader-networkimageview-example/
 */

public class VolleySingleton {

    private static final int sCacheSize = 10 * 1024 * 1024;
    private static final int sLruCacheSize = 20;
    private static VolleySingleton sInstance;
    private static Context sCtx;
    private final ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;


    private VolleySingleton(Context context) {
        sCtx = context;
        mRequestQueue = getRequestQueue();

        // Includes an LruCache, for storing image data downloaded in memory
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<>(sLruCacheSize);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    /**
     * Returns the instance of the singleton *
     */
    public static synchronized VolleySingleton getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VolleySingleton(context);
        }
        return sInstance;
    }

    /**
     * Returns the current request queue, or creates a new one, which will be backed up by both
     * a memory and disk cache, before finally reaching out to the internet in order to download the data.
     *
     * @return Returns the existing request queue
     */

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Cache cache = new DiskBasedCache(sCtx.getCacheDir(), sCacheSize);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache, network);
            // Don't forget to start the volley request queue
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}