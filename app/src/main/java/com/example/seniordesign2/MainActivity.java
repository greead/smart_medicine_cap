package com.example.seniordesign2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.seniordesign2.ui.main.DeviceListFragment;
import com.example.seniordesign2.ui.main.DeviceListViewModel;

import java.util.ArrayList;

/**
 * Main activity
 * Controls fragment flow and handles all general operations
 */
public class  MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BT_PERMS = 2;
    private static final int REQUEST_SELECT_DEVICE = 3;
    private static final int REQUEST_BOND_STATE_CHANGE = 4;
    private static final String[] BT_PERMS = {"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH_CONNECT"};
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothViewModel bluetoothViewModel;
    private DeviceListViewModel deviceListViewModel;
    private DeviceLogViewModel deviceLogViewModel;
    private BluetoothLeService bluetoothService;
    private boolean connected = false;
    private DeviceLog db;
    private DeviceLog.LogDao logDao;

    private ArrayList<DeviceLog.Entry> entries;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService != null) {
                if(!bluetoothService.initialize(bluetoothViewModel)) {
                    Log.e("APPDEBUG", "Unable to initialize bluetooth");
                    finish();
                }
                // perform device connection
                bluetoothService.connect();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                bluetoothViewModel.getConnectionStatus()
                        .setValue(BluetoothProfile.STATE_CONNECTED);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                bluetoothViewModel.getConnectionStatus()
                        .setValue(BluetoothProfile.STATE_DISCONNECTED);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                displayGattServices(bluetoothService.getSupportedGattServices());
                bluetoothViewModel.getGattServices()
                        .setValue(bluetoothService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_CHAR_DATA_READ.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                bluetoothViewModel.getExtraData()
                        .setValue(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 7);
//        }
//        SMSHandler smsHandler = new SMSHandler(this);
//        smsHandler.sendSMS("test message", "");

        // Set up device log vm
        deviceLogViewModel = new ViewModelProvider(this).get(DeviceLogViewModel.class);

        // Set up database
        db = Room.databaseBuilder(this, DeviceLog.class, "medicine_log").build();
        deviceLogViewModel.getDeviceLog().setValue(db);
        logDao = db.logDao();
        deviceLogViewModel.getLogDao().setValue(logDao);
        Observer<ArrayList<DeviceLog.Entry>> selectedEntriesObserver = new Observer<ArrayList<DeviceLog.Entry>>() {
            @Override
            public void onChanged(ArrayList<DeviceLog.Entry> list) {
                entries = list;
            }
        };
        deviceLogViewModel.getSelectedEntries().observe(this, selectedEntriesObserver);
        deviceLogViewModel.getAllDevices();

        // Set up device list vm
        deviceListViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);

        // Register receiver for paired device state change
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                retrieveDevices();
            }
        }, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        // Bluetooth vm setup
        bluetoothViewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);

        // Request BT permissions
        requestBtPerms();

        // Create gatt service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Set up observer to connect when the attempt connect flag is true
        Observer<Boolean> connectFlagObserver = connectFlag -> {
            if(connectFlag == true && bluetoothService != null) {
                final boolean result = bluetoothService.connect();
                Log.e("APPDEBUG", "Connection Request Result = " + result);
                bluetoothViewModel.getAttemptConnectFlag().setValue(false);
            }
        };
        bluetoothViewModel.getAttemptConnectFlag().observe(this, connectFlagObserver);

        // Get the bluetooth manager and update the vm
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothViewModel.getBluetoothManager().setValue(bluetoothManager);

        // Get the bluetooth adapter and update the vm
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothViewModel.getBluetoothAdapter().setValue(bluetoothAdapter);

        // Get the companion device manager and update the vm
        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        bluetoothViewModel.getDeviceManager().setValue(deviceManager);

        // Request enable bt if it is not on
        requestEnableBt(bluetoothAdapter);

        // Retrieve paired device list
        retrieveDevices();

        // Set up default fragment (Device List)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DeviceListFragment.newInstance())
                    .commitNow();
        }

        // Set up selection item observer
        deviceListViewModel.getSelectedItem().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, DeviceDialogFragment.newInstance(position))
                        .commitNow();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the bluetooth receiver
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            final boolean result = bluetoothService.connect();
            Log.d("APPDEBUG", "Connect request result = " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the bluetooth receiver
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED );
        return intentFilter;
    }

    private void retrieveDevices() {
        try {
            ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
            ArrayList<BluetoothDevice> acceptedDevices = new ArrayList<>();
            for (BluetoothDevice pairedDevice : pairedDevices) {
                if(pairedDevice.getName().equals("SMART_MEDICINE_CAP")) {
                    acceptedDevices.add(pairedDevice);
                }
            }
            bluetoothViewModel.getPairedDevices().setValue(acceptedDevices);
        } catch (SecurityException e) {
            Toast.makeText(this, "Could not retrieve bluetooth devices", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestEnableBt(BluetoothAdapter bluetoothAdapter) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestBtPerms();
            try {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } catch (SecurityException e) {
                Toast.makeText(this, "Could not enable bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void requestBtPerms() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(BT_PERMS, REQUEST_BT_PERMS);
        }
    }

    private void displayGattServices(ArrayList<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        for(BluetoothGattService gattService : gattServices) {
            Log.e("SERVICEDEBUG", gattService.getUuid().toString());
            for(BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                Log.e("SERVICEDEBUG", characteristic.toString());
            }

        }
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, DeviceListFragment.newInstance())
                .commitNow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    retrieveDevices();
                }
                break;
            }
            case REQUEST_BT_PERMS: {
                if (resultCode == RESULT_OK) {

                }
                break;
            }
            case REQUEST_SELECT_DEVICE: {
                if (resultCode == RESULT_OK) {
                    ScanResult scanResult = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
                    if(scanResult != null) {
                        try {
                            scanResult.getDevice().createBond();


                        } catch (SecurityException e) {
                            Toast.makeText(this, "Could not pair to device", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            }

        }


    }
}