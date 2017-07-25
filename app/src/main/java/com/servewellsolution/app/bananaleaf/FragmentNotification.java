package com.servewellsolution.app.bananaleaf;

import android.app.Activity;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by Breeshy on 9/4/2016 AD.
 */

public class FragmentNotification extends Fragment {
    RelativeLayout id_nothingmsg;
    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private HashMap<String, String> userdetail;
    private NotificationAdapter listAdpt;
    private String orderdetailjson;
    public FragmentNotification() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentNotification newInstance() {
        FragmentNotification fragment = new FragmentNotification();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        SessionManagement sess = new SessionManagement(getContext());
        this.id_nothingmsg = (RelativeLayout) rootView.findViewById(R.id.id_nothingmsg);
        this.listview = (ListView) rootView.findViewById(R.id.listView);
        this.userdetail = sess.getUserDetails();
//        this.id_nothingmsg.setVisibility(View.VISIBLE);

        this.mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        this.listview.setDivider(null);
        this.listview.setDividerHeight(0);

        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> tmp = sList.get(position);
                Log.d("pos", "Respons: " + tmp);
                if(tmp.get(ListItem.KEY_ORDERORDERNO) != "null"){
                    Intent intent = new Intent(getActivity(), OrderdetailActivity.class);
                    intent.putExtra("obj", tmp);
                    intent.putExtra("mode", "view");
                    startActivity(intent);
                }
            }
        });

        this.firstload();
        return rootView;
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
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getnotification");

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
                    String id = obj.getJSONObject(i).getString("id");
                    String orderid = obj.getJSONObject(i).getString("orderid");
                    String title = obj.getJSONObject(i).getString("title");
                    String message = obj.getJSONObject(i).getString("message");
                    String notidate = obj.getJSONObject(i).getString("notidate");
                    String ref = obj.getJSONObject(i).getString("ref");

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
                    map.put(ListItem.KEY_NOTIID, id);
                    map.put(ListItem.KEY_ORDERID, orderid);
                    map.put(ListItem.KEY_NOTILTITLE, title);
                    map.put(ListItem.KEY_NOTILMESSAGE, message);
                    map.put(ListItem.KEY_NOTILREF, ref);
                    map.put(ListItem.KEY_NOTILCREATEDATE, notidate);

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

                    loadOrderdetail(orderid);
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

    private void bindList() {
        if (this.sList.size() > 0) {
            this.id_nothingmsg.setVisibility(View.GONE);
        } else {
            this.id_nothingmsg.setVisibility(View.VISIBLE);
        }
        this.listAdpt = new NotificationAdapter(this.getActivity(), this.sList);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);
    }
}

class NotificationAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private static LayoutInflater inflater = null;
    private ArrayList<HashMap<String, String>> sList;

    public NotificationAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        this.data = d;
        this.activity = a;
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

        HashMap<String, String> tmp = this.data.get(position);

        String id = tmp.get(ListItem.KEY_NOTIID);
        String title = tmp.get(ListItem.KEY_NOTILTITLE);
        String message = tmp.get(ListItem.KEY_NOTILMESSAGE);
        String ref = tmp.get(ListItem.KEY_NOTILREF);
        String createdate = tmp.get(ListItem.KEY_NOTILCREATEDATE);
        convertView = inflater.inflate(R.layout.notificationlist, parent, false);

        TextView txttitle = (TextView) convertView.findViewById(R.id.txttitle);
        TextView txt_time = (TextView) convertView.findViewById(R.id.txt_time);
        txttitle.setText(message);
        txt_time.setText(DatetimeHelper.convertDate(createdate));
        return convertView;
    }


}
