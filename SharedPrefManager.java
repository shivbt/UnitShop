package com.ikai.unitshop;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by shiv on 18/11/17.
 */

class SharedPrefManager {
    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "TrackUser";

    // All Shared Preferences Keys
    private static final String IS_Shop_Registered = "IsShopRegistered";
    private static final String IS_ACCOUNT_DETAILS_FILLED = "IsAccountDetailsFilled";

    // Constructor
    SharedPrefManager(Context context){
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // Function to check whether shop is registered completely.
    boolean isShopRegistered() {
        return pref.getBoolean(IS_Shop_Registered, false);
    }

    // Function to check whether account details is filled or not.
    boolean isAccountDetailsFilled() {
        return pref.getBoolean(IS_ACCOUNT_DETAILS_FILLED, false);
    }

    // Function to set IS_ACCOUNT_DETAILS_FILLED to true.
    void setAccountDetailsFilled() {
        editor.putBoolean(IS_ACCOUNT_DETAILS_FILLED, true);
        editor.commit();
    }

    // Function to set IS_Shop_Registered to true.
    void setShopRegistered() {
        editor.putBoolean(IS_Shop_Registered, true);
        editor.commit();
    }

}
