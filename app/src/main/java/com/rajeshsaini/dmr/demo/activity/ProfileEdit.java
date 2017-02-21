package com.rajeshsaini.dmr.demo.activity;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;

import com.rajeshsaini.dmr.demo.R;
import com.rajeshsaini.dmr.demo.credential.Admin;
import com.rajeshsaini.dmr.demo.credential.Active;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class ProfileEdit extends AppCompatActivity {
    Active active;
    private EditText update_name, update_email, update_dob, update_address;
    private RadioButton gender_male, gender_female;
    private DatePickerDialog fromDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private ScrollView scrollView;
    private void init() {
        active = Active.getInstance(this);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                update_dob.setText(dateFormatter.format(newDate.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.activity_profile_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        update_name = (EditText) findViewById(R.id.update_name);
        update_email = (EditText) findViewById(R.id.update_email);
        update_dob = (EditText) findViewById(R.id.update_dob);
        update_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.show();
            }
        });

        update_address = (EditText) findViewById(R.id.update_address);
        gender_male = (RadioButton) findViewById(R.id.gender_male);
        gender_female = (RadioButton) findViewById(R.id.gender_female);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile_update) {
            actionProfileUpdate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void actionProfileUpdate(){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        params.put(Admin.USER_NAME, update_name.getText().toString());
        params.put(Admin.EMAIL, update_email.getText().toString());
        params.put(Admin.DOB, update_dob.getText().toString());
        params.put(Admin.ADDRESS, update_address.getText().toString());
        params.put(Admin.GENDER, gender_male.isChecked() ? "Male" : "Female");
        new ProfileUpdate().execute(params);
    }
    private class ProfileUpdate extends AsyncTask<HashMap<String, String>, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(HashMap<String, String>... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(Admin.getProfileUpdate());
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
                    Log.d("Res", response.toString());

                    try {
                        JSONObject object = new JSONObject(response.toString());
                        if (object.has(Admin.SUCCESS)) {
                            int success = object.getInt(Admin.SUCCESS);
                            if (success == 1) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    } catch (Exception e) {
                    }
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
            if (integer == 1) {
                Snackbar.make(scrollView.getRootView(), "Profile Successfully update!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(scrollView.getRootView(), "Profile update failed!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private class MyProfile extends AsyncTask<HashMap<String, String>, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(HashMap<String, String>... params) {
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
                    return new JSONObject(response.toString());
                } else {
                    result = 0; //"Failed to fetch data!";
                    return null;
                }
            } catch (Exception e) {
                Log.d("Ex", e.getLocalizedMessage());
                Log.d("Ex", e.toString());
            }
            return null; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            super.onPostExecute(object);
            try {
                if (object != null) {
                    if (object.has(Admin.SUCCESS)) {
                        int success = object.getInt(Admin.SUCCESS);
                        if (success == 1) {
                            if (object.has(Admin.USER_NAME)) {
                                update_name.setText(object.getString(Admin.USER_NAME));
                            }
                            if (object.has(Admin.EMAIL)) {
                                update_email.setText(object.getString(Admin.EMAIL));
                            }
                            if (object.has(Admin.GENDER)) {

                                String gen = object.getString(Admin.GENDER);
                                if(gen.equalsIgnoreCase("Male")){
                                    gender_male.setChecked(true);
                                }else if(gen.equalsIgnoreCase("Female")){
                                    gender_female.setChecked(true);
                                }
                                //    update_gender.setText(object.getString(Admin.GENDER));
                            }
                            if (object.has(Admin.DOB)) {
                                update_dob.setText(object.getString(Admin.DOB));
                            }
                            if (object.has(Admin.ADDRESS)) {
                                update_address.setText(object.getString(Admin.ADDRESS));
                            }
                        } else {

                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }
}
