package com.example.seniordesign2.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * View model for live data related to the device list
 */
public class DeviceListViewModel extends ViewModel {

    private MutableLiveData<Integer> selectedItem;

    public MutableLiveData<Integer> getSelectedItem() {
        if(selectedItem == null) {
            selectedItem = new MutableLiveData<>();
        }
        return selectedItem;
    }

}
