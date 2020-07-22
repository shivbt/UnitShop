package com.ikai.unitshop;

import java.util.ArrayList;

/**
 * Created by shiv on 19/11/17.
 */

public class Seller {
    public String shopName, shopAddress, shopPinCode, sellerMobNumber;
    public ArrayList<String> categories;

    // Default constructor required for calls to DataSnapshot.getValue(Seller.class)
    public Seller() {}

    public Seller(final String shopName, final String shopAddress
            , final String shopPinCode, final String sellerMobNumber,
           final ArrayList<String> categories) {
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopPinCode = shopPinCode;
        this.sellerMobNumber = sellerMobNumber;
        this.categories = categories;
    }

}
