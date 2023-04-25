package com.example.seniordesign2;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

/**
 * Main activity
 * Controls fragment flow and handles all general operations
 */
public class  MainActivity extends AppCompatActivity implements DeviceDialogFragment.OnViewLogsListener {

    // Log Tags
    private static final String DEBUG_TAG = "APP-DEBUG";

    // Activity Flags
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_BT_PERMS = 2;
    public static final int REQUEST_SELECT_DEVICE = 3;
    public static final int REQUEST_SMS_PERMS = 4;
    public static final int REQUEST_ALARM_PERMS = 5;


    // Permission Arrays
    private static final String[] BT_PERMS = {"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH_CONNECT"};
    private static final String[] SMS_PERMS = {"Manifest.permission.SEND_SMS"};


    // Bluetooth Fields
    private BluetoothLeService bluetoothService;

    // View Models
    private BluetoothViewModel bluetoothViewModel;
    private DeviceListViewModel deviceListViewModel;
    private DatabaseViewModel databaseViewModel;
    private DeviceDialogViewModel deviceDialogViewModel;

    // Local Database
    private LocalDatabase db;

    // Device UUIDs
    private static final String SERVICE_UUID = "4FAFC201-1FB5-459E-8FCC-C5C9C331914B";
    private static final String BATTERY_CHAR_UUID = "BEB5483E-36E1-4688-B7F5-EA07361B26A8";
    private static final String OPEN_CLOSE_CHAR_UUID = "";
    private static final String DEVICE_NAME = "SMART_MEDICINE_CAP";

    // Fragment Stack
    private FragmentStack fragmentStack;

    // Bluetooth Service Connection
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService != null) {
                if(!bluetoothService.initialize(bluetoothViewModel, deviceDialogViewModel, databaseViewModel)) {
                    Log.e(DEBUG_TAG, "Unable to initialize bluetooth");
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

    // Bluetooth Broadcast Receiver
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED:

                    bluetoothViewModel.getConnectionStatus().setValue(BluetoothProfile.STATE_CONNECTED);

                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:

                    bluetoothViewModel.getConnectionStatus().setValue(BluetoothProfile.STATE_DISCONNECTED);

                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:

                    bluetoothViewModel.getGattServices().setValue(bluetoothService.getSupportedGattServices());
                    displayGattServices(bluetoothViewModel.getGattServices().getValue());

                    break;
                case BluetoothLeService.ACTION_CHAR_DATA_READ:

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    String extraData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    Log.e(DEBUG_TAG, intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    bluetoothViewModel.getExtraData().setValue(extraData);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up device list, device log, and bluetooth view models
        setUpViewModels();

        // Set up device logs, alarm logs, and contacts logs
        setUpDatabase();

        // Set up bluetooth
        setUpBluetoothLe();

        // Request enable bt if it is not on
        requestEnableBt(Objects.requireNonNull(bluetoothViewModel.getBluetoothAdapter().getValue()));



        // Set up fragment stack
        fragmentStack = new FragmentStack(getSupportFragmentManager(), R.id.container);

        // Set up initial fragment (Device List)
        if (savedInstanceState == null) fragmentStack.setCurrent(DeviceListFragment.newInstance());

        // Set up selection item observer
        deviceListViewModel.getSelectedItem().observe(this, position -> fragmentStack.setCurrent(DeviceDialogFragment.newInstance(position, this)));

        // Retrieve paired device list
        retrievePairedDevices();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the bluetooth receiver
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            final boolean result = bluetoothService.connect();
            Log.e(DEBUG_TAG, "Connect request result = " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the bluetooth receiver
        // unregisterReceiver(gattUpdateReceiver);
    }

    private void setUpDatabase() {
        // Build database
        db = Room.databaseBuilder(this, LocalDatabase.class, "local_database").build();

        // Get the DAO's
        databaseViewModel.getDeviceDao().setValue(db.deviceDao());
        databaseViewModel.getAlarmDao().setValue(db.alarmDao());
        databaseViewModel.getContactDao().setValue(db.contactDao());
    }

    private void setUpViewModels() {
        // Set up device log vm
        databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);

        // Set up device list vm
        deviceListViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);

        // Bluetooth vm setup
        bluetoothViewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);

        // Set up device dialog vm
        deviceDialogViewModel = new ViewModelProvider(this).get(DeviceDialogViewModel.class);

    }

    private void setUpBluetoothLe() {

        // Request BT permissions
        requestBtPerms();

        // Get the bluetooth manager
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);

        // Get the bluetooth adapter and update the vm
        bluetoothViewModel.getBluetoothAdapter().setValue(bluetoothManager.getAdapter());

        // Get the companion device manager and update the vm
        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        bluetoothViewModel.getDeviceManager().setValue(deviceManager);

        // Register receiver for paired device state change
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                retrievePairedDevices();
            }
        }, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        // Create gatt service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Set up observer to connect when the attempt connect flag is true
        Observer<Boolean> connectFlagObserver = connectFlag -> {
            if(connectFlag && bluetoothService != null) {
                final boolean result = bluetoothService.connect();
                Log.e(DEBUG_TAG, "Connection Request Result = " + result);
                bluetoothViewModel.getAttemptConnectFlag().setValue(false);
            }
        };
        bluetoothViewModel.getAttemptConnectFlag().observe(this, connectFlagObserver);

        
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED );
        return intentFilter;
    }

    private void retrievePairedDevices() {
        try {
            ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>(bluetoothViewModel.getBluetoothAdapter().getValue().getBondedDevices());
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
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    switch (result.getResultCode()) {
                        case RESULT_OK:
                            retrievePairedDevices();
                            Log.e(DEBUG_TAG, "Bluetooth enable request successful");
                            break;
                        case RESULT_CANCELED:
                            Log.e(DEBUG_TAG, "Bluetooth enable request cancelled");
                            break;
                    }
                }).launch(enableBtIntent);
            } catch (SecurityException e) {
                Log.e(DEBUG_TAG, "Could not enable bluetooth due to permissions");
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
            Log.e(DEBUG_TAG, gattService.getUuid().toString() + " | " + gattService);
            for(BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                Log.e(DEBUG_TAG, "\t" + characteristic.getUuid().toString() + " | " + characteristic);
                if(characteristic.getUuid().toString().equals("beb5483e-36e1-4688-b7f5-ea07361b26a8")) {
                    bluetoothService.readCharacteristic(characteristic);
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        fragmentStack.setPrevious();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_DEVICE) {
            Log.e(DEBUG_TAG, "REQUEST SELECT DEVICE: " + resultCode);
//            if (resultCode == RESULT_OK) {
//                ScanResult scanResult = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
//                if (scanResult != null) {
//                    try {
//                        Log.e(DEBUG_TAG, "BOND RESULT: " + scanResult.getDevice().createBond());
//
//
//                    } catch (SecurityException e) {
//                        Toast.makeText(this, "Could not pair to device", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
        } else if(requestCode == REQUEST_ALARM_PERMS) {
            if (resultCode == RESULT_OK) {
                deviceDialogViewModel.getTimePickerFlag().setValue(true);
            }
        }


    }

    @Override
    public void onButtonShowLogsPressed(DeviceLogViewModel deviceLogViewModel) {;
        fragmentStack.setCurrent(DeviceLogFragment.newInstance(deviceLogViewModel));
    }
}