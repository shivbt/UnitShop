package com.ikai.unitshop;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * Created by shiv on 16/11/17.
 */

class ShopCategoryAdapter extends RecyclerView.Adapter<ShopCategoryAdapter.MyViewHolder> {

    // Variable to hold all category name.
    private List<ShopCategory> shopCategoryList;

    ShopCategoryAdapter(List<ShopCategory> shopCategoryList) {
        this.shopCategoryList = shopCategoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_shop_category, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ShopCategory shopCategory = shopCategoryList.get(position);
        holder.catText.setText(shopCategory.getCategoryName());
    }

    @Override
    public int getItemCount() {
        return shopCategoryList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        Button catText;

        MyViewHolder(View itemView) {
            super(itemView);
            catText = itemView.findViewById(R.id.category_button);
        }
    }
}
