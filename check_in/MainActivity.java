package com.example.check_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.example.check_in.navigation.Index;
import com.example.check_in.navigation.My_info;
import com.example.check_in.utils.HttpUtils;
import com.example.check_in.utils.XToastUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    //定义Fragment
    private Index index;
    private My_info my_info;
    //记录当前正在使用的fragment
    private Fragment currentFragment;

    private int id;
    private Handler handler;
    private JSONObject jsonObject = null;
    private String msg;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFragment(savedInstanceState);
        handler = new Handler();
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sp = getSharedPreferences("user_config", MODE_PRIVATE);

    }

    public void initFragment(Bundle savedInstanceState) {
        //判断activity是否重建，如果不是，则不需要重新建立fragment.
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (index == null) {
                index = new Index();
            }
            currentFragment = index;
            ft.replace(R.id.container, index).commit();
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_index:
                    if (index == null) {
                        index = new Index();
                    }
                    switchContent(currentFragment, index);
                    return true;
                case R.id.navigation_my_info:
                    if (my_info == null) {
                        my_info = new My_info();
                    }
                    switchContent(currentFragment, my_info);
                    return true;
            }
            return false;
        }

    };

    public void switchContent(Fragment from, Fragment to) {
        if (currentFragment != to) {
            currentFragment = to;
            FragmentManager fm = getSupportFragmentManager();
            //添加渐隐渐现的动画
            FragmentTransaction ft = fm.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                ft.hide(from).add(R.id.container, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                ft.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }


    private void user_join(){
        String url = "http://8.136.15.178:28085/api/join_in";
        HashMap<String, String> params = new HashMap();
        params.put("id", String.valueOf(id));
        params.put("number", LoginActivity.user_number);
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableJoin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Runnable runnableJoin = new Runnable(){
        public void run() {
            msg = jsonObject.getString("msg");
            if(msg.equals("加入成功")){
                XToastUtils.success(msg);
                finish();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }else {
                XToastUtils.error(msg);
            }
        }
    };

    public void join_in(View v){
        new MaterialDialog.Builder(MainActivity.this)
                .title("请输入会议号")
                .inputType(
                        InputType.TYPE_CLASS_NUMBER)
                .input(
                        "会议号",
                        "",
                        false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                id = Integer.parseInt(input.toString());
                                user_join();
                            }
                        })
                .positiveText("加入")
                .negativeText("取消")
                .cancelable(false)
                .show();
    }


    public void click_add(View v){
        Intent intent= new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);
    }
    public void click_exit(View v){
        sp.edit().clear().commit();
        Intent intent= new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}