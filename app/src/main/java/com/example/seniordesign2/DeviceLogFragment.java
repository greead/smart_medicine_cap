package com.example.seniordesign2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DeviceLogFragment extends Fragment {

    private DeviceLogViewModel deviceLogViewModel;
    private RecyclerView.LayoutManager layoutManager;
    private DeviceLogAdapter deviceLogAdapter;
    private RecyclerView lstDeviceLogs;

    public DeviceLogFragment() {
        // Required empty public constructor
    }

    public static DeviceLogFragment newInstance(DeviceLogViewModel deviceLogViewModel) {
        DeviceLogFragment deviceLogFragment = new DeviceLogFragment();
        deviceLogFragment.setDeviceLogViewModel(deviceLogViewModel);
        return deviceLogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_log, container, false);

        layoutManager = new LinearLayoutManager(requireContext());
        deviceLogAdapter = new DeviceLogAdapter(deviceLogViewModel.getDeviceLogs().getValue());
        lstDeviceLogs = view.findViewById(R.id.lstDeviceLog);
        lstDeviceLogs.setAdapter(deviceLogAdapter);
        lstDeviceLogs.setLayoutManager(layoutManager);

        deviceLogViewModel.getDeviceLogs().observe(getViewLifecycleOwner(), observedValue -> {
            deviceLogAdapter = new DeviceLogAdapter(observedValue);
            lstDeviceLogs.setAdapter(deviceLogAdapter);
        });


        return view;
    }

    private void setDeviceLogViewModel(DeviceLogViewModel deviceLogViewModel) {
        this.deviceLogViewModel = deviceLogViewModel;
    }

}