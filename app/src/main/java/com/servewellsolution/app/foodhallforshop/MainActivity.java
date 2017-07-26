package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.soundcloud.android.crop.Crop;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_SHOPID;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_SHOPIMG;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_USERID;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.PREF_NAME;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static final int LOCATION = 1;
    private static final int PHONE = 2;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    static AbsoluteLayout notif_count_order;
    static AbsoluteLayout notif_count_delivery;
    static Button notifCountbtn_order;
    static Button notifCountbtn_delivery;
    static AbsoluteLayout notif_view_order;
    static AbsoluteLayout notif_view_delivery;
    static int mNotifCount_Order = 0;
    static int mNotifCount_Delivey = 0;
    private FragmentOrder timeline;
    private FragmentNotification noti;
    private FragmentOther other;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;
    private AsyncTask<Void, Void, Void> task;
    private int neworder = 0;
    private int delivery = 0;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
                setnoti();
            }
        }

        String token = FirebaseInstanceId.getInstance().getToken();

        SessionManagement sess = new SessionManagement(getApplicationContext());
        this.userdetail = sess.getUserDetails();
        Log.d("MyFirebaseIIDService", "Refreshed tokenxx: " + token);
        updatetoken(token);


        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("shops");
        dbRef.child(this.userdetail.get(KEY_SHOPID)).child("updatedate").setValue(ServerValue.TIMESTAMP);
        dbRef.child(this.userdetail.get(KEY_SHOPID)).child("updatedate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("firebase", "getUser:onChanges:" + dataSnapshot.getValue());

                if (timeline != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setnoti();
                        }
                    }, 1 * 1000);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("firebase", "getUser:onCancelled", databaseError.toException());
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setupTabIcons();

        this.fab = (FloatingActionButton) this.findViewById(R.id.fab);
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent newActivity = new Intent(getBaseContext(), OrderlistActivity.class);
//                newActivity.putExtra("SOMEKEY", URL);
                startActivity(newActivity);

            }
        });

        SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();

        this.methodRequiresPhonePermission();
        this.methodRequiresTwoPermission();

    }



    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    private void updatetoken(final String token) {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    settoken(token);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            }

        };
        this.task.execute((Void[]) null);
    }

    private void settoken(String token) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/settoken");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("userid", userdetail.get(KEY_USERID).toString()));
            nameValuePairs.add(new BasicNameValuePair("token", token));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

            Log.d("Response", "Respons: " + s);


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    private void setnoti() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    getnoti();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                setNotifCount_Order(neworder);
                setNotifCount_Delivey(delivery);
//                if(timeline.isAdded()){
//                    timeline.refresh();
//                }

            }

        };
        this.task.execute((Void[]) null);
    }

    private void getnoti() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getordernoticount");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("shopid", userdetail.get(KEY_SHOPID).toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

            Log.d("Response", "Respons: " + s);
            final JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "ResponseAll: " + jsonObj);
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONArray obj = (JSONArray) jsonObj.get("result");
                Log.d("Response", "objorder: " + obj);
                if (obj.length() > 0) {
                    neworder = Integer.parseInt(obj.getJSONObject(0).getString("neworder"));
                    delivery = Integer.parseInt(obj.getJSONObject(0).getString("delivery"));
                }

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void setupTabIcons() {

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("ภาพรวม");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.image1, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("รายการสั่ง");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.image2, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabThree.setText("แจ้งเตือน");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.image3, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabThree);

        TextView tabFour = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabFour.setText("ตั้งค่าร้านค้า");
        tabFour.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.image4, 0, 0);
        tabLayout.getTabAt(3).setCustomView(tabFour);

        tabOne.setSelected(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        // menu.findItem(R.id.badge).setIcon(resizeImage(R.drawable.ic_notification, 200, 250));

//        View count = menu.findItem(R.id.badge).getActionView();
//        notifCount = (Button) count.findViewById(R.id.notif_count);
//        notifCount.setText(String.valueOf(mNotifCount));

        MenuItem item = menu.findItem(R.id.badge);
        MenuItemCompat.setActionView(item, R.layout.feed_update_count);
        notif_count_order = (AbsoluteLayout) MenuItemCompat.getActionView(item);
        notifCountbtn_order = (Button) notif_count_order.findViewById(R.id.notif_count_order);
        notif_view_order = (AbsoluteLayout) notif_count_order.findViewById(R.id.notif_view_order);
        notifCountbtn_order.setText(String.valueOf(mNotifCount_Order));
        if (mNotifCount_Order > 0) {
            this.notifCountbtn_order.setVisibility(View.VISIBLE);
        } else {
            this.notifCountbtn_order.setVisibility(View.GONE);
        }

        notif_view_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(1);
//                if(timeline.isAdded()){
//                    timeline.refresh();
//                }
            }
        });

        item = menu.findItem(R.id.badgeorder);
        MenuItemCompat.setActionView(item, R.layout.feed_pendingdelivery_count);
        notif_view_delivery = (AbsoluteLayout) MenuItemCompat.getActionView(item);
        notifCountbtn_delivery = (Button) notif_view_delivery.findViewById(R.id.notif_count_delivery);
        notifCountbtn_delivery.setText(String.valueOf(mNotifCount_Delivey));
        if (mNotifCount_Delivey > 0) {
            this.notifCountbtn_delivery.setVisibility(View.VISIBLE);
        } else {
            this.notifCountbtn_delivery.setVisibility(View.GONE);
        }

        notif_view_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newActivity = new Intent(getBaseContext(), OrderlistActivity.class);
