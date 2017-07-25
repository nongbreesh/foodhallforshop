package com.servewellsolution.app.bananaleaf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_MINPRICE;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_ORDERTIME;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPID;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPLAT;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPLNG;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPNAME;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPRADIUS;
import static com.servewellsolution.app.bananaleaf.SessionManagement.PREF_NAME;

public class ShopSettingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static float radius = 0;
    private GoogleMap gmap;
    private Circle mapCircle;
    private static Double lat;
    private static Double lng;
//    private static String minamount;
    private static String minprice;
    private static String shopname;
    private static int ordertime;
    private Marker gmarker;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;
//    private Spinner txtradius;
    private ProgressDialog dialog;
    private MapFragment mapFragment;
    private Button id_submit_button;
    //private EditText txtminamount;
    private EditText txtminprice;
    private Spinner txtordertime;
    private EditText txtshopname;
    private static final int SHOPSETTING = 107;
    private AsyncTask<Void, Void, Void> task;
    private SeekBar seekBar;
    private TextView txtseekbarradius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopsetting);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        dialog = new ProgressDialog(this);


        this.setTitle("ตั้งค่าร้านค้า");

        this.mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        //this.txtminamount = (EditText) findViewById(R.id.txtminamount);
        this.txtminprice = (EditText) findViewById(R.id.txtminprice);
        this.txtordertime = (Spinner) findViewById(R.id.txtordertime);
        this.id_submit_button = (Button) findViewById(R.id.id_submit_button);
        this.txtshopname = (EditText) findViewById(R.id.txtshopname);

        setting();

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        txtseekbarradius = (TextView) findViewById(R.id.txtseekbarradius);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = (float) ((float) progress / 10.0);
                txtseekbarradius.setText(value + " กิโลเมตร");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                 radius = (float) ((float) seekBar.getProgress() / 10.0);
                if (gmap != null) {
                    if (mapCircle != null) {
                        mapCircle.remove();
                    }
                    mapCircle = gmap.addCircle(new CircleOptions()
                            .center(new LatLng(lat, lng))
                            .radius(radius * 1000)
                            .strokeColor(Color.RED)
                            .fillColor(getResources().getColor(R.color.overlaycolor)));

                    setmapradius(radius);
                }
            }
        });


        this.id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setusersession();
            }
        });

        Button btnsetlocation = (Button) findViewById(R.id.btnsetlocation);
        btnsetlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), Mapsetting.class);
                startActivityForResult(intent, 1001);
            }
        });

