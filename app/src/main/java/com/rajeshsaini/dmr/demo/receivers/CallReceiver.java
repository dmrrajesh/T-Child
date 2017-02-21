package com.rajeshsaini.dmr.demo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.rajeshsaini.dmr.demo.services.SyncCallLogsALL;
import com.rajeshsaini.dmr.demo.services.SyncCallLogsOne;

/**
 * Created by rajesh on 3/12/2016.
 */
public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state == null) {
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            /*
            Intent intent1 = new Intent(context.getApplicationContext(),SentCallInfo.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent1);
            */
            context.startService(new Intent(context, SyncCallLogsOne.class));
        }
    }
}
