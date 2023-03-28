package com.example.seniordesign2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BluetoothLeService extends Service {
    // Constants
    public final static String ACTION_GATT_CONNECTED = "SENIOR_DESIGN.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "SENIOR_DESIGN.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "SENIOR_DESIGN.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_CHAR_DATA_READ = "SENIOR_DESIGN.ACITON_CHAR_DATA_READ";
    public final static String EXTRA_DATA = "SENIOR_DESIGN.EXTRA_DATA";
    private IBinder binder = new LocalBinder();
    private BluetoothViewModel bluetoothViewModel;
    private BluetoothGatt bluetoothGatt;
    private int connectionState;
    private int connectionAttempts = 0;


    /**
     * Callback for GATT events
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("APPDEBUG", "Connection State: " + newState );
            if(status != BluetoothGatt.GATT_SUCCESS) {
                try {
                    bluetoothGatt.close();
                } catch (SecurityException e) {
                    Log.e("APPDEBUG", "Could not close GATT due to permissions");
                }
                bluetoothGatt = null;
                if (connectionAttempts < 5) {
                    connectionAttempts ++;
                    connect();
                    return;
                } else {
                    connectionAttempts = 0;
                    return;
                }
            }
            Log.e("APPDEBUG", "Connection Status: " + status);
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.e("APPDEBUG", "Connected");
                connectionState = BluetoothProfile.STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
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

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("APPDEBUG", "onCharacteristicRead received" + characteristic.toString());
                broadcastUpdate(ACTION_CHAR_DATA_READ, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        sendBroadcast(new Intent(action));
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte datum : data) {
                stringBuilder.append(String.format("%02X", datum));
            }
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            Log.e("APPDEBUG", "Broadcasting Characteristic: " + new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize(BluetoothViewModel bluetoothViewModel) {
        this.bluetoothViewModel = bluetoothViewModel;
        if (bluetoothViewModel == null) {
            Log.e("APPDEBUG", "Unable to obtain BluetoothViewModel");
            return false;
        }
        return true;
    }

    public boolean connect() {
        if (bluetoothViewModel == null || bluetoothViewModel.getBluetoothDevice().getValue() == null) {
            Log.e("APPDEBUG", "BluetoothViewModel not initialized or unspecified device");
            return false;
        }
        try {
            final BluetoothDevice device = bluetoothViewModel.getBluetoothDevice().getValue();
            // Connect to GATT server
            Log.e("APPDEBUG", "Device attempting to connect");
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);

            return true;
        } catch (IllegalArgumentException e) {
            Log.e("APPDEBUG", "Device not found");
            return false;
        } catch (SecurityException e) {
            Log.e("APPDEBUF", "Cannot connect due to permissions");
            return false;
        }
    }

    public void disconnect() {
        if (bluetoothGatt == null) {
            Log.e("APPDEBUG", "Not connected");
            return;
        }
        try {
            bluetoothGatt.disconnect();
        } catch (SecurityException e) {
            Log.e("APPDEBUG", "Could not disconnect due to permissions");
        }
    }

    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        try {
            bluetoothGatt.close();
            bluetoothGatt = null;
        } catch (SecurityException e) {
            Log.e("APPDEBUG", "Could not close GATT server due to permissions");
        }
    }

    public void readCharacteristic (BluetoothGattCharacteristic characteristic) {
        if(bluetoothGatt == null) {
            Log.e("APPDEBUG", "No GATT initialized");
            return;
        }
        try {
            bluetoothGatt.readCharacteristic(characteristic);
        } catch (SecurityException e) {
            Log.e("APPDEBUG", "Could not read characteristic due to permissions");
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if(bluetoothGatt == null) {
            Log.e("APPDEBUG", "No GATT initialized");
            return;
        }
        try {
            bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        } catch (SecurityException e) {
            Log.e("APPDEBUG", "Could not set characteristic notifications due to permissions");
        }
    }

    public ArrayList<BluetoothGattService> getSupportedGattServices() {
        if(bluetoothGatt == null) return null;
        return new ArrayList<>(bluetoothGatt.getServices());
    }

}
