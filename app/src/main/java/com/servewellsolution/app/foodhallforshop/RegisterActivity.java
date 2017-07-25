package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class RegisterActivity extends Activity {
    private ProgressDialog dialog;
    private AsyncTask<Void, Void, Void> task;
    EditText txtpassword, txtpassword2;
    EditText txtemail;
    EditText txttel;

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dialog = new ProgressDialog(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        TextView linkforogtpassword = (TextView) findViewById(R.id.linkforogtpassword);
        SpannableString content = new SpannableString("ลืมรหัสผ่าน?");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linkforogtpassword.setText(content);

        linkforogtpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), FogotPassword.class);
                startActivity(intent);
            }
        });

        TextView linklogin = (TextView) findViewById(R.id.linklogin);
        content = new SpannableString("ล๊อคอินเข้าสู่ระบบ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linklogin.setText(content);
        linklogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        this.txtpassword = (EditText) findViewById(R.id.txtpassword);
        this.txtpassword2 = (EditText) findViewById(R.id.txtpassword2);
        this.txtemail = (EditText) findViewById(R.id.txtemail);
        this.txttel = (EditText) findViewById(R.id.txttel);

        Button id_submit_button = (Button) findViewById(R.id.id_submit_button);
        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtpassword.getText().toString().equals("")
                        && !txtemail.getText().toString().equals("")
                        && !txttel.getText().toString().equals("")) {


                    if (isValidEmail(txtemail.getText().toString())) {
                        if (txtpassword.getText().toString().equals(txtpassword2.getText().toString())) {
                            registeruser();
                        } else {
                            Toast.makeText(getBaseContext(), "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "กรุณากรอกอีเมลล์ให้ถูกต้อง", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getBaseContext(), "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void registeruser() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMessage("กรุณารอสักครู่...");
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();

            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    register();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                dialog.hide();
                Toast.makeText(getBaseContext(), "สมัครสมาชิกเรียบร้อยแล้วกรุณาล๊อกอินเข้าสู่ระบบ", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 3 * 1000);
            }

        };
        this.task.execute((Void[]) null);
    }

    private void register() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/user_register");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("password", txtpassword.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("tel", txttel.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("email", txtemail.getText().toString().trim()));
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

            JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "ResponseAll: " + jsonObj);
            Log.d("Response", "ResponseJson: " + jsonObj.get("result"));


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

