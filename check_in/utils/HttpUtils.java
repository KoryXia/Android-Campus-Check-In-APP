package com.example.check_in.utils;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;
import com.example.check_in.LoginActivity;


public class HttpUtils {

    private String url;
    private int code;
    private HashMap<String, String> params;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public HttpUtils(String url, HashMap<String, String> params) {
        this.url = url;
        this.params = params;
    }

    public JSONObject get() throws IOException {
        URL url = new URL(this.buildURI());
        //2. HttpURLConnection
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        //3. set(GET)
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        code = conn.getResponseCode();
        if(code == 200){
            //4. getInputStream
            InputStream is = conn.getInputStream();
            //5. 解析is，获取responseText，这里用缓冲字符流
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while((line=reader.readLine()) != null){
                stringBuilder.append(line);
            }
            JSONObject jsonObject = JSONObject.parseObject(stringBuilder.toString());
            return jsonObject;
        }
        return new JSONObject();
    }


    public String buildURI() {

        Uri.Builder builder = Uri.parse(this.url).buildUpon();
        for (HashMap.Entry<String, String> entry : this.params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        return builder.build().toString();
    }

}
