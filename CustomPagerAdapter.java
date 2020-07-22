package com.ikai.unitshop;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

/**
 * Created by shiv on 1/12/17.
 */

public class CustomPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> shopImages;

    public CustomPagerAdapter(final Context context, final ArrayList<String> shopImages) {
        this.context = context;
        this.shopImages = shopImages;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {

        // Create layout inflater and inflate the swipe layout.
        LayoutInflater layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View itemView = layoutInflater.inflate(R.layout.swipe_layout, null);

        // Get reference to image view in the layout.
        ImageView shopImageView = itemView.findViewById(R.id.slider_image_view);

        // Use glide to load the image.
        Glide.with(context).load(shopImages.get(position))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model
                            , Target<Drawable> target, boolean isFirstResource) {

                        // Hide all progress bar if it is shown.
                        int totalChildCount = container.getChildCount();
                        View view;
                        for (int i = 0; i < totalChildCount; i++) {
                            view = container.getChildAt(i);
                            ProgressBar progressBar = view.findViewById(R.id.progress_bar);
                            if (progressBar.getVisibility() == View.VISIBLE) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model
                            , Target<Drawable> target, DataSource dataSource
                            , boolean isFirstResource) {

                        // Hide the progress bar if it is shown.
                        int totalChildCount = container.getChildCount();
                        View view;
                        for (int i = 0; i < totalChildCount; i++) {
                            view = container.getChildAt(i);
                            ProgressBar progressBar = view.findViewById(R.id.progress_bar);
                            if (progressBar.getVisibility() == View.VISIBLE) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                        return false;
                    }
                })
                .thumbnail(0.5f)
                .into(shopImageView);

        // Finally add the itemView to the container.
        container.addView(itemView);

        // And return the itemView object.
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((FrameLayout) object));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return shopImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (FrameLayout) object);
    }
}
