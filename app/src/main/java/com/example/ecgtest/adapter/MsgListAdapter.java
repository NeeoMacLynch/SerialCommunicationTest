package com.example.ecgtest.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecgtest.R;
import com.example.ecgtest.activity.PortDetailActivity;

import java.util.List;

public class MsgListAdapter extends RecyclerView.Adapter<MsgListAdapter.MyViewHolder> {

    private List<String> messages;

    public MsgListAdapter(Context context, List<String> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MsgListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_item, parent, false);
        return new MsgListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MsgListAdapter.MyViewHolder holder, int position) {
        holder.itemTvMsg.setText(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemTvMsg;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemTvMsg = itemView.findViewById(R.id.item_tv_msg);
        }
    }

}
