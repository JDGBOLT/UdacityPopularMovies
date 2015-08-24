/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class manages the creation and management of the databases required for the application,
 * creates a database and also uses the information in the MovieContract for setting up the columns
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "media.db";
    private static final int DATABASE_VERSION = 4;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Helper method in order to create a database with all the columns specified
    private String initTable(String tableName, String id, MovieContract.DatabaseColumn[] columns) {
        String sql = "create table " + tableName + " (" + id + " integer primary key";
        for (MovieContract.DatabaseColumn column : columns) {
            sql = sql + ", " + column.COLUMN + " " + column.TYPE + " " + column.ARGS;
        }
        sql = sql + ");";
        return sql;
    }

    // Creates the 4 different databases required for the application, passing in the columns from the contract for creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(initTable(MovieContract.MovieEntry.TABLE_NAME, MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMNS));
        db.execSQL(initTable(MovieContract.ReviewEntry.TABLE_NAME, MovieContract.ReviewEntry._ID, MovieContract.ReviewEntry.COLUMNS));
        db.execSQL(initTable(MovieContract.TrailerEntry.TABLE_NAME, MovieContract.TrailerEntry._ID, MovieContract.TrailerEntry.COLUMNS));
        db.execSQL(initTable(MovieContract.TVEntry.TABLE_NAME, MovieContract.TVEntry._ID, MovieContract.TVEntry.COLUMNS));
    }

    /**
     * For upgrading of the database, currently just clears out the old database and puts the new information in
     * the database, mostly to ensure the application doesn't crash when adding new columns and such.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TVEntry.TABLE_NAME);
        onCreate(db);
    }
}