//        this.txtradius = (Spinner) findViewById(R.id.txtradius);
//        this.txtradius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                radius = Float.parseFloat(parent.getItemAtPosition(position).toString());
//                if (gmap != null) {
//                    if (mapCircle != null) {
//                        mapCircle.remove();
//                    }
//                    mapCircle = gmap.addCircle(new CircleOptions()
//                            .center(new LatLng(lat, lng))
//                            .radius(radius * 1000)
//                            .strokeColor(Color.RED)
//                            .fillColor(getResources().getColor(R.color.overlaycolor)));
//
//                    setmapradius(radius);
//
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }


    private void setusersession() {

        dialog.setMessage("กรุณารอสักครู่...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    updateshop();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                editor.putString(KEY_SHOPRADIUS, radius + "");
                editor.putString(KEY_SHOPLAT, lat + "");
                editor.putString(KEY_SHOPLNG, lng + "");
                editor.putString(KEY_ORDERTIME, txtordertime.getSelectedItem().toString());
                editor.putString(KEY_MINPRICE, txtminprice.getText().toString());
                //editor.putString(KEY_MINAMOUNT, txtminamount.getText().toString());
                editor.putString(KEY_SHOPNAME, txtshopname.getText().toString());
                editor.apply();

                dialog.hide();


                setResult(RESULT_OK);
                finishActivity(SHOPSETTING);
                finish();


            }

        };
        this.task.execute((Void[]) null);
    }

    private void updateshop() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/updateshop");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", userdetail.get(KEY_SHOPID).toString()));
            nameValuePairs.add(new BasicNameValuePair("title", txtshopname.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("lat", lat + ""));
            nameValuePairs.add(new BasicNameValuePair("lng", lng + ""));
            nameValuePairs.add(new BasicNameValuePair("radius", radius + ""));
            nameValuePairs.add(new BasicNameValuePair("minprice", txtminprice.getText().toString()));
            //nameValuePairs.add(new BasicNameValuePair("minamout", txtminamount.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("ordertime", txtordertime.getSelectedItem().toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            Log.d("Response", "Response: " + s);
            JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "ResponseAll: " + jsonObj);
            Log.d("Response", "ResponseJson: " + jsonObj.get("result"));


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            dialog.hide();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            dialog.hide();
        } catch (JSONException e) {
            e.printStackTrace();
            dialog.hide();
        }
    }

    private void setting() {
        SessionManagement sess = new SessionManagement(getApplicationContext());
        this.userdetail = sess.getUserDetails();
        this.radius = Float.parseFloat(this.userdetail.get(KEY_SHOPRADIUS).toString());
        this.ordertime = Integer.parseInt(this.userdetail.get(KEY_ORDERTIME).toString());
        this.lat = Double.parseDouble(this.userdetail.get(KEY_SHOPLAT).toString());
        this.lng = Double.parseDouble(this.userdetail.get(KEY_SHOPLNG).toString());

        //this.minamount = this.userdetail.get(KEY_MINAMOUNT).toString();
        this.minprice = this.userdetail.get(KEY_MINPRICE).toString();
        this.shopname = this.userdetail.get(KEY_SHOPNAME).toString();


        //this.txtminamount.setText(this.minamount);
        this.txtminprice.setText(this.minprice);
        this.txtshopname.setText(this.shopname);


//        final List<String> radius_arrays = Arrays.asList(getResources().getStringArray(R.array.radius_arrays));
//        final List<String> ordertime_arrays = Arrays.asList(getResources().getStringArray(R.array.ordertime_arrays));

        if (this.radius > 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    radius = Float.parseFloat(userdetail.get(KEY_SHOPRADIUS).toString());
//                    txtradius.setSelection(radius_arrays.indexOf(radius + ""));
                    int intseeker = Integer.parseInt(String.valueOf(Math.round(radius * 10)));
                    txtseekbarradius.setText(intseeker + " กิโลเมตร");
                    seekBar.setProgress(intseeker);
                }
            }, 500);

        } else {
            txtseekbarradius.setText("0 กิโลเมตร");
            seekBar.setProgress(0);
        }


        if (this.ordertime > 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        if(userdetail.get(KEY_ORDERTIME) != null){
                            String[] androidStrings = getResources().getStringArray(R.array.ordertime_arrays);
                            int pos = 0;
                            for (String s : androidStrings) {
                                int i = s.indexOf(userdetail.get(KEY_ORDERTIME).toString());
                                Log.d("ordertime", "" + i);
                                if (i >= 0) {
                                    txtordertime.setSelection(pos);
                                }
                                pos++;
                            }

                        }
                    }catch (Exception e){
                        Log.d("ordertime", "" + e.getMessage());
                    }


                }
            }, 500);

        }

        this.mapFragment.getMapAsync(this);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                this.lat = Double.parseDouble(data.getStringExtra("lat"));
                this.lng = Double.parseDouble(data.getStringExtra("lng"));
                if (gmap != null) {
                    if (mapCircle != null) {
                        mapCircle.remove();
                    }
                    if (gmarker != null) {
                        gmarker.remove();
                    }
                    gmap.addMarker(new MarkerOptions()
                            .position(new LatLng(this.lat, this.lng))
                            .title("ตำแหน่งร้านของคุณ"));
                    CameraUpdate center =
                            CameraUpdateFactory.newLatLng(new LatLng(this.lat, this.lng));
                    gmap.moveCamera(center);
                    mapCircle = gmap.addCircle(new CircleOptions()
                            .center(new LatLng(this.lat, this.lng))
                            .radius(this.radius * 1000)
                            .strokeColor(Color.RED)
                            .fillColor(getResources().getColor(R.color.overlaycolor)));
                }


            }
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        gmap = map;
        gmap.getUiSettings().setScrollGesturesEnabled(false);
        gmarker = gmap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title("ตำแหน่งร้านของคุณ"));
        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(lat, lng));


        mapCircle = gmap.addCircle(new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(radius * 1000)
                .strokeColor(Color.RED)
                .fillColor(getResources().getColor(R.color.overlaycolor)));


        gmap.moveCamera(center);
        setmapradius(radius);
    }

    public void setmapradius(float rad) {
        // CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
//        if (rad >= 1) {
//            zoom = CameraUpdateFactory.zoomTo(15);
//        }
//        if (rad >= 1.5) {
//            zoom = CameraUpdateFactory.zoomTo(14);
//        }
//        if (rad >= 3) {
//            zoom = CameraUpdateFactory.zoomTo(13);
//        }
//        if (rad >= 4) {
//            zoom = CameraUpdateFactory.zoomTo(13);
//        }
//        if (rad >= 5) {
//            zoom = CameraUpdateFactory.zoomTo(12);
//        }
//        if (rad >= 7) {
//            zoom = CameraUpdateFactory.zoomTo(12);
//        }

        gmap.animateCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(rad)), 1000, null);
    }

    private int calculateZoomLevel(float rad) {
        double equatorLength = 40075004; // in meters
        double widthInPixels = 45;
        double metersPerPixel = equatorLength / 256;
        int zoomLevel = 1;
        while ((metersPerPixel * widthInPixels) > (rad * 1000)) {
            metersPerPixel /= 2;
            ++zoomLevel;
        }
        Log.i("ADNAN", "zoom level = " + zoomLevel);
        return zoomLevel;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
