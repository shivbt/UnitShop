package com.ikai.unitshop;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by shiv on 23/11/17.
 */

class GridCategoryAdapter extends RecyclerView.Adapter<GridCategoryAdapter.MyViewHolder> {
    // Object to hold all category names and image URL.
    private List<ShopCategory> shopCategoryList;

    // Context object necessary for image loading through Glide.
    private Context context;


    GridCategoryAdapter(final Context context, final List<ShopCategory>shopCategoryList) {
        this.shopCategoryList = shopCategoryList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_category_card_grid_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ShopCategory shopCategory = shopCategoryList.get(position);
        holder.categoryTextView.setText(shopCategory.getCategoryName());

        // Use glide to load the image.
        Glide.with(context).load(shopCategory.getCategoryImageURL())
                .thumbnail(0.4f)
                .into(holder.categoryImageView);
    }

    @Override
    public int getItemCount() {
        return shopCategoryList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImageView;
        TextView categoryTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            categoryImageView = itemView.findViewById(R.id.category_image);
            categoryTextView = itemView.findViewById(R.id.category_text);
        }
    }
}
