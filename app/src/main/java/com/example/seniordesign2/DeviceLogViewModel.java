package com.example.seniordesign2;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class DeviceLogViewModel extends ViewModel {

    private MutableLiveData<ArrayList<LocalDatabase.DeviceLog>> deviceLogs;

    public MutableLiveData<ArrayList<LocalDatabase.DeviceLog>> getDeviceLogs() {
        if(deviceLogs == null) {
            deviceLogs = new MutableLiveData<>();
            deviceLogs.setValue(new ArrayList<>());
        }
        return deviceLogs;
    }
}
