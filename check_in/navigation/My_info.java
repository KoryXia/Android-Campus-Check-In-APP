package com.example.check_in.navigation;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.alibaba.fastjson.JSONObject;
import com.example.check_in.LoginActivity;
import com.example.check_in.R;
import com.example.check_in.utils.HttpUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;


import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class My_info extends Fragment{
    @BindView(R.id.info_titlebar)
    TitleBar info_TitleBar;
    @BindView(R.id.name)
    SuperTextView name;
    @BindView(R.id.telephone)
    SuperTextView telephone;
    @BindView(R.id.college)
    SuperTextView college;
    @BindView(R.id.number)
    SuperTextView number;

    private View myInfoFragment;

    private Handler handler;
    private JSONObject jsonObject = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        myInfoFragment = inflater.inflate(R.layout.my_info_layout, container, false);
        ButterKnife.bind(this, myInfoFragment);
        handler = new Handler();
        initView();
        return myInfoFragment;
    }

    /**
     * 初始化视图
     * */
    private void initView(){
        info_TitleBar.disableLeftView();
        getUserInfo();
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
                    jsonObject = httpUtils.get();
                    handler.post(runnableUi);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable runnableUi = new Runnable(){
        @Override
        public void run() {
            //更新界面
            name.setRightString(jsonObject.getString("name"));
            telephone.setRightString(jsonObject.getString("telephone"));
            college.setRightString(jsonObject.getString("college"));
            number.setRightString(jsonObject.getString("number"));
        }

    };


}