package com.rajeshsaini.dmr.demo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rajeshsaini.dmr.demo.credential.MySharedPreferences;
import com.rajeshsaini.dmr.demo.R;
import com.rajeshsaini.dmr.demo.credential.Admin;
import com.rajeshsaini.dmr.demo.credential.Active;
import com.rajeshsaini.dmr.demo.models.User;
import com.rajeshsaini.dmr.demo.netwok.MultipartUtility;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class Profile extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;
    ImageView user_profile_pic;
    TextView user_profile_name, user_profile_mobile, user_profile_email, user_profile_address, user_profile_dob, user_profile_gender;
    ProgressBar progressBar;
    private JSONObject object;
    Active active;
    private void init() {
        active = Active.getInstance(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        user_profile_pic = (ImageView) findViewById(R.id.user_profile_pic);
        user_profile_name = (TextView) findViewById(R.id.user_profile_name);
        user_profile_mobile = (TextView) findViewById(R.id.user_profile_mobile);
        user_profile_email = (TextView) findViewById(R.id.user_profile_email);
        user_profile_gender = (TextView) findViewById(R.id.user_profile_gender);
        user_profile_dob = (TextView) findViewById(R.id.user_profile_dob);
        user_profile_address = (TextView) findViewById(R.id.user_profile_address);

        HashMap<String, String> params = new HashMap<String, String>();
//        params.put(Admin.USER_ID, MySharedPreferences.getSharedPreferences(this,Admin.USER_ID));
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.change_profile) {
            // Create intent to Open Image applications like Gallery, Google Photos
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        }else if(id == R.id.edit_profile){
            startActivity(new Intent(Profile.this, ProfileEdit.class));
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
    }

    private class UpdateProfile extends AsyncTask<String, String, String> {
        private Bitmap selectedProfile;
        private String name;

        public UpdateProfile(Bitmap selectedProfile, String name) {
            this.selectedProfile = scaleDown(selectedProfile, 480.0f, true);
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            user_profile_pic.setAlpha(.5f);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            user_profile_pic.setAlpha(1.0f);
        }

        @Override
        protected String doInBackground(String... params) {
            User user = active.getUser();
            String url = Admin.getUploadServerImage();
            try {
                MultipartUtility multipart = new MultipartUtility(url);
                multipart.addHeaderField("User-Agent", "CodeJava");
                multipart.addHeaderField("Test-Header", "Header-Value");
                multipart.addFormField(Admin.USER_ID, active.getUser().getId());
                multipart.addFilePartBitmap(Admin.USER_PIC, name, selectedProfile);
                return multipart.finishString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PROFILE", e.getMessage() + "");
            }
            return "";
        }
    }

    public Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    private class MyProfile extends AsyncTask<HashMap<String, String>, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(HashMap<String, String>... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(Admin.getMyProfile());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("User-Agent", Admin.USER_AGENT);
                urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                urlConnection.setDoOutput(true);
                StringBuilder stringBuilder = new StringBuilder();
                if (params[0].size() > 0) {
                    Iterator<String> iterator = (params[0].keySet()).iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        stringBuilder.append(key);
                        stringBuilder.append("=");
                        stringBuilder.append(params[0].get(key));
                        stringBuilder.append("&");
                    }
                }
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(stringBuilder.toString());
                wr.flush();
                wr.close();
                int statusCode = urlConnection.getResponseCode();
                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    object = new JSONObject(response.toString());
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("Ex", e.getLocalizedMessage());
                Log.d("Ex", e.toString());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            try {
                if (object != null) {
                    if (object.has(Admin.SUCCESS)) {
                        int success = object.getInt(Admin.SUCCESS);
                        if (success == 1) {
                            if (object.has(Admin.USER_NAME)) {
                                user_profile_name.setText(object.getString(Admin.USER_NAME));
                            }
                            if (object.has(Admin.MOBILE)) {
                                user_profile_mobile.setText(object.getString(Admin.MOBILE));
                            }
                            if (object.has(Admin.EMAIL)) {
                                user_profile_email.setText(object.getString(Admin.EMAIL));
                            }
                            if (object.has(Admin.GENDER)) {
                                user_profile_gender.setText(object.getString(Admin.GENDER));
                            }
                            if (object.has(Admin.DOB)) {
                                user_profile_dob.setText(object.getString(Admin.DOB));
                            }
                            if (object.has(Admin.ADDRESS)) {
                                user_profile_address.setText(object.getString(Admin.ADDRESS));
                            }
                            if (object.has(Admin.IMAGE)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String image = Admin.getImagePath(object.getString(Admin.IMAGE));
                                            Picasso.with(Profile.this).load(image)
                                                    .error(R.mipmap.ic_account_box_white_48dp)
                                                    .placeholder(R.mipmap.ic_account_box_white_48dp)
                                                    .into(user_profile_pic);
                                        } catch (JSONException ee) {

                                        }
                                    }
                                });
                            }
                        } else {

                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,
                proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == this.RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                /*
                if (profile_image != null)
                    profile_image.setImageBitmap(bitmap);
                */
                String s = getRealPathFromURI(filePath);
                String name = s.substring(s.lastIndexOf("/") + 1);
                Bitmap selectedProfile = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                if (selectedProfile != null) {
                    if (!active.isLogin()) {
                        return;
                    }
                    user_profile_pic.setImageBitmap(selectedProfile);
                    new UpdateProfile(selectedProfile, name).execute();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUri = null;
    public static final int RESULT_LOAD_IMG = 1;

    /**********
     * File Path
     *************/
    final String uploadFilePath = "/storage/sdcard0/";
    final String uploadFileName = "temp_photo.jpg";
    String imgDecodableString = "";

    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    + uploadFilePath + "" + uploadFileName);

            runOnUiThread(new Runnable() {
                public void run() {
                    //messageText.setText("Source File not exist :"+uploadFilePath + "" + uploadFileName);
                }
            });

            return 0;

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(Admin.getUploadServerImage());

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Admin.USER_ID);
                stringBuilder.append("=");
                stringBuilder.append(MySharedPreferences.getSharedPreferences(this, Admin.USER_ID));
                stringBuilder.append("&");
                dos.writeBytes(stringBuilder.toString());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    + " http://www.dmrsaini.somee.com/upload/"
                                    + uploadFileName + " Download here";

                            //messageText.setText(msg);
                            //messageText.setText("File Upload Completed");

                            Toast.makeText(Profile.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(Profile.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(Profile.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file ", "Exception : "
                        + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }
}
