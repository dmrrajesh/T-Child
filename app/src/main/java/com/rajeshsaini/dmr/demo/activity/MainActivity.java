package com.rajeshsaini.dmr.demo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rajeshsaini.dmr.demo.credential.MySharedPreferences;
import com.rajeshsaini.dmr.demo.R;
import com.rajeshsaini.dmr.demo.credential.Admin;
import com.rajeshsaini.dmr.demo.adapter.NavItemAdapter;
import com.rajeshsaini.dmr.demo.credential.Active;
import com.rajeshsaini.dmr.demo.credential.Schema;
import com.rajeshsaini.dmr.demo.custom.Utils2;
import com.rajeshsaini.dmr.demo.fragment.ViewDetail;
import com.rajeshsaini.dmr.demo.models.NavItem;
import com.rajeshsaini.dmr.demo.services.LocationUpdateService;
import com.rajeshsaini.dmr.demo.services.SyncCallLogsALL;
import com.rajeshsaini.dmr.demo.services.SyncMessageAll;
import com.rajeshsaini.dmr.demo.volly.DMRRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ViewDetail.OnViewDetailListener {
    private static final String TAG = MainActivity.class.getName();
    Active active;
    Gson gson;
    private List<NavItem> navItems;
    private ListView navItemView;
    private ImageView parent_imageView, addChild;
    private TextView parent_user_name, parent_user_mobile;
    public static Handler handler = new Handler();
    public static Handler handler1 = new Handler();
    public static final int PERMISSIONS_REQUEST_READ_CALL_LOG = 100;
    public static final int PERMISSIONS_REQUEST_WRITE_CALL_LOG = 101;
    private DMRRequest dmrRequest;

    private void init() {
        active = Active.getInstance(this);
        gson = new Gson();
        dmrRequest = DMRRequest.getInstance(this, TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parent_imageView = (ImageView) findViewById(R.id.parent_imageView);
        parent_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        });
        parent_user_name = (TextView) findViewById(R.id.parent_user_name);
        parent_user_mobile = (TextView) findViewById(R.id.parent_mobile_number);

        if (MySharedPreferences.getSharedPreferences(this, Admin.MOBILE).length() > 0) {
            parent_user_mobile.setText(MySharedPreferences.getSharedPreferences(this, Admin.MOBILE));
        }

        addChild = (ImageView) findViewById(R.id.add_child);
        addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChildAdd.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
        new MyChilds().execute(params);

        if(checkPermission()) {
            startService(new Intent(this, SyncCallLogsALL.class));
        }
        startService(new Intent(this, SyncMessageAll.class));
        startService(new Intent(this,LocationUpdateService.class));
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CALL_LOG);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CALL_LOG}, PERMISSIONS_REQUEST_WRITE_CALL_LOG);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_requests);
        LayerDrawable icon = (LayerDrawable) item.getIcon();

        // Update LayerDrawable's BadgeDrawable
        Utils2.setBadgeCount(this, icon, 2);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            MySharedPreferences.logOut(this);
            active.setLogOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        if (id == R.id.action_requests) {
            startActivity(new Intent(MainActivity.this, ParentRequest.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, ViewDetail.newInstance(), ViewDetail.TAG).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
        new MyChilds().execute(params);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Admin.USER_ID, active.getUser().getId());
        new MyProfile().execute(params);
        new MyChilds().execute(params);
    }

    @Override
    public void onViewDetail(Uri uri) {

    }

    @Override
    public void onStart() {
        super.onStart();
     }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class NavMenuItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            navigateTo(position);
        }
    }

    private void navigateTo(int position) {
        //MySharedPreferences.setSharedPreferences(MainActivity.this, Admin.CHILD_ID, navItems.get(position).getId());
        //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, ViewDetail.newInstance(navItems.get(position).getId()), ViewDetail.TAG).commit();

        startActivity(new Intent(this,ChildInfo.class));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private class MyChilds extends AsyncTask<HashMap<String, String>, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(HashMap<String, String>... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(Admin.getAllChild());
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
                    parseResult(response.toString());
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

            } else {

            }
        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            navItems = new ArrayList<>();
            if (response.has(Schema.SUCCESS)) {

                int success = response.getInt(Schema.SUCCESS);
                if (success == 1) {
                    if (response.has(Schema.CHILD)) {
                        JSONArray jsonArray = response.getJSONArray(Schema.CHILD);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            NavItem giftCard = new NavItem();
                            if (jsonObject.has(Admin.USER_ID)) {
                                giftCard.setId(jsonObject.getString(Admin.USER_ID));
                            } else {
                                giftCard.setId("");
                            }
                            if (jsonObject.has(Schema.NAME)) {
                                giftCard.setNavTitle(jsonObject.getString(Schema.NAME));
                            } else {
                                giftCard.setNavTitle("");
                            }
                            if (jsonObject.has(Schema.MOBILE)) {
                                giftCard.setMobile(jsonObject.getString(Schema.MOBILE));
                            } else {
                                giftCard.setMobile("");
                            }
                            if (jsonObject.has(Schema.IMAGE)) {
                                giftCard.setImage(jsonObject.getString(Schema.IMAGE));
                            } else {
                                giftCard.setImage("");
                            }
                            navItems.add(giftCard);
                        }
                    }
                } else if (success == 2) {
                } else {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("MyChildEx", e.toString());
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navItemView = (ListView) navigationView.findViewById(R.id.drawer_menu_items);
                navItemView.setOnItemClickListener(new NavMenuItemClickListener());
                if (navItems != null) {
                    navItemView.setAdapter(new NavItemAdapter(getApplicationContext(), R.layout.drawer_menu_item, navItems));
                }
                navigationView.setNavigationItemSelectedListener(MainActivity.this);
            }
        });
    }

    private class LocationUpdate extends AsyncTask<HashMap<String, String>, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(HashMap<String, String>... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(Admin.getLocationUpdate());
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
                Log.d("ResponseCode", statusCode + "");
                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    final JSONObject object = new JSONObject(response.toString());
                    if (object.has(Admin.SUCCESS)) {
                        int success = object.getInt(Admin.SUCCESS);
                        if (success == 1) {
                            if (object.has(Admin.USER_NAME)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            parent_user_name.setText(object.getString(Admin.USER_NAME));
                                        } catch (Exception ee) {

                                        }
                                    }
                                });
                            }
                            if (object.has(Admin.IMAGE)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String image = Admin.getImagePath(object.getString(Admin.IMAGE));
                                            Picasso.with(MainActivity.this).load(image)
                                                    .error(R.mipmap.ic_account_box_white_48dp)
                                                    .placeholder(R.mipmap.ic_account_box_white_48dp)
                                                    .into(parent_imageView);
                                        } catch (JSONException ee) {
                                        }
                                    }
                                });
                            }
                            if (object.has(Admin.MOBILE)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            parent_user_mobile.setText(object.getString(Admin.MOBILE).length() == 10 ? object.getString(Admin.MOBILE) : MySharedPreferences.getSharedPreferences(MainActivity.this, Admin.MOBILE));
                                        } catch (Exception ee) {
                                        }
                                    }
                                });
                            }
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("LocationUpdate", e.getLocalizedMessage());
                Log.d("LocationUpdate", e.toString());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }

    private class CallUpdate extends AsyncTask<HashMap<String, String>, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(HashMap<String, String>... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(Admin.getCallUpdate());
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
                Log.d("ResponseCode", statusCode + "");
                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    final JSONObject object = new JSONObject(response.toString());
                    if (object.has(Admin.SUCCESS)) {
                        int success = object.getInt(Admin.SUCCESS);
                        if (success == 1) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("CallUpdate", e.getLocalizedMessage());
                Log.d("CallUpdate", e.toString());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
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
                    final JSONObject object = new JSONObject(response.toString());
                    if (object.has(Admin.SUCCESS)) {
                        int success = object.getInt(Admin.SUCCESS);
                        if (success == 1) {
                            if (object.has(Admin.USER_NAME)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            parent_user_name.setText(object.getString(Admin.USER_NAME));
                                        } catch (Exception ee) {

                                        }
                                    }
                                });
                            }
                            if (object.has(Admin.IMAGE)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String image = Admin.getImagePath(object.getString(Admin.IMAGE));
                                            Picasso.with(MainActivity.this).load(image)
                                                    .error(R.mipmap.ic_account_box_white_48dp)
                                                    .placeholder(R.mipmap.ic_account_box_white_48dp)
                                                    .into(parent_imageView);
                                        } catch (JSONException ee) {
                                        }
                                    }
                                });
                            }
                            if (object.has(Admin.MOBILE)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            parent_user_mobile.setText(object.getString(Admin.MOBILE).length() == 10 ? object.getString(Admin.MOBILE) : MySharedPreferences.getSharedPreferences(MainActivity.this, Admin.MOBILE));
                                        } catch (Exception ee) {
                                        }
                                    }
                                });
                            }
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("MyProfile", e.getLocalizedMessage());
                Log.d("MyProfile", e.toString());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CALL_LOG || requestCode == PERMISSIONS_REQUEST_WRITE_CALL_LOG) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "you grant the permission, we  display the names", Toast.LENGTH_SHORT).show();
                //handler1.postDelayed(runnable1, 1000);
            } else {
                Toast.makeText(this, "Until you grant the permission, we can't display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
