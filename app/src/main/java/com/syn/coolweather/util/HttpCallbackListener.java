package com.syn.coolweather.util;

/**
 * Created by 孙亚楠 on 2016/7/17.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);

}
