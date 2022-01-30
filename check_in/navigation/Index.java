package com.example.check_in.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.example.check_in.Check;
import com.example.check_in.LoginActivity;
import com.example.check_in.MainActivity;
import com.example.check_in.R;
import com.example.check_in.utils.CardAdapter;
import com.example.check_in.utils.HttpUtils;
import com.example.check_in.utils.XToastUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.dialog.strategy.InputInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Index extends Fragment {
    @BindView(R.id.recyclerView)
    RecyclerView rv;
    @BindView(R.id.index_titlebar)
    TitleBar index_titlebar;

    private View indexFragment;
    private Handler handler;
    private int index;
    private JSONObject data = null;
    private ArrayList<JSONObject> meetings_list = new ArrayList<>();

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        handler = new Handler();
//        initData();
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        indexFragment = inflater.inflate(R.layout.index_layout,container,false);
        ButterKnife.bind(this,indexFragment);
        handler = new Handler();
        initData();
        return indexFragment;
    }

    private void initData(){
        String url = "http://8.136.15.178:28085/api/getMeetingList";
        HashMap<String, String> params = new HashMap();
        params.put("number", LoginActivity.user_number);
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
        public void run() {
            //后台返回数据
            index = Integer.parseInt(data.getString("index"));
            if(index != 0){
                for(int i = 0; i < index; i++){
                    String str = String.format(("data%d"),i);
                    meetings_list.add(JSONObject.parseObject(data.getString(str)));
                }
            }
            //新建卡片适配器，并初始化会议数据
            CardAdapter adapter = new CardAdapter(getContext(),meetings_list);
            adapter.setHasStableIds(true);
            rv.setAdapter(adapter);
            //卡片显示布局
            LinearLayoutManager layoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(layoutManager);
            adapter.setItemClickListener(new CardAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    String id = meetings_list.get(position).getString("id");
                    Check.id = id;
                    Intent intent = new Intent(getContext(), Check.class);
                    startActivity(intent);
                }
            });
        }
    };

}
