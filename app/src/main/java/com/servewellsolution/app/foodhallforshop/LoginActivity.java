package com.servewellsolution.app.foodhallforshop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
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

import static com.servewellsolution.app.foodhallforshop.SessionManagement.*;
import static com.servewellsolution.app.foodhallforshop.SessionManagement.PREF_NAME;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class LoginActivity extends Activity {
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;
    private EditText txtemail;
    private EditText txtpassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dialog = new ProgressDialog(this);

        Button id_submit_button = (Button) findViewById(R.id.id_submit_button);
        TextView linkregister = (TextView) findViewById(R.id.linkregister);
        SpannableString content = new SpannableString("สมัครสมาชิกเพื่อใช้บริการ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linkregister.setText(content);
        linkregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();

        TextView linkforogtpassword = (TextView) findViewById(R.id.linkforogtpassword);
        content = new SpannableString("ลืมรหัสผ่าน?");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linkforogtpassword.setText(content);

        linkforogtpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), FogotPassword.class);
                startActivity(intent);
            }
        });


        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtemail = (EditText) findViewById(R.id.txtemail);
                txtpassword = (EditText) findViewById(R.id.txtpassword);
                if (txtemail.getText().toString().equals("") || txtpassword.getText().toString().equals("")) {
                    Snackbar.make(v, "กรุณาระบุ Username และ Password", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {

                    dialog.setMessage("กรุณารอสักครู่...");
                    dialog.setCancelable(false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.show();

                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/user_login");

                    try {
                        // Add your data
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("email", txtemail.getText().toString().trim()));
                        nameValuePairs.add(new BasicNameValuePair("password", txtpassword.getText().toString().trim()));
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
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //create login session
                                    editor.putString(IS_LOGIN, "1");
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
                                        editor.putString(KEY_SHOPIMG, getString(R.string.imageaddress) + obj.get("img").toString());
                                        editor.putString(KEY_USERNAME, obj.get("email").toString());
                                        editor.putString(KEY_SHOPID, obj.get("shopid").toString());

                                        editor.commit();
                                    } catch (JSONException e) {
                                        dialog.dismiss();
                                        e.printStackTrace();
                                    }
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    }, 1 * 1000);
                                }
                            }, 3 * 1000);

                        } else {
                            Toast.makeText(getBaseContext(), "อีเมลล์หรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
                        }


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

//
//
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        public void run() {
//                            //create login session
//                            editor.putString(IS_LOGIN, "1");
//                            editor.putString(KEY_USERID, "01");
//                            editor.putString(KEY_ISSHOPOPEN, "true");
//                            editor.putString(KEY_SHOPLAT, "13.769955");
//                            editor.putString(KEY_SHOPLNG, "100.607932");
//                            editor.putString(KEY_SHOPRADIUS, "0.2");
//                            editor.putString(KEY_MINPRICE, "0");
//                            editor.putString(KEY_MINAMOUNT, "3");
//                            editor.putString(KEY_ORDERTIME, "1");
//                            editor.putString(KEY_SHOPNAME, "อาหารลดน้ำหนัก/อาหารคลีน");
//                            editor.putString(KEY_SHOPIMG, "http://wm.thaibuffer.com/o/u/annchawan/women4/14808.jpg");
//                            editor.putString(KEY_USERNAME, txtemail.getText().toString());
//
//                            editor.commit();
//                            dialog.hide();
//
//                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                        }
//                    }, 3000);


                }


            }
        });


    }
}