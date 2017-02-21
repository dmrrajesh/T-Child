package com.rajeshsaini.dmr.demo.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

import com.android.volley.VolleyError;
import com.rajeshsaini.dmr.demo.credential.Admin;
import com.rajeshsaini.dmr.demo.credential.Active;
import com.rajeshsaini.dmr.demo.models.CallDetails;
import com.rajeshsaini.dmr.demo.volly.DMRRequest;
import com.rajeshsaini.dmr.demo.volly.DMRResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncCallLogsOne extends Service {
    private static final String TAG = SyncCallLogsOne.class.getName();
    Active active;
    DMRRequest dmrRequest;
    private void init() {
        active = Active.getInstance(this);
        dmrRequest = DMRRequest.getInstance(this, TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        init();
        List<CallDetails> detailsList = getLastCallDetails();
        for (CallDetails details : detailsList) {
            Map<String, String> map = new HashMap<>();
            map.put(Admin.USER_ID, active.getUser().getId());
            map.put(Admin.DATE, details.getDate());
            map.put(Admin.TIME, details.getTime());
            map.put(Admin.MOBILE, details.getMobile());
            map.put(Admin.DURATION, details.getDuration());
            map.put(Admin.TYPE, details.getType());
            map.put(Admin.LAST, details.getOldUpdate());
            dmrRequest.doPost(Admin.getCallUpdate(), map, new DMRResult() {
                @Override
                public void onSuccess(String jsonResponse) {
                    try {
                        JSONObject object = new JSONObject(jsonResponse);
                        if (object.has(Admin.SUCCESS)) {
                            if (object.getInt(Admin.SUCCESS) == 1) {
                                if (object.has(Admin.LAST)) {
                                    active.setKey(Admin.OLD_CALL_UPDATE, object.getString(Admin.LAST));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }
                @Override
                public void onError(VolleyError volleyError) {
                    Log.e(TAG, volleyError.toString());
                }
            });
        }
    }

    private List<CallDetails> getLastCallDetails() {
        List<CallDetails> detailsList = new ArrayList<>();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, null, null, strOrder);
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        while (cursor.moveToNext()) {
            CallDetails details = new CallDetails();
            String phNum = cursor.getString(number);
            details.setMobile(phNum);
            String callTypeCode = cursor.getString(type);
            String strcallDate = cursor.getString(date);
            details.setOldUpdate(strcallDate);
            Date callDate = new Date(Long.valueOf(strcallDate));
            java.sql.Date date1 = new java.sql.Date(callDate.getTime());
            java.sql.Time time = new java.sql.Time(callDate.getTime());
            details.setDate(date1.toString());
            details.setTime(time.toString());
            String callDuration = cursor.getString(duration);
            details.setDuration(callDuration);
            String callType = null;
            int callcode = Integer.parseInt(callTypeCode);
            switch (callcode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    details.setType(Admin.CALL_OUTGOING);
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    details.setType(Admin.CALL_INCOMING);
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    details.setType(Admin.CALL_MISS);
                    break;
            }
            if(active.getValue(Admin.OLD_CALL_UPDATE).equalsIgnoreCase(details.getOldUpdate())){
                break;
            }
            detailsList.add(details);
        }
        cursor.close();
        return detailsList;
    }
}
