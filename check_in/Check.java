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
        //判断GPS是否开启，没有开启，则开启
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            MaterialDialog.Builder builder = new MaterialDialog.Builder(Check.this);
            builder .title("请打开GPS连接")
                    .content("为了提高定位的准确度，更好的为您服务，请打开GPS")
                    .positiveText("设置")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //跳转到手机打开GPS页面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //设置完成后返回原来的界面
                            startActivityForResult(intent,-1);
                        }
                    })
                    .negativeText("取消")
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
            //后台返回数据
            msg = jsonObject.getString("msg");
            if(msg.equals("1")){
                //普通参会者且已经签到
                btn_send.setText("已签到");
                btn_send.setEnabled(false);
            } else if(msg.equals("2")){
                //会议创建者，显示统计和删除按钮
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
            if(msg.equals("签到成功")){
                btn_send.setText("已签到");
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
            XToastUtils.info("删除成功");
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
     * 初始化定位

     */
    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        mOption.setHttpTimeOut(3000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setNeedAddress(false);
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                if(location.getErrorCode() == 0){
                    //用户设备经纬度
                    user_LatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    //计算会议地点和用户位置之间等距离
                    distance = AMapUtils.calculateLineDistance(user_LatLng,meeting_LatLng);
                    //发送服务器判断
                    send_distance();
                } else {
                    XToastUtils.error("定位失败");
                }
            }
        }
    };



    private void startLocation(){
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }


    private void stopLocation(){
        // 停止定位
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