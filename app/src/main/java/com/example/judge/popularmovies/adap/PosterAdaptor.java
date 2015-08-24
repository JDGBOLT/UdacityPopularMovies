/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.adap;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.act.MainActivity;
import com.example.judge.popularmovies.api.VolleySingleton;
import com.example.judge.popularmovies.frag.PosterFragment;

/**
 * Adaptor in order to feed the gridview movie posters, uses volley in order to provide
 * dynamically cached images for use in the view
 */

public class PosterAdaptor extends RecyclerView.Adapter<PosterAdaptor.ViewHolder> {

    private final MainActivity mContext;
    private Cursor mCursor;
    private String mType;

    public PosterAdaptor(MainActivity c) {
        mContext = c;
    }

    // Swaps the cursor, closing a cursor if it isn't null just to save some memory.
    public void swapCursor(Cursor cursor, final String type) {
        if (mCursor != null && cursor == null) mCursor.close();
        mCursor = cursor;
        mType = type;
        notifyDataSetChanged();
    }

    // Return how many posters there are, but only if we actually have that data to give.
    @Override
    public int getItemCount() {
        if (mCursor != null) return mCursor.getCount();
        else return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movieposter, parent, false);
        return new ViewHolder(v);
    }

    // Bind the view holder, setting the URL for the Poster ImageView and the title.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String posterPath = mCursor.getString(PosterFragment.COLUMN_POSTER_PATH);
        if (!posterPath.equals("null")) {
            String imageUrl = mContext.getString(R.string.api_poster_base_path) + posterPath;
            holder.mImageView.setImageUrl(imageUrl, VolleySingleton.getInstance(mContext).getImageLoader());
        }
        holder.mImageView.setDefaultImageResId(R.drawable.noposter);
        holder.mImageView.setErrorImageResId(R.drawable.noposter);
        holder.mTextView.setText(mCursor.getString(PosterFragment.COLUMN_TITLE));

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ViewHolder for the Poster Recycler View
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final NetworkImageView mImageView;
        private final TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mImageView = (NetworkImageView) v.findViewById(R.id.grid_item_moviepost_imageview);
            mTextView = (TextView) v.findViewById(R.id.grid_item_moviepost_textview);
        }

        /**
         * When one of the posters is clicked, we notify the main activity to open the detail fragment
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            mContext.openDetail(mCursor.getInt(PosterFragment.COLUMN_MEDIA_ID), mType);
        }
    }
}
