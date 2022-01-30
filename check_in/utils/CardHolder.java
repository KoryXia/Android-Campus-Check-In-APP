package com.example.check_in.utils;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.check_in.R;

public class CardHolder extends RecyclerView.ViewHolder {
    private TextView tv_title;
    private TextView tv_name;
    private TextView tv_time;
    private TextView tv_id;
    private TextView tv_location;

    public CardHolder(View view , final CardAdapter.OnItemClickListener onClickListener) {
        super(view);
        tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        tv_name = (TextView) itemView.findViewById(R.id.tv_name);
        tv_time = (TextView) itemView.findViewById(R.id.tv_time);
        tv_id = (TextView) itemView.findViewById(R.id.tv_id);
        tv_location = (TextView) itemView.findViewById(R.id.tv_location);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    int position = getAdapterPosition();
                    //确保position值有效
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onItemClicked(view, position);
                    }
                }
            }
        });
    }

}
