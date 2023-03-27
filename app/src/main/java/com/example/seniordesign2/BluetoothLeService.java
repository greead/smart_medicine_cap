package com.example.seniordesign2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BluetoothLeService extends Service {
    // Constants
    public final static String ACTION_GATT_CONNECTED = "SENIOR_DESIGN.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "SENIOR_DESIGN.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "SENIOR_DESIGN.ACTION_GATT_SERVICES_DISCOVERED";
    private Binder binder = new LocalBinder();
    private BluetoothViewModel bluetoothViewModel;
    private BluetoothGatt bluetoothGatt;
    private int connectionState;

    // TODO Discover Services

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("APPDEBUG", "Connection State: " + newState );
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.e("APPDEBUG", "Connected");
                broadcastUpdate(ACTION_GATT_CONNECTED);
                connectionState = BluetoothProfile.STATE_CONNECTED;
                try {
                    bluetoothGatt.discoverServices();
                } catch (SecurityException e) {
                    Log.e("APPDEBUG", "Could not perform service discovery due to permissions");
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("APPDEBUG", "Disconnected");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                connectionState = BluetoothProfile.STATE_DISCONNECTED;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e("APPDEBUG", "onServicesDiscovered received: " + status);
            }

        }
    };



    public boolean initialize(BluetoothViewModel bluetoothViewModel) {
        this.bluetoothViewModel = bluetoothViewModel;
        if (bluetoothViewModel == null) {
            Log.e("APPDEBUG", "Unable to obtain BluetoothViewModel");
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
    // TODO CONNECT TO A DEVICE
    public boolean connect() {
        if (bluetoothViewModel == null || bluetoothViewModel.getBluetoothDevice().getValue() == null) {
            Log.e("APPDEBUG", "BluetoothViewModel not initialized or unspecified device");
            return false;
        }
        try {
            final BluetoothDevice device = bluetoothViewModel.getBluetoothDevice().getValue();
            // Connect to GATT server
            Log.e("APPDEBUG", "Device attempting to connect");
            bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);

            return true;
        } catch (IllegalArgumentException e) {
            Log.e("APPDEBUG", "Device not found");
            return false;
        } catch (SecurityException e) {
            Log.e("APPDEBUF", "Cannot connect due to permissions");
            return false;
        }
    }

    private void broadcastUpdate(final String action) {
        sendBroadcast(new Intent(action));
    }

    public ArrayList<BluetoothGattService> getSupportedGattServices() {
        if(bluetoothGatt == null) return null;
        return (ArrayList<BluetoothGattService>) bluetoothGatt.getServices();
    }

}
