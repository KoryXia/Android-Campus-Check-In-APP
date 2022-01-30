package com.example.check_in;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.alibaba.fastjson.JSONObject;
import com.example.check_in.utils.ChoosedPoint;
import com.example.check_in.utils.HttpUtils;
import com.example.check_in.utils.LocationViewModel;
import com.example.check_in.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.util.Base64;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddActivity extends AppCompatActivity{
    @BindView(R.id.meeting_location)
    MaterialEditText meeting_location;
    @BindView(R.id.meeting_title)
    MaterialEditText meeting_title;
    @BindView(R.id.meeting_founder)
    MaterialEditText meeting_founder;
    @BindView(R.id.meeting_detail)
    MaterialEditText meeting_detail;
    @BindView(R.id.meeting_date)
    MaterialEditText meeting_date;


    private Handler handler;
    private JSONObject jsonObject = null;
    private JSONObject mjsonObject = null;
    private String msg;


    StringBuilder date_str = new StringBuilder("");
    public static LocationViewModel mLocationViewModel;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_layout);
        ButterKnife.bind(this);
        handler = new Handler();
        meeting_date.setAllowEmpty(false,"不能为空");
        meeting_detail.setAllowEmpty(false,"不能为空");
        meeting_founder.setAllowEmpty(false,"不能为空");
        meeting_location.setAllowEmpty(false,"不能为空");
        meeting_title.setAllowEmpty(false,"不能为空");
        getUserInfo();
        mLocationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        mLocationViewModel.getCurrentChoosedPoint().observe(this,new Observer<ChoosedPoint>() {
            @Override
            public void onChanged(ChoosedPoint choosedPoint) {
                meeting_location.setText(choosedPoint.getLocationName());
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void time_picker(View v){
        date_str.setLength(0);
        Calendar calendar = Calendar.getInstance();
        Dialog dateDialog = new DatePickerDialog(AddActivity.this, R.style.dialog_date, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date_str.append(year + "-" + (month + 1) + "-" + dayOfMonth + " ");
                Calendar time = Calendar.getInstance();
                Dialog timeDialog = new TimePickerDialog(AddActivity.this, R.style.dialog_date, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date_str.append(hourOfDay + ":" + minute + ":00");
                        try {
                            meeting_date.setText(format.format(format.parse(date_str.toString())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),true);
                timeDialog.show();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    private void getUserInfo(){
        String url = "http://8.136.15.178:28085/api/getUserInfo";
        HashMap<String, String> params = new HashMap();
        params.put("number", LoginActivity.user_number);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    mjsonObject = httpUtils.get();
                    handler.post(runnableUi);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableUi = new Runnable(){
        @Override
        public void run() {
            meeting_founder.setText(mjsonObject.getString("name"));
        }
    };


    private void submit_info(){
        String url = "http://8.136.15.178:28085/api/createMeeting";
        HashMap<String, String> params = new HashMap();
        params.put("title", meeting_title.getEditValue());
        params.put("founder", meeting_founder.getEditValue());
        params.put("latitude",String.valueOf(AddActivity.mLocationViewModel.getCurrentChoosedPoint().getValue().getLatitude()));
        params.put("longitude",String.valueOf(AddActivity.mLocationViewModel.getCurrentChoosedPoint().getValue().getLongitude()));
        params.put("detail",Base64.encodeToString(meeting_detail.getEditValue().getBytes(), Base64.NO_WRAP));
        params.put("locationName", meeting_location.getEditValue());
        params.put("date", Base64.encodeToString(meeting_date.getEditValue().getBytes(), Base64.NO_WRAP));
        params.put("number", LoginActivity.user_number);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableSubmit);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableSubmit = new Runnable(){
        public void run() {
            if(jsonObject != null){
                new MaterialDialog.Builder(AddActivity.this)
                        .title("创建成功")
                        .content("会议号："+jsonObject.getString("id"))
                        .positiveText("确定")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent intent= new Intent(AddActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .negativeText("取消")
                        .cancelable(false)
                        .show();
            }else {
                XToastUtils.error("创建失败");
            }
        }
    };

    public void submit(View v){
        if(meeting_title.validate() && meeting_founder.validate() && meeting_location.validate() && meeting_detail.validate() && meeting_date.validate()){
            submit_info();
        }
    }

    public void backToMain(View v){
        Intent intent= new Intent(AddActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void to_choose(View v){
        Intent intent= new Intent(AddActivity.this, ChooseLocationActivity.class);
        startActivity(intent);
    }

}

