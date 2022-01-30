package com.example.check_in.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.example.check_in.R;

import java.text.ParseException;
import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardHolder> {

    private Context mContext;
    private ArrayList<JSONObject> list = new ArrayList<>();
    TextView tv_title;
    TextView tv_name;
    TextView tv_time;
    TextView tv_id;
    TextView tv_location;
    private OnItemClickListener mOnItemClickListener;

    CardView cv;

    public CardAdapter(Context context, ArrayList<JSONObject> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview, parent, false);
        cv = view.findViewById(R.id.card_view);
        tv_title = view.findViewById(R.id.tv_title);
        tv_name = view.findViewById(R.id.tv_name);
        tv_time = view.findViewById(R.id.tv_time);
        tv_id = view.findViewById(R.id.tv_id);
        tv_location = view.findViewById(R.id.tv_location);
        CardHolder cardHolder = new CardHolder(view,mOnItemClickListener) {
        };
        return cardHolder;
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        if(list.size() != 0){
            CalendarReminderUtils reminder = new CalendarReminderUtils();
            JSONObject jsonObject = list.get(position);
            //更新卡片信息
            tv_id.setText(jsonObject.getString("id"));
            tv_title.setText(jsonObject.getString("title"));
            tv_name.setText(jsonObject.getString("founder"));
            tv_location.setText(jsonObject.getString("locationName")+jsonObject.getString("detail"));
            tv_time.setText(jsonObject.getString("date"));
            //创建日历提醒
            try {
                reminder.addCalendarEvent(mContext,jsonObject.getString("title")+"——会议号:"+jsonObject.getString("id")
                        ,jsonObject.getString("locationName")+jsonObject.getString("detail")
                        ,reminder.stringToLong(jsonObject.getString("date"),"yyyy-MM-dd HH:mm:ss"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //点击事件接口
    public interface OnItemClickListener{

        void onItemClicked(View view, int position);
    }
    //设置点击事件的方法
    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

}
