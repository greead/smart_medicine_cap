package com.example.seniordesign2.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeviceListViewModel extends ViewModel {

    private MutableLiveData<Integer> selectedItem;

    public MutableLiveData<Integer> getSelectedItem() {
        if(selectedItem == null) {
            selectedItem = new MutableLiveData<>();
        }
        return selectedItem;
    }

}
