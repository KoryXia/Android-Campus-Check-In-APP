package com.example.check_in;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.check_in.utils.HttpUtils;
import com.example.check_in.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Check extends AppCompatActivity {
    @BindView(R.id.mId)
    TextView mId;
    @BindView(R.id.mTitle)
    TextView mTitle;
    @BindView(R.id.mTime)
    TextView mTime;
    @BindView(R.id.mLocation)
    TextView mLocation;
    @BindView(R.id.btn_send)
    Button btn_send;
    @BindView(R.id.mName)
    TextView mName;
    @BindView(R.id.btn_manage)
    Button btn_manage;
    @BindView(R.id.btn_delete)
    Button btn_delete;

    public static  String id;
    private JSONObject meeting_info;
    private String msg;
    private JSONObject jsonObject;
    private Handler handler;
    private float distance;
    private LatLng user_LatLng;
    private LatLng meeting_LatLng;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        handler = new Handler();
        ButterKnife.bind(this);
        getMeetingInfo();
        isCheck();
        initGPS();
        initLocation();
    }

    private void initGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //??????GPS???????????????????????????????????????
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            MaterialDialog.Builder builder = new MaterialDialog.Builder(Check.this);
            builder .title("?????????GPS??????")
                    .content("??????????????????????????????????????????????????????????????????GPS")
                    .positiveText("??????")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //?????????????????????GPS??????
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //????????????????????????????????????
                            startActivityForResult(intent,-1);
                        }
                    })
                    .negativeText("??????")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    private void isCheck(){
        String url = "http://8.136.15.178:28085/api/isCheck";
        HashMap<String, String> params = new HashMap();
        params.put("number",LoginActivity.user_number );
        params.put("id",Check.id);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableIsCheck);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableIsCheck = new Runnable(){
        public void run() {
            //??????????????????
            msg = jsonObject.getString("msg");
            if(msg.equals("1")){
                //??????????????????????????????
                btn_send.setText("?????????");
                btn_send.setEnabled(false);
            } else if(msg.equals("2")){
                //?????????????????????????????????????????????
                btn_send.setVisibility(View.GONE);
                btn_manage.setVisibility(View.VISIBLE);
                btn_delete.setVisibility(View.VISIBLE);
            }
        }
    };

    private void send_distance(){
        String url = "http://8.136.15.178:28085/api/check_in";
        HashMap<String, String> params = new HashMap();
        params.put("distance", String.valueOf(distance));
        params.put("number",LoginActivity.user_number );
        params.put("id",id);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableSend = new Runnable(){
        public void run() {
            msg = jsonObject.getString("msg");
            if(msg.equals("????????????")){
                btn_send.setText("?????????");
                btn_send.setEnabled(false);
            }else {
                XToastUtils.error(msg);
            }
        }
    };

    private void getMeetingInfo(){
        String url = "http://8.136.15.178:28085/api/getMeetingInfo";
        HashMap<String, String> params = new HashMap();
        params.put("id", id);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    meeting_info = httpUtils.get();
                    handler.post(runnableMeetingInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableMeetingInfo = new Runnable(){
        public void run() {
            meeting_LatLng = new LatLng(Double.parseDouble(meeting_info.getString("latitude")),Double.parseDouble(meeting_info.getString("longitude")));
            mId.setText(meeting_info.getString("id"));
            mLocation.setText(meeting_info.getString("locationName") + meeting_info.getString("detail"));
            mTitle.setText(meeting_info.getString("title"));
            mName.setText(meeting_info.getString("founder"));
            mTime.setText(meeting_info.getString("date"));
        }
    };

    private void delete(){
        String url = "http://8.136.15.178:28085/api/delete";
        HashMap<String, String> params = new HashMap();
        params.put("id", id);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableDelete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableDelete = new Runnable(){
        public void run() {
            XToastUtils.info("????????????");
            Intent intent= new Intent(Check.this, MainActivity.class);
            startActivity(intent);
        }
    };



    public void send(View v) {
        startLocation();
    }

    public void manage(View v) {
        Intent intent= new Intent(Check.this, Administer.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    /**
     * ???????????????

     */
    private void initLocation(){
        //?????????client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //??????????????????
        locationClient.setLocationOption(locationOption);
        // ??????????????????
        locationClient.setLocationListener(locationListener);
    }

    /**
     * ?????????????????????
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//????????????????????????????????????????????????????????????????????????????????????????????????????????????
        mOption.setGpsFirst(true);//?????????????????????gps??????????????????????????????????????????????????????
        mOption.setOnceLocation(true);//?????????????????????????????????????????????false
        mOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        mOption.setHttpTimeOut(3000);//???????????????????????????????????????????????????30?????????????????????????????????
        mOption.setNeedAddress(false);
        return mOption;
    }

    /**
     * ????????????
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                if(location.getErrorCode() == 0){
                    //?????????????????????
                    user_LatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    //????????????????????????????????????????????????
                    distance = AMapUtils.calculateLineDistance(user_LatLng,meeting_LatLng);
                    //?????????????????????
                    send_distance();
                } else {
                    XToastUtils.error("????????????");
                }
            }
        }
    };



    private void startLocation(){
        // ??????????????????
        locationClient.setLocationOption(locationOption);
        // ????????????
        locationClient.startLocation();
    }


    private void stopLocation(){
        // ????????????
        locationClient.stopLocation();
    }

    private void destroyLocation(){
        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        destroyLocation();
    }

    public void backToMain(View v){
        Intent intent= new Intent(Check.this, MainActivity.class);
        startActivity(intent);
    }

    public void btn_delete(View v){
        delete();
    }

}