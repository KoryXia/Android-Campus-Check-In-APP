package com.example.check_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.example.check_in.utils.HttpUtils;
import com.example.check_in.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.register_form)
    LinearLayout register_form;
    @BindView(R.id.user_login_number)
    MaterialEditText user_login_number;
    @BindView(R.id.user_login_password)
    MaterialEditText user_login_password;
    @BindView(R.id.user_register_college)
    MaterialSpinner user_register_college;
    @BindView(R.id.user_register_name)
    MaterialEditText user_register_name;
    @BindView(R.id.user_register_telephone)
    MaterialEditText user_register_telephone;

    private Handler handler;
    private JSONObject jsonObject = null;
    private String msg;
    private SharedPreferences sp;
    private boolean user_exist;
    private boolean isRegister = false;
    public static String user_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.bind(this);
        handler = new Handler();
        user_login_number.setAllowEmpty(false,"不能为空");
        user_login_password.setAllowEmpty(false,"不能为空");
        user_register_name.setAllowEmpty(false,"不能为空");
        user_register_telephone.setAllowEmpty(false,"不能为空");
        sp = getSharedPreferences("user_config", MODE_PRIVATE);

        user_exist = sp.getBoolean("user_exist", false);

        if(!isNetworkAvailable()){
            MaterialDialog.Builder builder = new MaterialDialog.Builder(LoginActivity.this);
            builder .title("请打开网络连接")
                    .positiveText("完成")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                           dialog.dismiss();
                        }
                    }).show();
        }

        if(user_exist){
            user_login_number.setText(sp.getString("user_number",""));
            user_login_password.setText(sp.getString("user_password",""));
            login_in();
        }

    }

    private boolean isNetworkAvailable() {
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }



    private void login_in(){
        String url = "http://8.136.15.178:28085/api/login_in";
        HashMap<String, String> params = new HashMap();
        //添加参数
        params.put("number",user_login_number.getEditValue());
        params.put("password", user_login_password.getEditValue());
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableLogin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void register_in(){
        String url = "http://8.136.15.178:28085/api/register";
        HashMap<String, String> params = new HashMap();
        params.put("number",user_login_number.getEditValue());
        params.put("password", user_login_password.getEditValue());
        params.put("telephone", user_register_telephone.getEditValue());
        params.put("name", user_register_name.getEditValue());
        params.put("college", user_register_college.getSelectedItem().toString());
        new Thread() {
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils(url, params);
                try {
                    jsonObject = httpUtils.get();
                    handler.post(runnableRegister);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void login(View v) {
        //判断界面处于登录还是注册模式
        if(register_form.getVisibility() == View.VISIBLE){
            LoginActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    register_form.setVisibility(View.GONE);
                }
            });
            isRegister = false;
        }else{
            if(user_login_number.validate() && user_login_password.validate()){
                //进行登录
                login_in();
            }
        }
    }

    public void register(View v) {
        if(isRegister) {
            if(user_login_number.validate() && user_login_password.validate() && user_register_name.validate() && user_register_telephone.validate()) {
                if(user_register_college.getSelectedIndex() == 0){
                    XToastUtils.error("请选择学院");
                }else {
                    register_in();
                }
            }
        }else {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        register_form.setVisibility(View.VISIBLE);
                    }
                });
                isRegister = true;
            }
        }

    Runnable runnableLogin = new Runnable(){
        public void run() {
            msg = jsonObject.getString("msg");//后台JSON数据
            if(msg.equals("登录成功")){
                XToastUtils.success(msg);
                //保存登录信息，实现自动登陆
                sp.edit().putString("user_number",user_login_number.getEditValue()).commit();
                sp.edit().putString("user_password",user_login_password.getEditValue()).commit();
                sp.edit().putBoolean("user_exist", true).commit();
                //传递用户学号，跳转主界面
                user_number = user_login_number.getEditValue();
                Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }else {
                //错误提示
                XToastUtils.error(msg);
            }
        }
    };

    Runnable runnableRegister = new Runnable(){
        public void run() {
            XToastUtils.success("注册成功");
            sp.edit().putString("user_number",user_login_number.getEditValue()).commit();
            sp.edit().putString("user_password",user_login_password.getEditValue()).commit();
            sp.edit().putBoolean("user_exist", true).commit();
            user_number = jsonObject.getString("number");
            Intent intent= new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };


}