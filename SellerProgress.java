package com.ikai.unitshop;

/**
 * Created by shiv on 22/11/17.
 */

public class SellerProgress {
    public boolean registered;

    // Default constructor required for calls to DataSnapshot.getValue(Seller.class)
    public SellerProgress() {}

    SellerProgress(final boolean registered) {
        this.registered = registered;
    }

    void setRegistered() {
        this.registered = true;
    }
}
