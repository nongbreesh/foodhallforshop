package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.servewellsolution.app.foodhallforshop.Adapter.ListItem;
import com.squareup.picasso.Picasso;

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

import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_SHOPID;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_SHOPIMG;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.KEY_SHOPNAME;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.PREF_NAME;

/**
 * Created by Breeshy on 4/11/2016 AD.
 */
public class FragmentHome extends Fragment {


    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private HomeAdapter listAdpt;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;
    private LinearLayout linlaHeaderProgress;
 ;

    public FragmentHome() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public static FragmentHome newInstance() {
        FragmentHome fragment = new FragmentHome();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        SharedPreferences pref = getContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        this.setting();
        this.listview = (ListView) rootView.findViewById(R.id.listView);
        // CAST THE LINEARLAYOUT HOLDING THE MAIN PROGRESS (SPINNER)
        this.linlaHeaderProgress = (LinearLayout) rootView.findViewById(R.id.linlaHeaderProgress);


        this.mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                setting();
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //this.listview.setDivider(null);
        //this.listview.setDividerHeight(0);

        this.sList = new ArrayList<HashMap<String, String>>();

        this.firstload();

        return rootView;
    }

    private void setting() {
        SessionManagement sess = new SessionManagement(getContext());
        this.userdetail = sess.getUserDetails();
    }


    private void bindList() {
        this.listAdpt = new HomeAdapter(this.getActivity(), this.sList, this.userdetail);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);
        this.linlaHeaderProgress.setVisibility(View.GONE);

    }

    private void firstload() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                linlaHeaderProgress.setVisibility(View.VISIBLE);
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
                loaditems();
            }

        };
        this.task.execute((Void[]) null);
    }


    private void loadItemList() {
        try {
            int x = 0;
            for (int i = 0; i < 2; i++) { // 5 section
                map = new HashMap<String, String>();
                if (i == 0) {
                    map.put(ListItem.KEY_HEADER, "");
                    map.put(ListItem.KEY_INDEX, i + "");
                    map.put(ListItem.KEY_ID, i + "");
                } else {
//                    if (i == 1) {
//                        map.put(ListItem.KEY_HEADER, "ผู้สนใจ");
//                        map.put(ListItem.KEY_INDEX, i + "");
//                        map.put(ListItem.KEY_ID, i + "");
//                    } else if (i == 2) {
//                        map.put(ListItem.KEY_HEADER, "");
//                        map.put(ListItem.KEY_INDEX, i + "");
//                        map.put(ListItem.KEY_ID, i + "");
//                    }
                     if (i == 1) {
                        map.put(ListItem.KEY_HEADER, "เมนูของคุณ");
                        map.put(ListItem.KEY_INDEX, i + "");
                        map.put(ListItem.KEY_ID, i + "");
                    } else {

                    }

                }
                x++;
                sList.add(map);
            }


        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private void loaditems() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    loadItems();
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

    private void loadItems() {


        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getitemlist");

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
                Log.d("Response", "ResponseObject: " + obj);

                for (int i = 0; i < obj.length(); i++) {
                    String id = obj.getJSONObject(i).getString("id");
                    String title = obj.getJSONObject(i).getString("title");
                    String price = obj.getJSONObject(i).getString("price");
                    String img = obj.getJSONObject(i).getString("img");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_INDEX, "99");
                    map.put(ListItem.KEY_HEADER, "");
                    map.put(ListItem.KEY_ITEMID, id);
                    map.put(ListItem.KEY_ITEMTITLE, title);
                    map.put(ListItem.KEY_ITEMPRICE, price);
                    map.put(ListItem.KEY_ITEMIMAGE, img);
                    sList.add(map);
                }

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

}


class HomeAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private HashMap<String, String> userdetail;
    private Activity activity;
    private static LayoutInflater inflater = null;

    public HomeAdapter(Activity a, ArrayList<HashMap<String, String>> d, HashMap<String, String> userdetail) {
        this.data = d;
        this.activity = a;
        this.userdetail = userdetail;
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
        int index = Integer.parseInt(tmp.get(ListItem.KEY_INDEX));
        String header = tmp.get(ListItem.KEY_HEADER);
        if (index == 0) {
            convertView = inflater.inflate(R.layout.shopheader, parent, false);
            ImageView profile_header_bg = (ImageView) convertView.findViewById(R.id.circular_image_view);
            TextView txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            if (this.userdetail.get(KEY_SHOPNAME).toString().equals("")) {
                txt_title.setText("กรุณาตั้งค่าร้านค้า");
                txt_title.setTextColor(Color.parseColor("#F00000"));
            } else {
                txt_title.setText(this.userdetail.get(KEY_SHOPNAME).toString());
            }

            Picasso.with(this.activity).load(this.userdetail.get(KEY_SHOPIMG).toString()).into(profile_header_bg);

        }
//        else if (index == 1) {
//            convertView = inflater.inflate(R.layout.listheader, parent, false);
//            TextView textSeparator = (TextView) convertView.findViewById(R.id.textSeparator);
//            textSeparator.setText(header);
//        } else if (index == 2) {
//            // do add users
//            convertView = inflater.inflate(R.layout.userwaitinglist, parent, false);
//            LinearLayout holderscroll = (LinearLayout) convertView.findViewById(R.id.holderscroll);
//            RelativeLayout child = null;
//            //iterate views upto you adapater count.
//            for (int i = 0; i < 1; i++) {
//                child = (RelativeLayout) this.activity.getLayoutInflater().inflate(
//                        R.layout.userwaitingitem, null);
//
//                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                llp.setMargins(0, 0, 0, 0);
//                child.setLayoutParams(llp);
//
//                CircularImageView imguser = (CircularImageView) child.findViewById(R.id.imguser);
//                Picasso.with(this.activity).load("https://graph.facebook.com/1421016438/picture").into(imguser);
//                holderscroll.addView(child);
//            }
//
//        }
        else {
            if (header != "") {
                convertView = inflater.inflate(R.layout.listheader, parent, false);
                TextView textSeparator = (TextView) convertView.findViewById(R.id.textSeparator);
                textSeparator.setText(header);
            } else {

                int id = Integer.parseInt(tmp.get(ListItem.KEY_ITEMID));
                String title = tmp.get(ListItem.KEY_ITEMTITLE);
                String price = tmp.get(ListItem.KEY_ITEMPRICE);
                String img = tmp.get(ListItem.KEY_ITEMIMAGE);
                convertView = inflater.inflate(R.layout.itemlist, parent, false);
                ImageView imgmenu = (ImageView) convertView.findViewById(R.id.imgmenu);
                TextView txttitle = (TextView) convertView.findViewById(R.id.title);
                TextView txtprice = (TextView) convertView.findViewById(R.id.price);

                Picasso.with(this.activity).load(this.activity.getString(R.string.imageaddress) + img).into(imgmenu);
                txttitle.setText(title);
                txtprice.setText(price);
            }
        }

        return convertView;
    }


}
