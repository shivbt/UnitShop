package com.ikai.unitshop;

import java.net.URL;

/**
 * Created by shiv on 16/11/17.
 */

class ShopCategory {

    // Variable to hold category name
    private String categoryName;

    // Variable to hold URL of category photo.
    private String categoryImageURL;

    // Constructor with no argument. It is necessary for dataSnapshot.getValue(ShopCategory.class);
    public ShopCategory() {}

    // Constructor with category name as argument.
    ShopCategory(final String categoryName) {
        this.categoryName = categoryName;
    }

    ShopCategory(final String categoryName, final String categoryImageURL) {
        this.categoryName = categoryName;
        this.categoryImageURL = categoryImageURL;
    }

    // Make getter and setter method for this class.
    // Get category name.
    String getCategoryName() {
        return categoryName;
    }

    // Get category image URL.
    String getCategoryImageURL() {
        return categoryImageURL;
    }


}
