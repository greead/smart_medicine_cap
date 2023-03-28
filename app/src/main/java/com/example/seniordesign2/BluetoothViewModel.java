package com.example.seniordesign2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * View model for live data related to BLE activities and transactions
 */
public class BluetoothViewModel extends ViewModel {
    public static final String DEVICE_NAME = "SMART_MEDICINE_CAP";
    public static final int REQUEST_SELECT_DEVICE = 3;
    private MutableLiveData<ArrayList<BluetoothDevice>> pairedDevices;
    private MutableLiveData<BluetoothManager> bluetoothManager;
    private MutableLiveData<BluetoothAdapter> bluetoothAdapter;
    private MutableLiveData<BluetoothDevice> bluetoothDevice;
    private BluetoothLeDeviceFilter deviceFilter;
    private AssociationRequest pairingRequest;
    private MutableLiveData<CompanionDeviceManager> deviceManager;
    private MutableLiveData<ArrayList<ScanResult>> scanResultsList;
    private MutableLiveData<Integer> connectionStatus;
    private MutableLiveData<ArrayList<BluetoothGattService>> gattServices;
    private MutableLiveData<String> extraData;
    private MutableLiveData<Boolean> attemptConnectFlag;

    public BluetoothViewModel() {
        deviceFilter = new BluetoothLeDeviceFilter.Builder()
                .setNamePattern(Pattern.compile(DEVICE_NAME))
                .build();
        pairingRequest = new AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build();
    }

    public MutableLiveData<ArrayList<BluetoothDevice>> getPairedDevices() {
        if(pairedDevices == null) {
            pairedDevices = new MutableLiveData<>();
            pairedDevices.setValue(new ArrayList<>());
        }
        return pairedDevices;
    }

    public MutableLiveData<BluetoothManager> getBluetoothManager() {
        if(bluetoothManager == null) {
            bluetoothManager = new MutableLiveData<>();
        }
        return bluetoothManager;
    }

    public MutableLiveData<BluetoothAdapter> getBluetoothAdapter() {
        if(bluetoothAdapter == null) {
            bluetoothAdapter = new MutableLiveData<>();
        }
        return bluetoothAdapter;
    }

    public MutableLiveData<CompanionDeviceManager> getDeviceManager() {
        if(deviceManager == null) {
            deviceManager = new MutableLiveData<>();
        }
        return deviceManager;
    }

    public MutableLiveData<ArrayList<ScanResult>> getScanResultsList() {
        if(scanResultsList == null) {
            scanResultsList = new MutableLiveData<>();
        }
        return scanResultsList;
    }

    public MutableLiveData<BluetoothDevice> getBluetoothDevice() {
        if(bluetoothDevice == null) {
            bluetoothDevice = new MutableLiveData<>();
        }
        return bluetoothDevice;
    }

    public MutableLiveData<Integer> getConnectionStatus() {
        if(connectionStatus == null) {
            connectionStatus = new MutableLiveData<>();
        }
        return connectionStatus;
    }

    public MutableLiveData<ArrayList<BluetoothGattService>> getGattServices() {
        if(gattServices == null) {
            gattServices = new MutableLiveData<>();
            gattServices.setValue(new ArrayList<>());
        }
        return gattServices;
    }

    public MutableLiveData<String> getExtraData() {
        if(extraData == null) {
            extraData = new MutableLiveData<>();
        }
        return extraData;
    }

    public MutableLiveData<Boolean> getAttemptConnectFlag() {
        if(attemptConnectFlag == null) {
            attemptConnectFlag = new MutableLiveData<>();
            attemptConnectFlag.setValue(false);
        }
        return attemptConnectFlag;
    }

    public BluetoothLeDeviceFilter getDeviceFilter() {
        return deviceFilter;
    }

    public AssociationRequest getPairingRequest() {
        return pairingRequest;
    }
}
