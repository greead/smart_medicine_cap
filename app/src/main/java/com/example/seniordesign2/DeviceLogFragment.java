package com.example.seniordesign2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DeviceLogFragment extends Fragment {

    private DeviceLogViewModel deviceLogViewModel;

    public DeviceLogFragment() {
        // Required empty public constructor
    }

    public static DeviceLogFragment newInstance(DeviceLogViewModel deviceLogViewModel) {
        DeviceLogFragment fragment = new DeviceLogFragment();
        fragment.setDeviceLogViewModel(deviceLogViewModel);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void setDeviceLogViewModel(DeviceLogViewModel deviceLogViewModel) {
        this.deviceLogViewModel = deviceLogViewModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_log, container, false);
        // Components
        return view;
    }
}