package com.example.ecgtest.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecgtest.R;
import com.example.ecgtest.activity.PortDetailActivity;

import java.util.List;

/**
 * 串口列表适配器
 * */
public class PortListAdapter extends RecyclerView.Adapter<PortListAdapter.MyViewHolder> {

    private Context context;
    private List<String> devices;

    public PortListAdapter(Context context,List<String> devices) {
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.port_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemTvNum.setText(String.valueOf(position));
        holder.itemTVPort.setText(devices.get(position));
        //点击卡片跳转卡片详情
        holder.itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, PortDetailActivity.class);
            intent.putExtra("devicePath", devices.get(position));//传递串口地址
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemTvNum;
        TextView itemTVPort;
        LinearLayout itemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemTvNum = itemView.findViewById(R.id.item_tv_num);
            itemTVPort = itemView.findViewById(R.id.item_tv_port);
            itemLayout = itemView.findViewById(R.id.port_item_layout);
        }
    }

}
