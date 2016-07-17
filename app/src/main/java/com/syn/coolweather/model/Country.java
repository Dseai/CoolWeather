package com.syn.coolweather.model;

/**
 * Created by 孙亚楠 on 2016/7/17.
 */
public class Country {
    private int id;
    private String countryName;
    private String countryCode;
    private int cityId;
    public int getId(){
        return id;
    }
    public int getCityId(){
        return cityId ;
    }
    public String getCountryName(){
        return countryName;
    }
    public String getCountryCode(){
        return countryCode;
    }
    public void setId(int id){
        this.id=id;
    }
    public void setCountryName(String countryName){
        this.countryName=countryName;
    }
    public void setCountryCode(String countryCode){
        this.countryCode=countryCode;
    }
    public void setCityId(int cityId){
        this.cityId=cityId;
    }
}
