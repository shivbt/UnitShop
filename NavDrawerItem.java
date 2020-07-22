package com.ikai.unitshop;

/**
 * Created by shiv on 21/11/17.
 */

class NavDrawerItem {
    // Variable to hold image id of icon.
    private int iconId;
    // Variable to hold item name.
    private String itemName;

    // Constructor with no argument
    public NavDrawerItem() {}

    // Constructor with icon id and item name as arguments.
    public NavDrawerItem(final int iconId, final String itemName) {
        this.iconId = iconId;
        this.itemName = itemName;
    }

    // Make getter methods for this class.
    public int getIconId() {
        return this.iconId;
    }

    public String getItemName() {
        return this.itemName;
    }

}
