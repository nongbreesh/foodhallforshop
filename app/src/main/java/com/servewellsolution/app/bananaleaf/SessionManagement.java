package com.servewellsolution.app.bananaleaf;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Breeshy on 8/30/2016 AD.
 */

public class SessionManagement {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    public static final String PREF_NAME = "shopuser";

    // All Shared Preferences Keys
    public static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_SHOPLAT = "shoplat";
    public static final String KEY_SHOPLNG = "shoplng";
    public static final String KEY_SHOPRADIUS = "shopradius";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USERID = "userid";
    public static final String KEY_SHOPID = "shopid";
    public static final String KEY_ISSHOPOPEN = "isshopopen";
    public static final String KEY_MINAMOUNT = "minamount";
    public static final String KEY_MINPRICE = "minprice";
    public static final String KEY_ORDERTIME = "ordertime";
    public static final String KEY_SHOPNAME = "shopname";
    public static final String KEY_SHOPIMG = "shopimg";
    public static final String KEY_APPROVE = "isapprove";
    // Constructor
    public SessionManagement(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USERID, pref.getString(KEY_USERID, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_SHOPLAT, pref.getString(KEY_SHOPLAT, null));
        user.put(KEY_SHOPLNG, pref.getString(KEY_SHOPLNG, null));
        user.put(KEY_SHOPRADIUS, pref.getString(KEY_SHOPRADIUS, null));
        user.put(KEY_ISSHOPOPEN, pref.getString(KEY_ISSHOPOPEN, null));
        user.put(KEY_MINAMOUNT, pref.getString(KEY_MINAMOUNT, null));
        user.put(KEY_MINPRICE, pref.getString(KEY_MINPRICE, null));
        user.put(KEY_ORDERTIME, pref.getString(KEY_ORDERTIME, null));
        user.put(KEY_SHOPNAME, pref.getString(KEY_SHOPNAME, null));
        user.put(KEY_SHOPIMG, pref.getString(KEY_SHOPIMG, null));
        user.put(KEY_APPROVE, pref.getString(KEY_APPROVE, null));
        user.put(KEY_SHOPID, pref.getString(KEY_SHOPID, null));
        return user;
    }

}
