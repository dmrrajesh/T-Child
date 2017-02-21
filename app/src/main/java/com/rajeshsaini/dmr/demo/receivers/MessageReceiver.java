package com.rajeshsaini.dmr.demo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.rajeshsaini.dmr.demo.services.SyncMessageOne;

/**
 * Created by rajesh on 3/12/2016.
 */

public class MessageReceiver extends BroadcastReceiver {
    final SmsManager sms = SmsManager.getDefault();
    @Override
    public void onReceive(final Context context, Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    context.startService(new Intent(context , SyncMessageOne.class));
                }catch (InterruptedException e){

                }
            }
        }).start();
    }
}
