package com.myboxteam.scanner.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.dto.Video;
import com.myboxteam.scanner.scanlibrary.ImageResizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 5/5/16.
 */
public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {
    private List<Video> videos = new ArrayList<>();
    private Context mContext;
    private final OnItemClickListener listener;
    private BitmapFactory.Options mBitmapOptions;
    private int page = 0;
    private boolean isLoaded = true;

    private int mParentWidth;
    private int mParentHeight;
    public boolean isLoaded() {
        return isLoaded;
    }

    public void setIsLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void nextPage() {
        page++;
    }

    public Video getItem(int position) {
        return videos.get(position);
    }

    public BookRecyclerAdapter(Context context, OnItemClickListener listener, BitmapFactory.Options bitmapOptions, int parentWidth, int parentHeight) {
        mContext = context;
        mBitmapOptions = bitmapOptions;
        this.listener = listener;

        mParentHeight = parentHeight/3;
        mParentWidth = parentWidth/3;
    }


    public List<Video> addVideo(Video video) {
        videos.add(video);
        return videos;

    }

    public void restData() {
        videos = new ArrayList<>();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mThumb;

        public ViewHolder(View v) {
            super(v);

            // set the view's size, margins, paddings and layout parameters
            mTextView = (TextView) v.findViewById(R.id.video_title);
            mThumb = (ImageView) v.findViewById(R.id.video_thumb);
        }

        public void bind(final Video item, final OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = null;
        // create a new view

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_gird_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(videos.get(position), listener);
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Video video = videos.get(position);
        holder.mTextView.setText(video.getTitle());
        Bitmap bitmap = BitmapFactory.decodeFile(video.getBanner(), mBitmapOptions);
        Bitmap scaledBitmap = ImageResizer.scaleBitmap(bitmap, mParentWidth, mParentHeight);
        holder.mThumb.setImageBitmap(scaledBitmap);

    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Video item);
    }
}
