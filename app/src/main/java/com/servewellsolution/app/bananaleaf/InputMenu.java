package com.servewellsolution.app.bananaleaf;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.servewellsolution.app.bananaleaf.Adapter.ListItem;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import static com.servewellsolution.app.bananaleaf.SessionManagement.KEY_SHOPID;

public class InputMenu extends AppCompatActivity {
    EditText txttitle;
    EditText txtprice;
    Button id_submit_button;
    Button id_remove_button;
    LinearLayout frameupload;
    FrameLayout frameimagetemp;
    private static final int SELECT_PICTURE = 0;
    private HashMap<String, String> userdetail;
    private ProgressDialog dialog;
    Bitmap bitmap = null;
    private AsyncTask<Void, Void, Void> task;
    String img = "";
    int itemid = 0;
    int isdelete = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inputmenu);
        SessionManagement sess = new SessionManagement(getApplicationContext());

        this.userdetail = sess.getUserDetails();
        this.setTitle("เพิ่ม/แก้ไข");
        this.dialog = new ProgressDialog(this);
        txttitle = (EditText) findViewById(R.id.txttitle);

        txtprice = (EditText) findViewById(R.id.txtprice);

        frameupload = (LinearLayout) findViewById(R.id.frameupload);

        id_submit_button = (Button) findViewById(R.id.id_submit_button);
        id_remove_button = (Button) findViewById(R.id.id_remove_button);

        id_remove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("คุณต้องการลบรายการนี้?");

                alert.setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        isdelete = 1;
                        updateitems();
                    }
                });
                alert.setNegativeButton("เอาไว้ทีหลัง",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();
            }
        });


        frameimagetemp = (FrameLayout) findViewById(R.id.frameimagetemp);

        frameupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto(v);
            }
        });

        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txttitle.getText().toString().equals("") && !txtprice.getText().toString().equals("")) {
                    updateitems();

                } else {
                    Toast.makeText(getBaseContext(), "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("obj")) {
            HashMap<String, String> tmp = (HashMap<String, String>) intent.getSerializableExtra("obj");
            Log.d("tmp", tmp + "");
            this.itemid = Integer.parseInt(tmp.get(ListItem.KEY_ITEMID));
            String title = tmp.get(ListItem.KEY_ITEMTITLE);
            String price = tmp.get(ListItem.KEY_ITEMPRICE);
            this.img = tmp.get(ListItem.KEY_ITEMIMAGE);

            txttitle.setText(title);
            txtprice.setText(price);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            if ((frameimagetemp).getChildCount() > 0) {
                (frameimagetemp).removeAllViews();
            }
            frameimagetemp.addView(imageView);
            Picasso.with(this).load(getString(R.string.imageaddress) + this.img).into(imageView);

            if (this.itemid != 0) {
                id_remove_button.setVisibility(View.VISIBLE);
            }
        }

    }


    private void updateitems() {
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
                    executeMultipartPost(bitmap);
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.hide();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "OK");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

        };
        this.task.execute((Void[]) null);
    }


    public void executeMultipartPost(Bitmap bm) throws Exception {
        try {
            ByteArrayBody bab = null;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest;
            postRequest = new HttpPost(getString(R.string.apiaddress) + "api/upload_images_item");

            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            if (bm != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] data = bos.toByteArray();
                bab = new ByteArrayBody(data, "img.png");
                reqEntity.addPart("fileUpload", bab);
            }

            ContentType contentType = ContentType.create(
                    HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
            reqEntity.addPart("itemid", new StringBody(itemid + ""));
            reqEntity.addPart("txttitle", new StringBody(txttitle.getText().toString(), contentType));
            reqEntity.addPart("txtprice", new StringBody(txtprice.getText().toString()));
            reqEntity.addPart("shopid", new StringBody(userdetail.get(KEY_SHOPID).toString()));
            reqEntity.addPart("txtimg", new StringBody(this.img));
            reqEntity.addPart("isdelete", new StringBody(this.isdelete + ""));


            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            Log.d("Response", "Response: " + s);
            JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "ResponseObj: " + jsonObj.get("FileName"));


        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            beginCrop(selectedImage);
        }

        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }

    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));

        Crop.of(source, destination).withAspect(400, 400).withMaxSize(600, 600).start(this);
    }


    private void handleCrop(int resultCode, Intent result) {
        Log.d("resultView", "" + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Crop.getOutput(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("bitmap", bitmap + "");

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            if ((frameimagetemp).getChildCount() > 0) {
                (frameimagetemp).removeAllViews();
            }
            frameimagetemp.addView(imageView);


        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void pickPhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        try {

            i.putExtra("return-data", true);
            this.startActivityForResult(
                    Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }


    }


}
