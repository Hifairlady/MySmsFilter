package com.edgar.mysmsfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyRebootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent mIntent = new Intent(context, MainActivity.class);
            mIntent.putExtra("FROM_REBOOT", true);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
        }
    }
}
