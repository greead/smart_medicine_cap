package com.example.seniordesign2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// TODO

public class DeviceLogAdapter extends RecyclerView.Adapter<DeviceLogAdapter.ViewHolder> {

    private ArrayList<LocalDatabase.DeviceLog> deviceLogs;

    public DeviceLogAdapter(ArrayList<LocalDatabase.DeviceLog> deviceLogs) {
        this.deviceLogs = deviceLogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_log_row_item, parent, false);

        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDatabase.DeviceLog deviceLog = deviceLogs.get(position);
        holder.getItemComment().setText(deviceLog.comment);
        holder.getItemTime().setText(deviceLog.time);
        holder.getItemDate().setText(deviceLog.date);
    }

    @Override
    public int getItemCount() {
        return deviceLogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemComment;
        private final TextView itemTime;
        private final TextView itemDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemComment = itemView.findViewById(R.id.item_comment);
            itemTime = itemView.findViewById(R.id.item_time);
            itemDate = itemView.findViewById(R.id.item_date);
        }

        public TextView getItemComment() {
            return itemComment;
        }

        public TextView getItemTime() {
            return itemTime;
        }

        public TextView getItemDate() {
            return itemDate;
        }

    }
}
