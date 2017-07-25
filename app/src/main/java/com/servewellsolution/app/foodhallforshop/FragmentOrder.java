package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.servewellsolution.app.foodhallforshop.Adapter.ListItem;
import com.servewellsolution.app.foodhallforshop.Adapter.incommingorder_list_adpt;
import com.servewellsolution.app.foodhallforshop.Helper.DatetimeHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_ORDERTIME;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_SHOPID;

/**
 * Created by Breeshy on 4/11/2016 AD.
 */
public class FragmentOrder extends Fragment {

    private HashMap map;
    public ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private TimelineAdapter listAdpt;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private HashMap<String, String> userdetail;
    private double summary = 0;
    private String orderdetailjson;
    RelativeLayout id_nothingmsg;
    DatabaseReference dbRef;

    public FragmentOrder() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentOrder newInstance() {
        FragmentOrder fragment = new FragmentOrder();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        SessionManagement sess = new SessionManagement(getContext());
        this.id_nothingmsg = (RelativeLayout) rootView.findViewById(R.id.id_nothingmsg);
        this.userdetail = sess.getUserDetails();
        this.listview = (ListView) rootView.findViewById(R.id.listView);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        this.listview.setDivider(null);
        this.listview.setDividerHeight(0);

//        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.dbRef = database.getReference("shops");
//        dbRef.child(this.userdetail.get(KEY_SHOPID)).child("updatedate").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("firebase", "getUser:onChanges:" + dataSnapshot.getValue());
//                firstload();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d("firebase", "getUser:onCancelled", databaseError.toException());
//            }
//        });

        firstload();
        return rootView;
    }

    private void bindList() {
        if (this.sList.size() > 0) {
            this.id_nothingmsg.setVisibility(View.GONE);
        } else {
            this.id_nothingmsg.setVisibility(View.VISIBLE);
        }
        if (this.getActivity() != null) {
            this.listAdpt = new TimelineAdapter(this.getActivity(), this.sList, this.dbRef, this.userdetail);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in);
            this.listview.startAnimation(animation);
            this.listview.setAdapter(this.listAdpt);
        }
    }

    public void refresh() {
        Log.d("reload", "reload");
        sList = new ArrayList<HashMap<String, String>>();
        firstload();
    }

    private void firstload() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sList = new ArrayList<HashMap<String, String>>();
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
        //getorderlist

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getorderlist");

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


                for (int i = 0; i < obj.length(); i++) {
                    String id = obj.getJSONObject(i).getString("orderid");
                    String shopid = obj.getJSONObject(i).getString("shopid");
                    String orderno = obj.getJSONObject(i).getString("orderno");
                    String status = obj.getJSONObject(i).getString("status");
                    String createdate = obj.getJSONObject(i).getString("ordercreatedate");
                    String deriverydate = obj.getJSONObject(i).getString("deriverydate");
                    String deriveryrange = obj.getJSONObject(i).getString("deriveryrange");


                    String fullname = obj.getJSONObject(i).getString("fullname");
                    String tel = obj.getJSONObject(i).getString("tel");
                    String email = obj.getJSONObject(i).getString("email");
                    String lat = obj.getJSONObject(i).getString("lat");
                    String lng = obj.getJSONObject(i).getString("lng");
                    String address = obj.getJSONObject(i).getString("address");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_ORDERID, id);
                    map.put(ListItem.KEY_ORDERSHOPID, shopid);
                    map.put(ListItem.KEY_ORDERORDERNO, orderno);
                    map.put(ListItem.KEY_ORDERSTATUS, status);
                    map.put(ListItem.KEY_ORDERCREATEDATE, createdate);
                    map.put(ListItem.KEY_ORDERDERIVERYDATE, deriverydate);
                    map.put(ListItem.KEY_ORDERDERIVERYRANGE, deriveryrange);


                    map.put("fullname", fullname);
                    map.put("tel", tel);
                    map.put("email", email);
                    map.put("lat", lat);
                    map.put("lng", lng);
                    map.put("address", address);


                    loadOrderdetail(id);


                    map.put(ListItem.KEY_ORDERDETAILJSON, orderdetailjson);
                    sList.add(map);
                }
                Log.d("sList", sList + "");

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void loadOrderdetail(String orderid) {
        orderdetailjson = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(this.getString(R.string.apiaddress) + "api/getorderdetail");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("orderid", orderid));
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
            orderdetailjson = s.toString();
            Log.d("Response", "Respons: " + s);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

}

class TimelineAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private LayoutInflater inflater;
    private SliderLayout mSlider;
    private AsyncTask<Void, Void, Void> task;
    private double summary = 0;
    private ArrayList<HashMap<String, String>> sList;
    DatabaseReference dbRef;
    private String orderid;
    private HashMap<String, String> userdetail;
    private FirebaseDatabase database;

    public TimelineAdapter(Activity a, ArrayList<HashMap<String, String>> d, DatabaseReference dref, HashMap<String, String> udt) {
        this.database = FirebaseDatabase.getInstance();
        this.data = d;
        this.activity = a;
        this.dbRef = dref;
        this.userdetail = udt;
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

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = null;
        if (convertView == null) {
            LayoutInflater inflater = (this.activity).getLayoutInflater();
            row = inflater.inflate(R.layout.incommingorder, parent, false);
        } else {
            row = convertView;
        }


        final HashMap<String, String> tmp = this.data.get(position);
        String orderno = tmp.get(ListItem.KEY_ORDERORDERNO);
        String ordertime = tmp.get(ListItem.KEY_ORDERCREATEDATE);
        orderid = tmp.get(ListItem.KEY_ORDERID);
        String s = tmp.get(ListItem.KEY_ORDERDETAILJSON);

        LinearLayout listOrderLayout = (LinearLayout) row.findViewById(R.id.listOrderLayout);

        TextView txt_ordertime = (TextView) row.findViewById(R.id.txt_ordertime);
        TextView txtsummary = (TextView) row.findViewById(R.id.txtsummary);

        ListView incommingorder_list = (ListView) row.findViewById(R.id.listOrder);

        Button btn_more = (Button) row.findViewById(R.id.btn_more);
        Button btn_accept = (Button) row.findViewById(R.id.btn_accept);
        Button btn_cancel = (Button) row.findViewById(R.id.btn_cancel);
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sList = new ArrayList<HashMap<String, String>>();
                acceptorder();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("ยืนยันการปฏิเสธ");
                // alert.setMessage("Message");

                alert.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sList = new ArrayList<HashMap<String, String>>();
                        rejectorder();
                    }
                });
                alert.setNegativeButton("ยกเลิก",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();
            }
        });
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, OrderdetailActivity.class);
                intent.putExtra("obj", tmp);
                intent.putExtra("mode", "");

                activity.startActivityForResult(intent, 93);
            }
        });


        summary = 0;
        this.sList = new ArrayList<HashMap<String, String>>();
        try {
            final JSONObject jsonObj = new JSONObject(s);
            if (!jsonObj.get("result").toString().equals("")) {
                JSONArray obj = (JSONArray) jsonObj.get("result");
                for (int i = 0; i < obj.length(); i++) {
                    String amount = obj.getJSONObject(i).getString("amount");
                    String moredetail = obj.getJSONObject(i).getString("moredetail");
                    String title = obj.getJSONObject(i).getString("title");
                    String img = obj.getJSONObject(i).getString("img");
                    String price = obj.getJSONObject(i).getString("price");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_ORDERDETAILTITLE, title);
                    map.put(ListItem.KEY_ORDERDETAILIMAGE, img);
                    map.put(ListItem.KEY_ORDERDETAILAMOUNT, amount);
                    map.put(ListItem.KEY_ORDERDETAILMOREDETAIL, moredetail);
                    map.put(ListItem.KEY_ORDERDETAILPRICE, price);

                    summary += (Double.parseDouble(amount) * Double.parseDouble(price));
                    sList.add(map);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String xsummary = new DecimalFormat("#,###.00").format(summary).toString();
        txtsummary.setText("ราคารวม " + xsummary + " บาท");
        BaseAdapter incomminglist_adpt = new incommingorder_list_adpt(activity, listOrderLayout, sList);
        incommingorder_list.setAdapter(incomminglist_adpt);


        TextView txt_title = (TextView) row.findViewById(R.id.txt_title);
        txt_title.setText("Incomming Order #" + orderno);
        txt_ordertime.setText(DatetimeHelper.convertDate(ordertime));


        return row;
    }

    private void acceptorder() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    setaccept();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Toast.makeText(activity, "ส่งการแจ้งเตือนไปยังลูกค้าแล้ว", Toast.LENGTH_SHORT).show();
            }

        };
        this.task.execute((Void[]) null);
    }

    private void setaccept() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(activity.getString(R.string.apiaddress) + "api/setacceptorder");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("orderid", orderid));
            nameValuePairs.add(new BasicNameValuePair("deliverytime", userdetail.get(KEY_ORDERTIME)));
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
            Log.d("accept", String.valueOf(s));
            if (!jsonObj.get("result").toString().equals("")) {
                JSONObject obj = (JSONObject) jsonObj.get("result");
                String buyeruserid = (String) obj.get("buyeruserid");
                DatabaseReference dbRef = database.getReference("buyerusers");
                dbRef.child(buyeruserid).child("updatedate").setValue(ServerValue.TIMESTAMP);
                dbRef = database.getReference("shops");
                dbRef.child(userdetail.get(KEY_SHOPID)).child("updatedate").setValue(ServerValue.TIMESTAMP);
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


    private void rejectorder() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    setreject();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Toast.makeText(activity, "ส่งการแจ้งเตือนไปยังลูกค้าแล้ว", Toast.LENGTH_SHORT).show();
            }

        };
        this.task.execute((Void[]) null);
    }

    private void setreject() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(activity.getString(R.string.apiaddress) + "api/setrejectorder");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("orderid", orderid));
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

            if (!jsonObj.get("result").toString().equals("")) {
                JSONObject obj = (JSONObject) jsonObj.get("result");
                String buyeruserid = (String) obj.get("buyeruserid");
                DatabaseReference dbRef = database.getReference("buyerusers");
                dbRef.child(buyeruserid).child("updatedate").setValue(ServerValue.TIMESTAMP);
                dbRef = database.getReference("shops");
                dbRef.child(userdetail.get(KEY_SHOPID)).child("updatedate").setValue(ServerValue.TIMESTAMP);
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


}
