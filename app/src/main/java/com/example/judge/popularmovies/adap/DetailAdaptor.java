/*
 * Copyright (C) 2015 Joshua Gwinn (jdgbolt@gmail.com)
 */

package com.example.judge.popularmovies.adap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.judge.popularmovies.R;
import com.example.judge.popularmovies.api.VolleySingleton;
import com.example.judge.popularmovies.data.MovieContract;
import com.example.judge.popularmovies.frag.DetailFragment;

/**
 * This is the code that handles most of the detail view functionality. All of the data is contained
 * within a recycler view so most of the functionality is contained here. A recyclerview was chosen
 * in order to have a scrollable view with the review and trailer data, which is a variable amount.
 * The implementation is somewhat complicated, with 3 cursors feeding the view and some logic for
 * what stuff goes where. The main detail view is the first position, with trailers below that then
 * review data.
 */
public class DetailAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // The 3 view types that can be put into the recyclerview
    private static final int VIEW_TYPE_TRAILER = 0;
    private static final int VIEW_TYPE_REVIEW = 1;
    private static final int VIEW_TYPE_DETAIL = 2;

    // The number of static views at the top
    private static final int[] STATIC_VIEWS = {
            VIEW_TYPE_DETAIL,
    };
    // The media type it is, whether tv or movie
    private final String mType;
    // The context passed in
    private Context mContext;
    // The cursors used to populate the views and their counts
    private Cursor mReviewCursor;
    private Cursor mTrailerCursor;
    private Cursor mMediaCursor;

    private int mTrailerCount;
    private int mReviewCount;

    public DetailAdaptor(Context context, final String type) {
        super();
        mContext = context;
        mType = type;
    }

    // This is where we get the position data, fairly simple, getting first the static views, trailers, then reviews
    @Override
    public int getItemViewType(int position) {
        if (position < STATIC_VIEWS.length) return STATIC_VIEWS[position];
        if (position < STATIC_VIEWS.length + mTrailerCount) return VIEW_TYPE_TRAILER;
        if (position < STATIC_VIEWS.length + mReviewCount + mTrailerCount) return VIEW_TYPE_REVIEW;
        return super.getItemViewType(position);
    }


    // Create the viewholders for each type
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_DETAIL: {
                return new DetailHolder(inflater.inflate(R.layout.media_detail, parent, false));
            }
            case VIEW_TYPE_REVIEW: {
                return new ReviewHolder(inflater.inflate(R.layout.list_item_review, parent, false));
            }
            case VIEW_TYPE_TRAILER: {
                return new TrailerHolder(inflater.inflate(R.layout.list_item_trailer, parent, false));
            }
        }
        return null;
    }

    /**
     * This is a fairly complicated function which handles the actual populating of data into the
     * views.
     *
     * @param holder   The holder to populate
     * @param position The position within the recyclerview
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // First we get what view type it should be, then populating the view depending on which type
        switch (getItemViewType(position)) {

            // The main detail view
            case VIEW_TYPE_DETAIL: {

                // Check whether we have actual data to put into the view, and moving the cursor to the first element if we do
                if (mMediaCursor == null) return;
                String posterUrl, title, status, overview, runtime, release;
                double rating;
                int voteCount;
                mMediaCursor.moveToFirst();

                // Here we are populating the fields that are specific to each type, as the data for release date and runtime is different
                switch (mType) {
                    case MovieContract.PATH_MOVIE: {

                        // Set release to the release date and give runtime in minutes
                        release = mMediaCursor.getString(DetailFragment.COLUMN_RELEASE_DATE);
                        runtime = mMediaCursor.getInt(DetailFragment.COLUMN_RUNTIME) + " minutes";
                        break;
                    }
                    case MovieContract.PATH_TV: {

                        // Here we get the first and last air date, and get only the year from them, and set the release, using both only if they are different
                        String start = mMediaCursor.getString(DetailFragment.COLUMN_FIRST_AIR_DATE).split("-")[0];
                        String end = mMediaCursor.getString(DetailFragment.COLUMN_LAST_AIR_DATE).split("-")[0];
                        release = (start.equals(end)) ? start : start + "-" + end;

                        // Here we set the runtime in both seasons and the number of episodes
                        runtime = mMediaCursor.getString(DetailFragment.COLUMN_NUMBER_OF_SEASONS) + " season(s), "
                                + mMediaCursor.getString(DetailFragment.COLUMN_NUMBER_OF_EPISODES) + " episodes";
                        break;
                    }
                    default: {
                        release = "";
                        runtime = "";
                    }
                }


                // Set temporary variables to simplify the settext statements
                DetailHolder dh = (DetailHolder) holder;
                posterUrl = mMediaCursor.getString(DetailFragment.COLUMN_POSTER_PATH);
                title = mMediaCursor.getString(DetailFragment.COLUMN_ORIGINAL_TITLE);
                status = mMediaCursor.getString(DetailFragment.COLUMN_STATUS);
                overview = mMediaCursor.getString(DetailFragment.COLUMN_OVERVIEW);
                rating = mMediaCursor.getDouble(DetailFragment.COLUMN_RATING);
                voteCount = mMediaCursor.getInt(DetailFragment.COLUMN_VOTE_COUNT);

                // Sets all the text fields and the poster
                dh.setPoster(posterUrl);
                dh.mTitle.setText(title);
                dh.mStatus.setText(status + " (" + release + ")");
                dh.mRuntime.setText(runtime);
                dh.mRating.setText(String.format("%.1f/10 (%d votes)", rating, voteCount));
                dh.mOverview.setText(overview);

                break;
            }

            // Populate a review view
            case VIEW_TYPE_REVIEW: {

                // Check to make sure that our data isn't null, and set the position to the right offset
                if (mReviewCursor == null) return;
                int reviewPos = position - (STATIC_VIEWS.length + mReviewCount);
                if (reviewPos >= 0 && reviewPos < mReviewCount) {
                    mReviewCursor.moveToPosition(reviewPos);
                } else {
                    mReviewCursor.moveToFirst();
                }

                // Get our holder and set the text values with the author and the review content
                ReviewHolder rh = (ReviewHolder) holder;
                rh.mAuthor.setText(mReviewCursor.getString(DetailFragment.COLUMN_AUTHOR));
                rh.mReview.setText(mReviewCursor.getString(DetailFragment.COLUMN_REVIEW));
                break;
            }

            // Populate a trailer view
            case VIEW_TYPE_TRAILER: {

                // Check to make sure that our data isn't null, and set the position to the right offset
                if (mTrailerCursor == null) return;
                int trailerPos = position - (STATIC_VIEWS.length + mReviewCount);
                if (trailerPos >= 0 && trailerPos < mTrailerCount) {
                    mTrailerCursor.moveToPosition(trailerPos);
                } else {
                    mTrailerCursor.moveToFirst();
                }

                // Get our holder and set the required fields, including some internal variables that are used when the view is clicked
                TrailerHolder th = (TrailerHolder) holder;
                th.mTitle.setText(mTrailerCursor.getString(DetailFragment.COLUMN_NAME));
                th.mKey = mTrailerCursor.getString(DetailFragment.COLUMN_KEY);
                th.mSite = mTrailerCursor.getString(DetailFragment.COLUMN_SITE);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return STATIC_VIEWS.length + mTrailerCount + mReviewCount;
    }

    // These next 3 functions swap the cursor data into the right fields
    public void swapMediaCursor(Cursor cursor) {
        mMediaCursor = cursor;
        notifyDataSetChanged();
    }

    public void swapTrailerCursor(Cursor cursor) {
        mTrailerCursor = cursor;
        mTrailerCount = (mTrailerCursor != null) ? mTrailerCursor.getCount() : 0;
        notifyDataSetChanged();
    }

    public void swapReviewCursor(Cursor cursor) {
        mReviewCursor = cursor;
        mReviewCount = (mReviewCursor != null) ? mReviewCursor.getCount() : 0;
        notifyDataSetChanged();
    }

    // This is the view holder for the main view, ButterKnife didn't like populating these views so they are done manually
    public final class DetailHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final View mView;
        final NetworkImageView mPoster;
        final TextView mTitle;
        final TextView mStatus;
        final TextView mRuntime;
        final TextView mRating;
        final TextView mOverview;
        final TextView mShare;

        public DetailHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPoster = (NetworkImageView) mView.findViewById(R.id.media_detail_poster);
            mTitle = (TextView) mView.findViewById(R.id.media_detail_title);
            mStatus = (TextView) mView.findViewById(R.id.media_detail_status);
            mRuntime = (TextView) mView.findViewById(R.id.media_detail_runtime);
            mRating = (TextView) mView.findViewById(R.id.media_detail_rating);
            mOverview = (TextView) mView.findViewById(R.id.media_detail_overview);

            // Here we set a click listener so we can send a share intent if clicked
            mShare = (TextView) mView.findViewById(R.id.media_share);
            mShare.setOnClickListener(this);
        }

        // Sets the poster correctly, including setting default poster images
        void setPoster(String url) {
            mPoster.setImageUrl(mContext.getString(R.string.api_poster_base_path) + url, VolleySingleton.getInstance(mContext).getImageLoader());
            mPoster.setDefaultImageResId(R.drawable.noposter);
            mPoster.setErrorImageResId(R.drawable.noposter);
        }

        // This function sends a share intent with the first trailer to any apps that listen for plain text
        @Override
        public void onClick(View v) {

            if (mTrailerCount != 0) {

                // Get our first trailer and set the url, if a youtube link prepend it with the right url
                mTrailerCursor.moveToFirst();
                String url = (mTrailerCursor.getString(DetailFragment.COLUMN_SITE).equals("YouTube")) ?
                        "http://www.youtube.com/watch?v=" + mTrailerCursor.getString(DetailFragment.COLUMN_KEY) :
                        mTrailerCursor.getString(DetailFragment.COLUMN_KEY);

                // Create our intent and start it using startActivity
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.setType("text/plain");

                // Check to ensure that an application actually can use the intent
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                }
            }
        }
    }

    // The view holder for the reviews, fairly straightforward, again populated manually
    public final class ReviewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        final TextView mAuthor;
        final TextView mReview;


        public ReviewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mAuthor = (TextView) mView.findViewById(R.id.author_textview);
            mReview = (TextView) mView.findViewById(R.id.review_textview);
        }
    }

    // View holder for the trailers, populated manually and when clicked tries to open the trailer in question
    public final class TrailerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final View mView;
        TextView mTitle;
        String mKey;
        String mSite;

        public TrailerHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTitle = (TextView) mView.findViewById(R.id.trailer_title);
            mView.setOnClickListener(this);
        }

        // Function in order to open an intent to view the trailer
        @Override
        public void onClick(View v) {
            Uri uri;

            // Check if we are a youtube url and if so prepend it with the youtube url.
            switch (mSite) {
                case "YouTube": {
                    uri = Uri.parse("http://youtube.com/watch?v=" + mKey);
                    break;
                }
                default: {
                    uri = Uri.parse(mKey);
                }
            }

            // Create our intent and try to send it
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(intent);
            }
        }
    }

}
