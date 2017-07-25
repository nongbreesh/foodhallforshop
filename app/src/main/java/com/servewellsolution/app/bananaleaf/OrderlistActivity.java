package com.servewellsolution.app.bananaleaf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.servewellsolution.app.bananaleaf.Adapter.ListItem;
import com.servewellsolution.app.bananaleaf.Helper.DatetimeHelper;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPID;

public class OrderlistActivity extends AppCompatActivity {

    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private OrderlistAdapter listAdpt;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    RelativeLayout id_nothingmsg;
    private HashMap<String, String> userdetail;
    private String orderdetailjson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.padcolor));
        setSupportActionBar(toolbar);

        this.setTitle("รายการออเดอร์รอส่ง");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        this.id_nothingmsg = (RelativeLayout) this.findViewById(R.id.id_nothingmsg);
        SessionManagement sess = new SessionManagement(getBaseContext());
        this.userdetail = sess.getUserDetails();
        this.listview = (ListView) this.findViewById(R.id.listView);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        this.listview.setDivider(null);
        this.listview.setDividerHeight(0);


        this.firstload();


    }

    private void bindList() {
        if (this.sList.size() > 0) {
            this.id_nothingmsg.setVisibility(View.GONE);
        } else {
            this.id_nothingmsg.setVisibility(View.VISIBLE);
        }
        this.listAdpt = new OrderlistAdapter(this, this.sList, this.userdetail, this);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);
    }

    public void refresh() {
        Log.d("reload", "reload");
        firstload();
    }

    private void firstload() {
        this.sList = new ArrayList<HashMap<String, String>>();
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
        //getorderlist

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getorderpendingdeliver");

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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

}


class OrderlistAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private static LayoutInflater inflater = null;
    private SliderLayout mSlider;
    private String orderid;
    private HashMap<String, String> userdetail;
    private AsyncTask<Void, Void, Void> task;
    OrderlistActivity objclass;

    public OrderlistAdapter(Activity a, ArrayList<HashMap<String, String>> d, HashMap<String, String> udt, OrderlistActivity cls) {
        this.data = d;
        this.activity = a;
        this.userdetail = udt;
        this.objclass = cls;
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
        final HashMap<String, String> tmp = this.data.get(position);

        String orderno = tmp.get(ListItem.KEY_ORDERORDERNO);
        String ordertime = tmp.get(ListItem.KEY_ORDERCREATEDATE);
        String deriverydate = tmp.get(ListItem.KEY_ORDERDERIVERYDATE);
        String deriveryrage = tmp.get(ListItem.KEY_ORDERDERIVERYRANGE);
        orderid = tmp.get(ListItem.KEY_ORDERID);
        String s = tmp.get(ListItem.KEY_ORDERDETAILJSON);


        convertView = inflater.inflate(R.layout.deliverypendinglist, parent, false);
        TextView txt_orderno = (TextView) convertView.findViewById(R.id.txt_orderno);
        TextView txt_ordertime = (TextView) convertView.findViewById(R.id.txt_ordertime);
        TextView txt_deliverytime = (TextView) convertView.findViewById(R.id.txt_deliverytime);
        Button btn_accept = (Button) convertView.findViewById(R.id.btn_accept);
        Button btn_more = (Button) convertView.findViewById(R.id.btn_more);
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, OrderdetailActivity.class);
                intent.putExtra("obj", tmp);
                intent.putExtra("mode", "view");
                activity.startActivity(intent);
            }
        });
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("ส่งออเดอร์เรียบร้อย");
                // alert.setMessage("Message");

                alert.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        confirmdelivery();
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

        txt_orderno.setText(orderno);
        txt_ordertime.setText(DatetimeHelper.convertDate(ordertime));

        if(!deriveryrage.equals("")){
            txt_deliverytime.setText("เวลาส่ง " + DatetimeHelper.convertDate2(deriverydate)+ " เวลา " + deriveryrage);
        }
        else{
            txt_deliverytime.setText("เวลาส่ง " + DatetimeHelper.convertDate(deriverydate));
        }



        return convertView;
    }

    private void confirmdelivery() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    setdelivered();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Toast.makeText(activity, "ส่งการแจ้งเตือนไปยังลูกค้าแล้ว", Toast.LENGTH_SHORT).show();
                objclass.refresh();
            }

        };
        this.task.execute((Void[]) null);
    }

    private void setdelivered() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(activity.getString(R.string.apiaddress) + "api/setcompletedorder");

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
                FirebaseDatabase database = FirebaseDatabase.getInstance();
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


