package com.example.seniordesign2;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeviceDialogViewModel extends ViewModel {

    private MutableLiveData<Boolean> timePickerFlag;
    private MutableLiveData<String> connectionStatus = new MutableLiveData<>();
    private MutableLiveData<String> alarmStatus = new MutableLiveData<>();
    private MutableLiveData<String> contactStatus = new MutableLiveData<>();

    public DeviceDialogViewModel() {
        connectionStatus.setValue("NOT CONNECTED");
        alarmStatus.setValue("No Alarm Set");
        contactStatus.setValue("No Contact Set");
    }


    public MutableLiveData<Boolean> getTimePickerFlag() {
        if(timePickerFlag == null) {
            timePickerFlag = new MutableLiveData<>();
        }
        return timePickerFlag;
    }

    public MutableLiveData<String> getConnectionStatus() {
        return connectionStatus;
    }

    public MutableLiveData<String> getAlarmStatus() {
        return alarmStatus;
    }

    public MutableLiveData<String> getContactStatus() {
        return contactStatus;
    }


}
