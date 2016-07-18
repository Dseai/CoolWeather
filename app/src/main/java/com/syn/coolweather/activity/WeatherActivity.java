package com.syn.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.syn.coolweather.R;
import com.syn.coolweather.util.HttpCallbackListener;
import com.syn.coolweather.util.HttpUtil;
import com.syn.coolweather.util.Utility;

/**
 * Created by 孙亚楠 on 2016/7/18.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    //用于显示城市名
    private TextView cityNameText;
    //用于显示天气描述信息
    private TextView weatherDespText;
    //用于显示发布时间
    private  TextView publishText;
    //用于显示气温1
    private TextView temp1Text;
    //用于显示气温2
    private TextView temp2Text;
    //用于显示当前日期
    private TextView currentDateText;
    //切换城市按钮
    private Button switchCity;
    //更新天气按钮
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText=(TextView)findViewById(R.id.city_name);
        publishText=(TextView)findViewById(R.id.publish_text);
        weatherDespText=(TextView)findViewById(R.id.weather_desp);
        temp1Text=(TextView)findViewById(R.id.temp1);
        temp2Text=(TextView)findViewById(R.id.temp2);
        currentDateText=(TextView)findViewById(R.id.current_date);
        switchCity=(Button)findViewById(R.id.switch_city);
        refreshWeather=(Button)findViewById(R.id.refresh_weather);
        String countryCode=getIntent().getStringExtra("country_code");
        if(!TextUtils.isEmpty(countryCode)){
            //有县级别代号时就去查询天气
            publishText.setText("同步中。。");
            weatherInfoLayout.setVisibility(View.VISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        }else {
            showWeather();
        }
        switchCity.setOnClickListener(this);


    }






    /**
     * 查询县级代号对应的天气
     * @param countryCode
     */
    private void queryWeatherCode(String countryCode) {
        String address="http://route.showapi.com/9-7"+countryCode+".xml";
        queryFromServer(address,"countryCode");
    }

    /**
     * 查询天气代号对应的天气
     * @param weatherCode
     */
    private void  queryWeatherInfo(String weatherCode){
        String address="http://route.showapi.com/9-7"+weatherCode+".html";
        queryFromServer(address,"countryCode");

    }

    /**
     * 根据传入的地址类型去向服务器查询天气代号或者天气信息
     * @param address
     * @param
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countryCode".equals(type)){
                    if (!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析天气信息
                        String[] array=response.split("\\|");
                        if (array!=null&&array.length==2){
                            String weatherCode=array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if ("weatherCode".equals(type)){
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });

            }
        });
    }

    /**
     * 从sharedpreferences文件读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText(prefs.getString("publish_text",""));
        currentDateText.setText(prefs.getString("current_cate",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent=new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中");
                SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode=pref.getString("weanther_code","");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:break;
        }
    }
}
