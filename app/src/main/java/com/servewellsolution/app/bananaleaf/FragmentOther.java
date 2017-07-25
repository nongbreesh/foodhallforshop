package com.servewellsolution.app.bananaleaf;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.servewellsolution.app.bananaleaf.Adapter.ListItem;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_APPROVE;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_ISSHOPOPEN;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_MINAMOUNT;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_MINPRICE;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_ORDERTIME;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPID;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPIMG;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPLAT;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPLNG;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPNAME;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPRADIUS;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_USERID;
import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_USERNAME;
import static com.servewellsolution.app.bananaleaf.SessionManagement.PREF_NAME;


/**
 * Created by Breeshy on 4/11/2016 AD.
 */
public class FragmentOther extends Fragment {
    private ImageView imageView;
    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private OtherAdapter listAdpt;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;
    private static final int SELECT_PICTURE = 0;
    private static final int SHOPSETTING = 107;

    public FragmentOther() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            beginCrop(selectedImage);
        }
        if (requestCode == SHOPSETTING && resultCode == Activity.RESULT_OK) {
            refresh();
        }

    }


    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));

        Crop.of(source, destination).withAspect(400, 250).withMaxSize(600, 600).start(getActivity());
    }

    public static FragmentOther newInstance() {
        FragmentOther fragment = new FragmentOther();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_other, container, false);
        SharedPreferences pref = getContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        this.setting();
        this.listview = (ListView) rootView.findViewById(R.id.listView);
        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Intent intent;
                switch (pos) {
                    case 1:
                        intent = new Intent(getContext(), MenuSettingActivity.class);
                        intent.putExtra("position", "" + pos);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(getContext(), ShopSettingActivity.class);
                        intent.putExtra("position", "" + pos);
                        startActivityForResult(intent, SHOPSETTING);
                        break;
                    case 3:
                        new AlertDialog.Builder(getContext())
                                .setTitle("แจ้งเตือน")
                                .setMessage("คุณต้องการออกจากระบบใช่หรือไม่?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        editor.clear();
                                        editor.commit();
                                        Intent i = new Intent(getContext(), SplashActivity.class);

                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        // Staring Login Activity
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                        break;
                }
            }
        });
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                setting();
                bindList();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //this.listview.setDivider(null);
        //this.listview.setDividerHeight(0);

        this.sList = new ArrayList<HashMap<String, String>>();
        this.firstload();


        return rootView;
    }

    public void refresh() {
        setting();
        bindList();
    }

    private void setting() {
        SessionManagement sess = new SessionManagement(getContext());
        this.userdetail = sess.getUserDetails();
    }

    private void bindList() {
        Log.d("this.sList", "" + this.userdetail);
        this.listAdpt = new OtherAdapter(this.getActivity(), this.sList, this.userdetail, this, editor);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);
        this.listview.deferNotifyDataSetChanged();

    }

    private void firstload() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    loadItemList();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                bindList();
            }

        };
        this.task.execute((Void[]) null);
    }


    private void loadItemList() {
        try {
            map = new HashMap<String, String>();
            map.put(ListItem.KEY_HEADER, "");
            map.put(ListItem.KEY_INDEX, "");
            map.put(ListItem.KEY_ID, "");
            sList.add(map);

            map = new HashMap<String, String>();
            map.put(ListItem.KEY_HEADER, "เมนู");
            map.put(ListItem.KEY_INDEX, "");
            map.put(ListItem.KEY_ID, "");
            sList.add(map);

            map = new HashMap<String, String>();
            map.put(ListItem.KEY_HEADER, "ตั้งค่าร้านค้า");
            map.put(ListItem.KEY_INDEX, "");
            map.put(ListItem.KEY_ID, "");
            sList.add(map);

            map = new HashMap<String, String>();
            map.put(ListItem.KEY_HEADER, "ออกจากระบบ");
            map.put(ListItem.KEY_INDEX, "");
            map.put(ListItem.KEY_ID, "");
            sList.add(map);

        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}


class OtherAdapter extends BaseAdapter {
    private HashMap<String, String> userdetail;
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private Fragment fragment;
    private static LayoutInflater inflater = null;
    private static final int SELECT_PICTURE = 0;
    private SharedPreferences.Editor editor;
    private static final int SHOPSETTING = 107;
    private boolean issetautosc;
    private ProgressDialog indialog;
    private AsyncTask<Void, Void, Void> task;

    public OtherAdapter(Activity a, ArrayList<HashMap<String, String>> d, HashMap<String, String> userdetail, Fragment f, SharedPreferences.Editor e) {
        this.data = d;
        this.activity = a;
        this.fragment = f;
        this.editor = e;
        this.userdetail = userdetail;
        indialog = new ProgressDialog(activity);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.data.size();
    }


    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, final ViewGroup parent) {
        HashMap<String, String> tmp = this.data.get(position);
        String header = tmp.get(ListItem.KEY_HEADER);
        if (position == 0) {
            convertView = inflater.inflate(R.layout.shopheader_setting, parent, false);
            ImageView profile_header_bg = (ImageView) convertView.findViewById(R.id.circular_image_view);
            SwitchCompat sc_push = (SwitchCompat) convertView.findViewById(R.id.sc_push);
            TextView txtdesc_row1 = (TextView) convertView.findViewById(R.id.txtdesc_row1);
            TextView editshopimg = (TextView) convertView.findViewById(R.id.editshopimg);
            editshopimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("editshopimg", "editshopimg");
                    pickPhoto(v);
                }
            });

            sc_push.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d("Boolean", Boolean.toString(isChecked));
                    Log.d("issetautosc", issetautosc + "");

                    setusersession(isChecked, buttonView);


                }
            });

            if (this.userdetail.get(KEY_ISSHOPOPEN).toString().equals("true")) {
                sc_push.setChecked(true);
                this.issetautosc = false;
            } else {
                sc_push.setChecked(false);
                this.issetautosc = true;
            }

            if (this.userdetail.get(KEY_SHOPNAME).toString().equals("")) {
                txtdesc_row1.setText("กรุณาตั้งค่าร้านค้า");
                txtdesc_row1.setTextColor(Color.parseColor("#F00000"));
            } else {
                txtdesc_row1.setText(this.userdetail.get(KEY_SHOPNAME).toString());
            }

            Picasso.with(this.activity).load(this.userdetail.get(KEY_SHOPIMG).toString()).into(profile_header_bg);

        } else if (position == 3) {
            convertView = inflater.inflate(R.layout.logout, parent, false);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(header);
        } else {
            convertView = inflater.inflate(R.layout.settinglist, parent, false);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(header);

        }

        return convertView;
    }

    private void setscopenshop(Boolean isChecked, CompoundButton buttonView) {
        if (!userdetail.get(KEY_SHOPNAME).toString().equals("")) {
            if (userdetail.get(KEY_APPROVE).toString().equals("1")) {
                openshoprequest(isChecked);
//                if (isChecked && issetautosc) {
//                                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
//                                alert.setTitle("ต้องการส่งการแจ้งเตือนถึงผู้สนใจหรือไม่");
//                                // alert.setMessage("Message");
//                                alert.setPositiveButton("ต้องการ", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, int whichButton) {
//                                        // push noti to user waiting for this shop open
//                                         final ProgressDialog indialog = new ProgressDialog(activity);
//                                        indialog.setMessage("กรุณารอสักครู่...");
//                                        indialog.setCancelable(false);
//                                        indialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                                        indialog.show();
//
//                                        new Handler().postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                indialog.hide();
//                                                Toast.makeText(activity, "ส่งข้อมูลถึงผู้สนใจแล้ว", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }, 3 * 1000);
//
//                                    }
//                                });
//                                alert.setNegativeButton("เอาไว้ทีหลัง",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                            }
//                                        });
//
//                                alert.show();
//                } else {
//                    issetautosc = true;
//                }
            } else {
                if (isChecked) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    alert.setTitle("กรณีเปิดร้านครั้งแรก กรุณารอทีมงานตรวจสอบร้านค้าสักครู่");
                    // alert.setMessage("Message");

                    alert.setPositiveButton("ส่งข้อมูลเปิดร้าน", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            openrequest();
                        }
                    });
                    alert.setNegativeButton("เอาไว้ทีหลัง",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });

                    alert.show();
                    issetautosc = true;
                    buttonView.setChecked(false);
                }
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setTitle("คุณยังตั้งค่าร้านค้าไม่เรียบร้อย");
            // alert.setMessage("Message");

            alert.setPositiveButton("ไปตั้งค่าตอนนี้", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent intent = new Intent(activity, ShopSettingActivity.class);
                    fragment.startActivityForResult(intent, SHOPSETTING);
                }
            });
            alert.setNegativeButton("เอาไว้ทีหลัง",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });

            alert.show();

            buttonView.setChecked(false);
        }
    }

    private void setusersession(final Boolean isChecked, final CompoundButton buttonView) {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    updateusersession();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                setscopenshop(isChecked, buttonView);
                return;
            }

        };
        this.task.execute((Void[]) null);
    }


    public void updateusersession() {
// Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(activity.getString(R.string.apiaddress) + "api/getuserdetail");

        try {

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("userid", this.userdetail.get(KEY_USERID).toString()));
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
            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Response", "ResponseAll: " + jsonObj);
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONObject obj = (JSONObject) jsonObj.get("result");
                Log.d("Response", "ResponseObject: " + obj);

                //create login session
                try {
                    editor.putString(KEY_USERID, obj.get("userid").toString());
                    editor.putString(KEY_ISSHOPOPEN, obj.get("isshopopen").toString());
                    editor.putString(KEY_SHOPLAT, obj.get("lat").toString());
                    editor.putString(KEY_SHOPLNG, obj.get("lng").toString());
                    editor.putString(KEY_SHOPRADIUS, obj.get("radius").toString());
                    editor.putString(KEY_MINPRICE, obj.get("minprice").toString());
                    editor.putString(KEY_MINAMOUNT, obj.get("minamout").toString());
                    editor.putString(KEY_ORDERTIME, obj.get("ordertime").toString());
                    editor.putString(KEY_SHOPNAME, obj.get("title").toString());
                    editor.putString(KEY_APPROVE, obj.get("isapprove").toString());
                    editor.putString(KEY_SHOPIMG, activity.getString(R.string.imageaddress) + obj.get("img").toString());
                    editor.putString(KEY_USERNAME, obj.get("email").toString());
                    editor.putString(KEY_SHOPID, obj.get("shopid").toString());

                    editor.apply();
                    SessionManagement sess = new SessionManagement(activity);
                    this.userdetail = sess.getUserDetails();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    private void openshoprequest(final boolean isChecked) {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    setshopopen(isChecked);
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
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


    public void setshopopen(Boolean ischeck) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(activity.getString(R.string.apiaddress) + "api/setshopopen");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", userdetail.get(KEY_SHOPID).toString()));
            nameValuePairs.add(new BasicNameValuePair("isshopopen", ischeck + ""));
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
            editor.putString(KEY_ISSHOPOPEN, Boolean.toString(ischeck));
            editor.apply();

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    private void openrequest() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    sendopenrequest();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                Toast.makeText(activity, "ส่งข้อมูลเพื่อขอเปิดร้านเรียบร้อย", Toast.LENGTH_SHORT).show();
            }

        };
        this.task.execute((Void[]) null);
    }

    public void sendopenrequest() {
        //requestshopopen

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(activity.getString(R.string.apiaddress) + "api/requestshopopen");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("shopid", userdetail.get(KEY_SHOPID).toString()));
            nameValuePairs.add(new BasicNameValuePair("userid", userdetail.get(KEY_USERID).toString()));
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
            Log.d("Response", "Response: " + s);
            JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "ResponseAll: " + jsonObj);
            Log.d("Response", "ResponseJson: " + jsonObj.get("result"));


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    public void pickPhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        try {

            i.putExtra("return-data", true);
            this.fragment.startActivityForResult(
                    Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }


    }


}


