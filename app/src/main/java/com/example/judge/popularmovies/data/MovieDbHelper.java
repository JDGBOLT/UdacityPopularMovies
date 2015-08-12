package com.example.judge.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private String initTable(String tableName, String id, MovieContract.DatabaseColumn[] columns) {
        String sql = "create table " + tableName + " (" + id + " integer primary key";
        for (MovieContract.DatabaseColumn column : columns) {
            sql = sql + ", " + column.COLUMN + " " + column.TYPE + " " + column.ARGS;
        }
        sql = sql + ");";
        return sql;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(initTable(MovieContract.MovieEntry.TABLE_NAME, MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMNS));
        db.execSQL(initTable(MovieContract.ReviewEntry.TABLE_NAME, MovieContract.ReviewEntry._ID, MovieContract.ReviewEntry.COLUMNS));
        db.execSQL(initTable(MovieContract.TrailerEntry.TABLE_NAME, MovieContract.TrailerEntry._ID, MovieContract.TrailerEntry.COLUMNS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        onCreate(db);
    }
}
