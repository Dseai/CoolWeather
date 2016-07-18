package com.syn.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.syn.coolweather.R;
import com.syn.coolweather.model.City;
import com.syn.coolweather.model.CoolWeatherDB;
import com.syn.coolweather.model.Country;
import com.syn.coolweather.model.Province;
import com.syn.coolweather.util.HttpCallbackListener;
import com.syn.coolweather.util.HttpUtil;
import com.syn.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 孙亚楠 on 2016/7/17.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTRY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList=new ArrayList<String>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<Country> countryList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int  currentLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected",false)){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB=CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }
                else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCountries();
                }else if (currentLevel==LEVEL_COUNTRY){
                    String countryCode=countryList.get(position).getCountryCode();
                    Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("country_code",countryCode);
                    startActivity(intent);
                    finish();
                }
            }




        });
        //加载省级数据
        queryProvinces();
    }

    private void queryProvinces() {
        provinceList=coolWeatherDB.loadProvince();
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }
        else{
            queryFromSever(null,"province");
        }
    }

    private void queryCities() {
        cityList=coolWeatherDB.loadCity();
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }
        else{
            queryFromSever(selectedProvince.getProvinceCode(),"city");
        }
    }



    private void queryCountries() {
        countryList=coolWeatherDB.loadCcountries();
        if (countryList.size()>0){
            dataList.clear();
            for (Country country:countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTRY;
        }
        else{
            queryFromSever(selectedCity.getCityCode(),"country");
        }
    }
    private void queryFromSever(final String code,final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            address="http://route.showapi.com/9-7"+code+".xml";
        }else {
            address="http://route.showapi.com/9-7.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                    if("province".equals(type)){
                        result= Utility.handleProvinceResponse(coolWeatherDB,response);

                    }else if ("city".equals(type)){
                        result=Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());

                    }else if ("country".equals(type)){
                        result=Utility.handleCountriesResponse(coolWeatherDB,response,selectedCity.getId());
                    }
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                               queryProvinces();

                            }else if ("city".equals(type)){
                                queryCities();

                            }else if ("country".equals(type)){
                              queryCountries();
                            }

                        }


                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
    @Override
    public void onBackPressed(){
        if (currentLevel==LEVEL_COUNTRY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvinces();
        }else {
            finish();
        }
    }
}
