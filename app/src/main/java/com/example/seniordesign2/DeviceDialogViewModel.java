package com.example.seniordesign2;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeviceDialogViewModel extends ViewModel {

    private MutableLiveData<Boolean> timePickerFlag;

    public MutableLiveData<Boolean> getTimePickerFlag() {
        if(timePickerFlag == null) {
            timePickerFlag = new MutableLiveData<>();
        }
        return timePickerFlag;
    }


}
