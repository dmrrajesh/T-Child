package com.rajeshsaini.dmr.demo.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.rajeshsaini.dmr.demo.credential.Admin;
import com.rajeshsaini.dmr.demo.credential.Active;
import com.rajeshsaini.dmr.demo.models.MessageModel;
import com.rajeshsaini.dmr.demo.volly.DMRRequest;
import com.rajeshsaini.dmr.demo.volly.DMRResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncMessageAll extends Service {

    private static final String TAG = SyncMessageAll.class.getName();
    Active active;
    DMRRequest dmrRequest;
    String reqColumns[] = new String[14];

    private void init() {
        active = Active.getInstance(this);
        dmrRequest = DMRRequest.getInstance(this, TAG);
        reqColumns[0] = "_id";
        reqColumns[1] = "thread_id";
        reqColumns[2] = "address";
        reqColumns[3] = "person";
        reqColumns[4] = "date";
        reqColumns[5] = "protocol";
        reqColumns[6] = "read";
        reqColumns[7] = "status";
        reqColumns[8] = "type";
        reqColumns[9] = "reply_path_present";
        reqColumns[10] = "subject";
        reqColumns[11] = "body";
        reqColumns[12] = "service_center";
        reqColumns[13] = "locked";
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        init();
        requestUpdateMessage(getInbox());
        requestUpdateMessage(getSent());
        requestUpdateMessage(getDraft());
    }
    public void requestUpdateMessage(List<MessageModel> detailsList){
        for (MessageModel details : detailsList) {
            Map<String, String> map = new HashMap<>();
            map.put(Admin.USER_ID, active.getUser().getId());
            map.put(Admin.MESSAGE_ID,details.get_id());
            map.put(Admin.MESSAGE_THREAD_ID, details.getThread_id());
            map.put(Admin.MESSAGE_ADDRESS, details.getAddress());
            map.put(Admin.MESSAGE_PERSON, details.getPerson());
            map.put(Admin.MESSAGE_DATE, details.getDate());
            map.put(Admin.MESSAGE_PROTOCOL, details.getProtocol());
            map.put(Admin.MESSAGE_READ, details.getRead());
            map.put(Admin.MESSAGE_STATUS, details.getStatus());
            map.put(Admin.MESSAGE_TYPE, details.getType());
            map.put(Admin.MESSAGE_REPLY_PATH_PRESENT, details.getReply_path_present());
            map.put(Admin.MESSAGE_SUBJECT, details.getSubject());
            map.put(Admin.MESSAGE_BODY, details.getBody());
            map.put(Admin.MESSAGE_SERVICE, details.getService_center());
            map.put(Admin.MESSAGE_LOCKED, details.getLocked());
            map.put(Admin.MESSAGE_MSG_TYPE, details.getMessageType());
            dmrRequest.doPost(Admin.getMessageUpdate(), map, new DMRResult() {
                @Override
                public void onSuccess(String jsonResponse) {
                        Log.d(TAG,jsonResponse);
                    try {
                        JSONObject object = new JSONObject(jsonResponse);
                        if (object.has(Admin.SUCCESS)) {
                            if (object.getInt(Admin.SUCCESS) == 1) {
                                if (object.has(Admin.LAST)) {
                                    active.setKey(Admin.OLD_MESSAGE_UPDATE, object.getString(Admin.LAST));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private List<MessageModel> getInbox() {
        List<MessageModel> messageModels = new ArrayList<>();
        // Create Inbox box URI
        Uri inboxURI = Uri.parse("content://sms/inbox");
        // List required columns
        String[] reqCols = new String[]{"_id", "address", "body"};
        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = getContentResolver();
        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor c = cr.query(inboxURI, reqColumns, null, null, null);
        while (c.moveToNext()) {
            MessageModel model = new MessageModel();
            model.set_id(c.getString(0));
            model.setThread_id(c.getString(1));
            model.setAddress(c.getString(2));
            model.setPerson(c.getString(3));
            model.setDate(c.getString(4));
            model.setProtocol(c.getString(5));
            model.setRead(c.getString(6));
            model.setStatus(c.getString(7));
            model.setType(c.getString(8));
            model.setReply_path_present(c.getString(9));
            model.setSubject(c.getString(10));
            model.setBody(c.getString(11));
            model.setService_center(c.getString(12));
            model.setLocked(c.getString(13));
            model.setMessageType("INBOX");
            messageModels.add(model);
        }
        return messageModels;
    }

    private List<MessageModel> getSent() {
        List<MessageModel> messageModels = new ArrayList<>();
        // Create Inbox box URI
        Uri inboxURI = Uri.parse("content://sms/sent");
        // List required columns
        String[] reqCols = new String[]{"_id", "address", "body"};
        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = getContentResolver();
        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor c = cr.query(inboxURI, reqColumns, null, null, null);
        while (c.moveToNext()) {
            MessageModel model = new MessageModel();
            model.set_id(c.getString(0));
            model.setThread_id(c.getString(1));
            model.setAddress(c.getString(2));
            model.setPerson(c.getString(3));
            model.setDate(c.getString(4));
            model.setProtocol(c.getString(5));
            model.setRead(c.getString(6));
            model.setStatus(c.getString(7));
            model.setType(c.getString(8));
            model.setReply_path_present(c.getString(9));
            model.setSubject(c.getString(10));
            model.setBody(c.getString(11));
            model.setService_center(c.getString(12));
            model.setLocked(c.getString(13));
            model.setMessageType("SENT");
            messageModels.add(model);
        }
        return messageModels;
    }

    private List<MessageModel> getDraft() {
        List<MessageModel> messageModels = new ArrayList<>();
        // Create Inbox box URI
        Uri inboxURI = Uri.parse("content://sms/draft");
        // List required columns
        String[] reqCols = new String[]{"_id", "address", "body"};
        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = getContentResolver();
        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor c = cr.query(inboxURI, reqColumns, null, null, null);
        while (c.moveToNext()) {
            MessageModel model = new MessageModel();
            model.set_id(c.getString(0));
            model.setThread_id(c.getString(1));
            model.setAddress(c.getString(2));
            model.setPerson(c.getString(3));
            model.setDate(c.getString(4));
            model.setProtocol(c.getString(5));
            model.setRead(c.getString(6));
            model.setStatus(c.getString(7));
            model.setType(c.getString(8));
            model.setReply_path_present(c.getString(9));
            model.setSubject(c.getString(10));
            model.setBody(c.getString(11));
            model.setService_center(c.getString(12));
            model.setLocked(c.getString(13));
            model.setMessageType("DRAFT");
            messageModels.add(model);
        }
        return messageModels;
    }
}
