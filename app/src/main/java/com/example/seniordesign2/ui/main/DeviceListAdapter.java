package com.example.seniordesign2.ui.main;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.seniordesign2.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> deviceList;
    private OnNoteListener onNoteListener;

    public DeviceListAdapter(ArrayList<BluetoothDevice> deviceList, OnNoteListener onNoteListener) {
        this.deviceList = deviceList;
        this.onNoteListener = onNoteListener;
        Log.e("APPDEBUG", "DEVICE SET SIZE: " + deviceList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_list_row_item, viewGroup, false);

        return new ViewHolder(view, onNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice currentDevice = deviceList.get(position);
        try {
            holder.getDeviceNameView().setText(currentDevice.getName());
            holder.getDeviceAddressView().setText(currentDevice.getAddress());
        } catch (SecurityException e) {
            Log.e("APPDEBUG", "SECURITY EXCEPTION");
        }

    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView deviceNameView;
        private final TextView deviceAddressView;
        private OnNoteListener onNoteListener;
        public ViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            deviceNameView = itemView.findViewById(R.id.deviceName);
            deviceAddressView = itemView.findViewById(R.id.deviceAddress);
            itemView.setOnClickListener(this);
            this.onNoteListener = onNoteListener;
        }

        @Override
        public void onClick(View view) {
            this.onNoteListener.onNoteClick(getAdapterPosition());
        }

        public TextView getDeviceNameView() {
            return deviceNameView;
        }
        public TextView getDeviceAddressView() {return deviceAddressView;}



    }

    public interface OnNoteListener {
        void onNoteClick(int position);
    }

}
