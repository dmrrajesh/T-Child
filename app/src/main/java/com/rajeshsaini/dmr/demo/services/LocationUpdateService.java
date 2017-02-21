package com.rajeshsaini.dmr.demo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.rajeshsaini.dmr.demo.credential.Active;
import com.rajeshsaini.dmr.demo.credential.Admin;
import com.rajeshsaini.dmr.demo.gps.GPSTracker;
import com.rajeshsaini.dmr.demo.models.LocationModel;
import com.rajeshsaini.dmr.demo.volly.DMRRequest;
import com.rajeshsaini.dmr.demo.volly.DMRResult;

import java.util.HashMap;
import java.util.Map;

public class LocationUpdateService extends Service {
    private static final String TAG = LocationUpdateService.class.getName();
    private Active active;
    private DMRRequest dmrRequest;
    private GPSTracker gpsTracker;
    private Handler handler;
    private static final long DURATION = 1000*60*1;
    private void init(Context context){
        active = Active.getInstance(context);
        dmrRequest=DMRRequest.getInstance(context,TAG);
        handler = new Handler();
    }

    public LocationUpdateService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        init(getApplicationContext());
        handler.postDelayed(runnable,DURATION);
    }

    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            gpsTracker = new GPSTracker(getBaseContext());
            if(gpsTracker.canGetLocation()) {
                LocationModel locationModel = new LocationModel();
                locationModel.setLatitude(gpsTracker.getLatitude()+"");
                locationModel.setLongitude(gpsTracker.getLongitude()+"");
                locationModel.setAddress(gpsTracker.getAddress());
                locationSendServer(locationModel);
                handler.postDelayed(runnable,DURATION);
            }else {
                LocationUpdateService.this.onDestroy();
            }
        }
    };

    private void locationSendServer(LocationModel locationModel){
        Map<String,String> map = new HashMap<>();
        map.put(Admin.USER_ID,active.getUser().getId());
        map.put(Admin.LATITUDE,locationModel.getLatitude());
        map.put(Admin.LONGITUDE,locationModel.getLongitude());
        map.put(Admin.ADDRESS,locationModel.getAddress());
        dmrRequest.doPost(Admin.getLocationUpdate(), map, new DMRResult() {
            @Override
            public void onSuccess(String jsonResponse) {
                Log.d(TAG,jsonResponse);
            }

            @Override
            public void onError(VolleyError volleyError) {
                Log.e(TAG,volleyError.toString());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
