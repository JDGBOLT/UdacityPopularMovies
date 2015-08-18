package com.example.judge.popularmovies.adap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.act.MovieActivity;
import com.example.judge.popularmovies.api.VolleySingleton;
import com.example.judge.popularmovies.data.MovieContract;

/**
 * Adaptor in order to feed the gridview movie posters, uses volley in order to provide
 * dynamically cached images for use in the view
 */

public class PosterAdaptor extends RecyclerView.Adapter<PosterAdaptor.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private int mColumnPosterPath;
    private int mColumnTitle;
    private int mColumnMediaId;

    public PosterAdaptor(Context c) {
        mContext = c;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        if (cursor != null) {
            mColumnPosterPath = mCursor.getColumnIndex(MovieContract.MovieEntry.POSTER_PATH.COLUMN);
            mColumnTitle = mCursor.getColumnIndex(MovieContract.MovieEntry.ORIGINAL_TITLE.COLUMN);
            mColumnMediaId = mCursor.getColumnIndex(MovieContract.MovieEntry.MEDIA_ID.COLUMN);
        }
        notifyDataSetChanged();
    }

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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String posterPath = mCursor.getString(mColumnPosterPath);
        if (!posterPath.equals("null")) {
            String imageUrl = mContext.getString(R.string.api_poster_base_path) + posterPath;
            holder.mImageView.setImageUrl(imageUrl, VolleySingleton.getInstance(mContext).getImageLoader());
        }
        holder.mImageView.setDefaultImageResId(R.drawable.noposter);
        holder.mImageView.setErrorImageResId(R.drawable.noposter);
        holder.mTextView.setText(mCursor.getString(mColumnTitle));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent movieIntent = new Intent(mContext, MovieActivity.class);
            mCursor.moveToPosition(getAdapterPosition());
            movieIntent.putExtra(Intent.EXTRA_SUBJECT, mCursor.getInt(mColumnMediaId));

            mContext.startActivity(movieIntent);
        }
    }
}
