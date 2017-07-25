package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import static com.servewellsolution.app.foodhallforshop.SessionManagement.IS_LOGIN;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.PREF_NAME;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class SplashActivity extends Activity {
    private SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0);
        if (!this.isLoggedIn()) {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            // Closing all the Activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            // Closing all the Activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public boolean isLoggedIn() {
        if(pref.getString(IS_LOGIN, "").toString().equals("1")){
            return true;
        }
        else{
            return false;
        }
    }
//    public HashMap<String, String> getUserDetails() {
//        HashMap<String, String> user = new HashMap<String, String>();
//        // user name
//        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
//
//        // user email id
//        user.put(KEY_USERID, pref.getString(KEY_USERID, null));
//
//        // return user
//        return user;
//    }

}
