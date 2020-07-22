package com.ikai.unitshop;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiv on 21/11/17.
 */

class NavDrawerItemsAdapter extends RecyclerView.Adapter<NavDrawerItemsAdapter.MyViewHolder> {

    // List to store all drawer items names and icons.
    private List<NavDrawerItem> navDrawerItemList;

    NavDrawerItemsAdapter(List<NavDrawerItem> navDrawerItemList) {
        this.navDrawerItemList = navDrawerItemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_nav_drawer_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NavDrawerItem navDrawerItem = navDrawerItemList.get(position);
        holder.itemName.setText(navDrawerItem.getItemName());
        holder.itemIcon.setImageResource(navDrawerItem.getIconId());
    }

    @Override
    public int getItemCount() {
        return navDrawerItemList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView itemIcon;
        TextView itemName;

        MyViewHolder(View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_text);
        }
    }
}
