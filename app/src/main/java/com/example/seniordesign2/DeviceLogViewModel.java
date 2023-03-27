package com.example.seniordesign2;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class DeviceLogViewModel extends ViewModel {

    private MutableLiveData<DeviceLog> deviceLog;
    private MutableLiveData<DeviceLog.LogDao> logDao;
    private MutableLiveData<ArrayList<DeviceLog.Entry>> selectedEntries;

    // Getters
    public MutableLiveData<DeviceLog> getDeviceLog() {
        if (deviceLog == null) {
            deviceLog = new MutableLiveData<>();
        }
        return deviceLog;
    }

    public MutableLiveData<DeviceLog.LogDao> getLogDao() {
        if (logDao == null) {
            logDao = new MutableLiveData<>();
        }
        return logDao;
    }

    public MutableLiveData<ArrayList<DeviceLog.Entry>> getSelectedEntries() {
        if (selectedEntries == null) {
            selectedEntries = new MutableLiveData<>();
            selectedEntries.setValue(new ArrayList<>());
        }
        return selectedEntries;
    }

    public void getAllDevices() {
        if (deviceLog == null || deviceLog.getValue() == null) {
            Log.e("APPDEBUG", "Device log not initialized");
            return;
        } else if (logDao == null || logDao.getValue() == null) {
            Log.e("APPDEBUG", "DAO not initialized");
            return;
        }
        new GetAllThread().start();

    }

    private class GetAllThread extends Thread {
        @Override
        public void run() {
            selectedEntries = new MutableLiveData<>(new ArrayList<>(logDao.getValue().getAll()));
        }
    }
}
