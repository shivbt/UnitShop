package com.ikai.unitshop;

/**
 * Created by shiv on 25/11/17.
 */

public class Constants {
    static final int GRID_CATEGORY_WIDTH_WITH_MARGIN = 170;

    // Variable to hold height of a product card in dp.
    static final int PRODUCT_CARD_HEIGHT = 248;

    // Store all categories in string array.
    static final String[] CAT_LIST = {"Grocery", "Medicine", "Vegetable", "Fruit", "Cloths",
            "Footwear", "Non-Veg", "Fast Food", "Beverage", "Cake & Bakery", "Sweet",
            "Breakfast, Lunch & Dinner"};

    // Store all icons and item name for navigation drawer here.
    static final String[] DRAWER_ITEM_NAMES = {"Account Details", "Order Details",
            "Business", "Growth", "Growth Charge", "Rating", "About Us"
    };
    static final int[] DRAWER_ITEM_ICONS_IDS = {R.drawable.ic_bank_account
            , R.drawable.ic_orderdetail_24, R.drawable.ic_business, R.drawable.ic_growth
            , R.mipmap.ic_launcher, R.mipmap.ic_launcher
            , R.drawable.ic_about_24
    };

    // Read external storage request code.
    public static final int READ_EXTERNAL_STORAGE_REQUEST = 500;
}
