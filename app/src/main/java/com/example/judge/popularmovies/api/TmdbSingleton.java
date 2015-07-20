package com.example.judge.popularmovies.api;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class TmdbSingleton {
    private static final int cacheSize = 10 * 1024 * 1024;
    private static RestAdapter restAdapter;

    public static RestAdapter getRestAdapter(Context context) {
        if (restAdapter == null) {
            OkHttpClient client = new OkHttpClient();
            try {
                Cache cache = new Cache(new File(context.getCacheDir(), "json"), cacheSize);
                client.setCache(cache);
            } catch (IOException e) {
                e.printStackTrace();
            }
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://api.themoviedb.org/3")
                    .setClient(new OkClient(client))
                    .build();
        }
        return restAdapter;
    }

}