//                newActivity.putExtra("SOMEKEY", URL);
                startActivity(newActivity);
            }
        });

//        menu.findItem(R.id.action_search).setIcon(resizeImage(R.drawable.ic_search,250,250));
//        menu.findItem(R.id.action_more).setIcon(resizeImage(R.drawable.ic_more,70,250));
        return true;

    }

    //Here's a runnable/handler combo
//    private Runnable refreshdelay = new Runnable() {
//        @Override
//        public void run() {
//            if (timeline != null) {
//                timeline.refresh();
//            }
//        }
//    };

    private void setNotifCount_Order(int count) {
        mNotifCount_Order = count;
        if (count > 0) {
            this.notifCountbtn_order.setVisibility(View.VISIBLE);
        } else {
            this.notifCountbtn_order.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    private void setNotifCount_Delivey(int count) {
        mNotifCount_Delivey = count;
        if (count > 0) {
            this.notifCountbtn_delivery.setVisibility(View.VISIBLE);
        } else {
            this.notifCountbtn_delivery.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // Respond to the action bar's Up/Home button
            case R.id.badge:
                return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @AfterPermissionGranted(PHONE)
    private boolean methodRequiresPhonePermission() {
        String[] perms = {android.Manifest.permission.CALL_PHONE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Foodhall ขออนุญาติการใช้งานการโทรออก",
                    PHONE, perms);
        }
        return false;
    }


    @AfterPermissionGranted(LOCATION)
    private boolean methodRequiresTwoPermission() {
        String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Foodhall ขออนุญาติการใช้งานการระบุตำแหน่ง",
                    LOCATION, perms);
        }
        return false;
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentHome.newInstance();
                case 1:
                    timeline = FragmentOrder.newInstance();
                    return timeline;
                case 2:
                    noti = FragmentNotification.newInstance();
                    return noti;
                case 3:
                    other = FragmentOther.newInstance();
                    return other;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }

    }

    private void handleCrop(int resultCode, Intent result) {
        Log.d("resultView", "" + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Crop.getOutput(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadPhoto(bitmap);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadPhoto(Bitmap bitmap) {
        try {
            executeMultipartPost(bitmap);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void executeMultipartPost(Bitmap bm) throws Exception {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] data = bos.toByteArray();
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(getString(R.string.apiaddress) + "api/upload_images_shop");
            ByteArrayBody bab = new ByteArrayBody(data, "img.png");
            // File file= new File("/mnt/sdcard/forest.png");
            // FileBody bin = new FileBody(file);
            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("fileUpload", bab);
            reqEntity.addPart("shopid", new StringBody(userdetail.get(KEY_SHOPID).toString()));

            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

            JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "Response: " + jsonObj.get("FileName"));
            String imgsec = getString(R.string.imageaddress) + jsonObj.get("FileName").toString();
            editor.putString(KEY_SHOPIMG, imgsec);
            editor.apply();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    other.refresh();
                }
            }, 1 * 1000);

        } catch (Exception e) {
            // handle exception here
            Log.e(e.getClass().getName(), e.getMessage());
        }
    }


    private Drawable resizeImage(int resId, int w, int h) {
        // load the origial Bitmap
        Bitmap BitmapOrg = BitmapFactory.decodeResource(getResources(), resId);
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;
        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(resizedBitmap);
    }
}
