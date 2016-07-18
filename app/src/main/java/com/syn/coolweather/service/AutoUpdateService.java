package com.syn.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.syn.coolweather.receiver.AutoUpdateReceiver;
import com.syn.coolweather.util.HttpCallbackListener;
import com.syn.coolweather.util.HttpUtil;
import com.syn.coolweather.util.Utility;


/**
 * Created by 孙亚楠 on 2016/7/18.
 */
public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;//8小时的毫秒数
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateReceiver.class);
        PendingIntent pi =PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return  super.onStartCommand(intent,flags,startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode=prefs.getString("weather_code","");
        String address="http://route.showapi.com/9-7"+weatherCode+".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this,response);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();

            }
        });
    }


}
