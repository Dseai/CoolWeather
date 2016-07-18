package com.syn.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.syn.coolweather.service.AutoUpdateService;

/**
 * Created by 孙亚楠 on 2016/7/18.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i=new Intent(context, AutoUpdateService.class);
        context.startActivity(i);
    }
}
