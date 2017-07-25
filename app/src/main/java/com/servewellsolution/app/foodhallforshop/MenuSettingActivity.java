package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

public class MenuSettingActivity extends AppCompatActivity {

    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private MenuAdapter listAdpt;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton fab;
    private ProgressDialog progress;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menusetting);
        SessionManagement sess = new SessionManagement(getApplicationContext());
        this.userdetail = sess.getUserDetails();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String position = intent.getStringExtra("position");
        Log.d("position", position);
        this.setTitle("เพิ่ม/แก้ไข รายการอาหาร");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        this.progress = new ProgressDialog(this);


        this.listview = (ListView) findViewById(R.id.listView);


        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> tmp = sList.get(position);

                Intent newActivity = new Intent(getBaseContext(), InputMenu.class);
                newActivity.putExtra("obj", tmp);
                startActivityForResult(newActivity, 1000);
            }
        });

        this.mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //this.listview.setDivider(null);
        //this.listview.setDividerHeight(0);

        this.sList = new ArrayList<HashMap<String, String>>();

        this.fab = (FloatingActionButton) this.findViewById(R.id.fab);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newActivity = new Intent(getBaseContext(), InputMenu.class);
                newActivity.putExtra("Activity", "Add");
                startActivityForResult(newActivity, 1000);
            }
        });


        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        this.firstload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                switch (result) {
                    case "OK":
                        progress.setTitle("Loading");
                        progress.setMessage("กรุณารอสักครู่...");
                        progress.show();

                        firstload();


                        progress.dismiss();
                        break;
                }
            }
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void bindList() {
        this.listAdpt = new MenuAdapter(this, this.sList);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);

    }

    private void firstload() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sList = new ArrayList<HashMap<String, String>>();
                progress.setMessage("กรุณารอสักครู่...");
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();

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
                progress.dismiss();
            }

        };
        this.task.execute((Void[]) null);
    }


    private void loadItemList() {


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
                    map.put(ListItem.KEY_ITEMID, id);
                    map.put(ListItem.KEY_ITEMTITLE, title);
                    map.put(ListItem.KEY_ITEMPRICE, price);
                    map.put(ListItem.KEY_ITEMIMAGE, img);
                    sList.add(map);
                }

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}


class MenuAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private static LayoutInflater inflater = null;

    public MenuAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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

        return convertView;
    }


}