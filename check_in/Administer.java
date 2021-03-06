package com.example.check_in;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.style.FontStyle;
import com.example.check_in.utils.ExcelUtil;
import com.example.check_in.utils.HttpUtils;
import com.example.check_in.utils.UserInfo;
import com.example.check_in.utils.XToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Administer extends AppCompatActivity {
    @BindView(R.id.table)
    SmartTable table;

    private Handler handler;
    private JSONObject data = null;
    private JSONObject flag0 = null;
    private JSONObject flag1 = null;
    private List<UserInfo> list = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer);
        handler = new Handler();
        ButterKnife.bind(this);
        checkPermission();
        initData();
    }


    private void initData(){
        String url = "http://8.136.15.178:28085/api/getMeetingDetail";
        HashMap<String, String> params = new HashMap();
        params.put("id", Check.id);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    data = httpUtils.get();
                    handler.post(runnableList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableList = new Runnable(){
        //?????????????????????
        public void run() {
            //??????JSON????????????
            //??????????????????
            flag0 = data.getJSONObject("flag0");
            flag1 = data.getJSONObject("flag1");
            list = new ArrayList<>();
            for(int i=0;i<flag0.getInteger("index");i++){
                list.add(new UserInfo(flag0.getJSONObject(String.format("data%d",i)).getString("name")
                        ,flag0.getJSONObject(String.format("data%d",i)).getString("telephone")
                        ,flag0.getJSONObject(String.format("data%d",i)).getString("college")
                        ,flag0.getJSONObject(String.format("data%d",i)).getString("number"),"?????????"));
            }
            for(int i=0;i<flag1.getInteger("index");i++){
                list.add(new UserInfo(flag1.getJSONObject(String.format("data%d",i)).getString("name")
                        ,flag1.getJSONObject(String.format("data%d",i)).getString("telephone")
                        ,flag1.getJSONObject(String.format("data%d",i)).getString("college")
                        ,flag1.getJSONObject(String.format("data%d",i)).getString("number"),""));
            }
            table.setData(list);
            table.setZoom(true);
            table.getConfig().setHorizontalPadding(50);
            table.getConfig().setShowXSequence(false);
            table.getConfig().setColumnTitleStyle(new FontStyle(50, Color.BLACK));
            table.getConfig().setTableTitleStyle(new FontStyle(80, Color.BLACK));
            table.getConfig().setContentStyle(new FontStyle(50, Color.BLUE));

        }
    };

    public void back(View v){
        Intent intent= new Intent(Administer.this, Check.class);
        startActivity(intent);
    }

    public void toExcel(View V){
        //???????????????????????????????????????????????????
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/partcipants_datas";
        File files = new File(filePath);
        if (!files.exists()) {
            files.mkdirs();
        }
        //?????????
        String excelName = "/" + "??????????????????-" + Check.id;
        //?????????excel??????
        String[] title = {"??????", "??????", "??????", "??????","??????"};
        String excelFileName = excelName + ".xlsx";
        String resultPath = files.getAbsolutePath() + excelFileName;
        ExcelUtil.initExcel(resultPath, title);
        File moudleFile = ExcelUtil.writeObjListToExcel(list, resultPath, Administer.this);
        if (moudleFile != null) {
            XToastUtils.info("?????????????????????" + filePath);
        }
    }

    private void checkPermission() {
        //???????????????NEED_PERMISSION?????????????????? PackageManager.PERMISSION_GRANTED??????????????????
        if (ActivityCompat.checkSelfPermission(Administer.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //??????????????????????????????????????????????????????????????????????????????????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(Administer.this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                XToastUtils.info("???????????????????????????????????????????????????????????????");
            }
            //????????????
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}