package com.example.seniordesign2;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class DatabaseViewModel extends ViewModel {

    private MutableLiveData<LocalDatabase.DeviceDao> deviceDao;
    private MutableLiveData<LocalDatabase.AlarmDao> alarmDao;
    private MutableLiveData<LocalDatabase.ContactDao> contactDao;

    private MutableLiveData<ArrayList<LocalDatabase.DeviceLog>> selectedDeviceLogs;
    private MutableLiveData<ArrayList<LocalDatabase.AlarmLog>> selectedAlarmLogs;
    private MutableLiveData<ArrayList<LocalDatabase.ContactLog>> selectedContactLogs;

    public MutableLiveData<LocalDatabase.DeviceDao> getDeviceDao() {
        if(deviceDao == null) {
            deviceDao = new MutableLiveData<>();
        }
        return deviceDao;
    }

    public MutableLiveData<LocalDatabase.AlarmDao> getAlarmDao() {
        if (alarmDao == null) {
            alarmDao = new MutableLiveData<>();
        }
        return alarmDao;
    }

    public MutableLiveData<LocalDatabase.ContactDao> getContactDao() {
        if (contactDao == null) {
            contactDao = new MutableLiveData<>();
        }
        return contactDao;
    }

    public MutableLiveData<ArrayList<LocalDatabase.DeviceLog>> getSelectedDeviceLogs() {
        if (selectedDeviceLogs == null) {
            selectedDeviceLogs = new MutableLiveData<>();
            selectedDeviceLogs.setValue(new ArrayList<>());
        }
        return selectedDeviceLogs;
    }

    public MutableLiveData<ArrayList<LocalDatabase.AlarmLog>> getSelectedAlarmLogs() {
        if (selectedAlarmLogs == null) {
            selectedAlarmLogs = new MutableLiveData<>();
            selectedAlarmLogs.setValue(new ArrayList<>());
        }
        return selectedAlarmLogs;
    }

    public MutableLiveData<ArrayList<LocalDatabase.ContactLog>> getSelectedContactLogs() {
        if (selectedContactLogs == null) {
            selectedContactLogs = new MutableLiveData<>();
            selectedContactLogs.setValue(new ArrayList<>());
        }
        return selectedContactLogs;
    }

    public void getAllAndWait(String deviceAddress) {
        try {
            GetDeviceLogsThread getDeviceLogsThread = new GetDeviceLogsThread(deviceAddress);
            getDeviceLogsThread.start();
            getDeviceLogsThread.join();

            GetContactLogsThread getContactLogsThread = new GetContactLogsThread(deviceAddress);
            getContactLogsThread.start();
            getContactLogsThread.join();

            GetAlarmLogsThread getAlarmLogsThread = new GetAlarmLogsThread(deviceAddress);
            getAlarmLogsThread.start();
            getAlarmLogsThread.join();
        } catch (InterruptedException e) {
            Log.e("APPDEBUG", "Interrupted Exception occurred");
        }
    }

    public void getDeviceLogs(String deviceAddress) {
        if (deviceDao == null || deviceDao.getValue() == null) {
            Log.e("APP-DEBUG", "Device DAO not initialized or is null");
            return;
        }
        new GetDeviceLogsThread(deviceAddress).start();

    }

    // Query methods and thread classes
    private class GetDeviceLogsThread extends Thread {
        private String deviceAddress;
        public GetDeviceLogsThread(String deviceAddress) {this.deviceAddress = deviceAddress;}

        @Override
        public void run() {
            selectedDeviceLogs.postValue(new ArrayList<>(deviceDao.getValue().loadByAddress(deviceAddress)));
        }
    }

    public void getAlarmLogs(String deviceAddress) {
        if (alarmDao == null || alarmDao.getValue() == null) {
            Log.e("APP-DEBUG", "Alarm DAO not initialized or is null");
            return;
        }
        new GetAlarmLogsThread(deviceAddress).start();
    }
    private class GetAlarmLogsThread extends Thread {
        private String deviceAddress;
        public GetAlarmLogsThread(String deviceAddress) {this.deviceAddress = deviceAddress;}

        @Override
        public void run() {
            selectedAlarmLogs.postValue(new ArrayList<>(alarmDao.getValue().loadByAddress(deviceAddress)));
        }
    }

    public void getContactLogs(String deviceAddress) {
        if (contactDao == null || contactDao.getValue() == null) {
            Log.e("APP-DEBUG", "Contact DAO not initialized or is null");
            return;
        }
        new GetContactLogsThread(deviceAddress).start();
    }
    private class GetContactLogsThread extends Thread {
        private String deviceAddress;
        public GetContactLogsThread(String deviceAddress) {this.deviceAddress = deviceAddress;}

        @Override
        public void run() {
            selectedContactLogs.postValue(new ArrayList<>(contactDao.getValue().loadByAddress(deviceAddress)));
        }
    }

    public void insertDeviceLogs(LocalDatabase.DeviceLog... deviceLogs) {
        if (deviceDao == null || deviceDao.getValue() == null) {
            Log.e("APP-DEBUG", "Device DAO not initialized or is null");
            return;
        }
        new InsertDeviceLogsThread(deviceLogs).start();
    }

    // Insert methods and thread classes
    private class InsertDeviceLogsThread extends Thread {
        LocalDatabase.DeviceLog[] deviceLogs;
        public InsertDeviceLogsThread(LocalDatabase.DeviceLog... deviceLogs) {
            this.deviceLogs = deviceLogs;
        }

        @Override
        public void run() {
            deviceDao.getValue().insertAll(deviceLogs);
        }
    }

    public void insertAlarmLogs(LocalDatabase.AlarmLog... alarmLogs) {
        if (alarmDao == null || alarmDao.getValue() == null) {
            Log.e("APP-DEBUG", "Alarm DAO not initialized or is null");
            return;
        }
        new InsertAlarmLogsThread(alarmLogs).start();
    }
    private class InsertAlarmLogsThread extends Thread {
        LocalDatabase.AlarmLog[] alarmLogs;
        public InsertAlarmLogsThread(LocalDatabase.AlarmLog... alarmLogs) {
            this.alarmLogs = alarmLogs;
        }

        @Override
        public void run() {
            alarmDao.getValue().insertAll(alarmLogs);
        }
    }

    public void insertContactLogs(LocalDatabase.ContactLog... contactLogs) {
        if (contactDao == null || contactDao.getValue() == null) {
            Log.e("APP-DEBUG", "Contact DAO not initialized or is null");
            return;
        }
        new InsertContactLogsThread(contactLogs).start();
    }
    private class InsertContactLogsThread extends Thread {
        LocalDatabase.ContactLog[] contactLogs;
        public InsertContactLogsThread(LocalDatabase.ContactLog... contactLogs) {
            this.contactLogs = contactLogs;
        }

        @Override
        public void run() {
            contactDao.getValue().insertAll(contactLogs);
        }
    }

}
