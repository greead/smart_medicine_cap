package com.example.seniordesign2;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Fragment for displaying device dialog and BLE transactions
 */
public class DeviceDialogFragment extends Fragment implements AlarmsHandler.DatabaseAlarmUpdater {
    private static final String[] ALARM_PERMS = {"Manifest.permission.SET_EXACT_ALARM"};
    private static final String[] SMS_PERMS = {"android.permission.SEND_SMS"};
    private static final String ARG_POSITION = "POSITION";
    private int position;
    private BluetoothDevice selectedDevice;
    private BluetoothViewModel bluetoothViewModel;

    private DeviceDialogViewModel deviceDialogViewModel;
    private DeviceLogViewModel deviceLogViewModel;
    private DatabaseViewModel databaseViewModel;
    private OnViewLogsListener onViewLogsListener;

    // UI Components
    private Button btnConnectDevice;
    private Button btnViewLogs;
    private Button btnSetContact;
    private Button btnSetAlarm;
    private Button btnChangeDeviceName;
    private TextView txtDeviceNameTitle;
    private TextView txtConnectionStatus;
    private TextView txtContactStatus;
    private TextView txtAlarmStatus;
    private AlarmsHandler alarmsHandler;

    private SMSHandler smsHandler;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    public DeviceDialogFragment() {
        // Required empty public constructor
    }

    public static DeviceDialogFragment newInstance(int position, OnViewLogsListener onViewLogsListener) {
        DeviceDialogFragment fragment = new DeviceDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        fragment.setOnViewLogsListener(onViewLogsListener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);

            // Get bluetooth view model from main activity
            bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
            databaseViewModel = new ViewModelProvider(requireActivity()).get(DatabaseViewModel.class);

            Log.e("APPDEBUG", "onCreate");

            // Observe change in device connection status from bluetooth vm
            Observer<Integer> connectionStatusObserver = state -> {
                if(state == BluetoothProfile.STATE_CONNECTED) {
                    btnConnectDevice.setText(R.string.connected);
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    btnConnectDevice.setText(R.string.disconnected);
                } else {
                    btnConnectDevice.setText(R.string.click_to_connect);
                }
            };
            bluetoothViewModel.getConnectionStatus().observe(this, connectionStatusObserver);

            deviceDialogViewModel = new ViewModelProvider(requireActivity()).get(DeviceDialogViewModel.class);
              deviceDialogViewModel.getTimePickerFlag().observe(this, observedValue -> {
                if(observedValue) {
                    showTimePicker();
                    deviceDialogViewModel.getTimePickerFlag().setValue(false);
                }
            });
            deviceDialogViewModel.getContactStatus().observe(this, observedValue -> {
                txtContactStatus.setText(observedValue);
            });
            deviceDialogViewModel.getConnectionStatus().observe(this, observedValue -> {
                txtConnectionStatus.setText(observedValue);
            });
            deviceDialogViewModel.getAlarmStatus().observe(this, observedValue -> {
                txtAlarmStatus.setText(observedValue);
            });

            deviceLogViewModel = new ViewModelProvider(this).get(DeviceLogViewModel.class);

            alarmsHandler = new AlarmsHandler(requireContext(), deviceDialogViewModel, this);
            smsHandler = new SMSHandler(requireContext());

            activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                switch (result.getResultCode()) {
                    case RESULT_OK:
                        Log.e("APPDEBUG", "INTENT RESULT OK");
                        Uri contactUri = result.getData().getData();
                        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                        Cursor cursor = getActivity().getContentResolver().query(contactUri, projection, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String number = cursor.getString(numberIndex);
                            databaseViewModel.insertContactLogs( new LocalDatabase.ContactLog(
                                    selectedDevice.getAddress(),
                                    number
                            ));
                            databaseViewModel.getContactLogs(selectedDevice.getAddress());
                            checkSMSPermissions();
                            try {
                                smsHandler.sendSMS("Your phone number has been registered for medication emergencies", number);
                                deviceDialogViewModel.getContactStatus().setValue(number);
                            } catch (SecurityException e) {
                                Log.e("APPDEBUG", "Could not send SMS due to permissions");
                            }

                        }
                        Log.e("APPDEBUG", "Contact intent successful");
                        try {
                            cursor.close();
                        } catch (NullPointerException e) {
                            Log.e("APPDEBUG", "Cursor could not close");
                        }
                        break;
                    case RESULT_CANCELED:
                        Log.e("APPDEBUG", "Contact intent cancelled");
                        break;
                }
            });


        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("APPDEBUG", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_device_dialog, container, false);
        selectedDevice = bluetoothViewModel.getPairedDevices().getValue().get(position);
        bluetoothViewModel.getBluetoothDevice().setValue(selectedDevice);
//        databaseViewModel.getAllAndWait(selectedDevice.getAddress());

        txtConnectionStatus = view.findViewById(R.id.txtConnectionStatus);
        txtConnectionStatus.setText(deviceDialogViewModel.getConnectionStatus().getValue());

        txtContactStatus = view.findViewById(R.id.txtContactStatus);
        txtContactStatus.setText(deviceDialogViewModel.getContactStatus().getValue());
        try {
            databaseViewModel.getContactLogs(selectedDevice.getAddress());
            databaseViewModel.getSelectedContactLogs().observe(getViewLifecycleOwner(), contactLogs -> {
                if (contactLogs.size() > 0) {
                    txtContactStatus.setText(contactLogs.get(0).phoneNumber);
                } else {
                    txtContactStatus.setText(R.string.no_contact_set);
                }
            });
        } catch (Exception e) {
            Log.e("APPDEBUG", "Exception: " + e.getMessage());
        }

        txtAlarmStatus = view.findViewById(R.id.txtAlarmStatus);
        txtAlarmStatus.setText(deviceDialogViewModel.getAlarmStatus().getValue());
        try {

            databaseViewModel.getSelectedAlarmLogs().observe(requireActivity(), alarmLogs -> {
                if (alarmLogs.size() > 0) {
                    String newText = alarmLogs.get(0).hour + ":" + alarmLogs.get(0).minute;
                    txtAlarmStatus.setText(newText);
                } else {
                    txtAlarmStatus.setText(R.string.no_alarm_set);
                }
            });
            databaseViewModel.getAlarmLogs(selectedDevice.getAddress());
        } catch (Exception e) {
            Log.e("APPDEBUG", "Exception: " + e.getMessage());
        }

        txtDeviceNameTitle = view.findViewById(R.id.txtDeviceAlias);
        txtDeviceNameTitle.setText(" ");
        try {
            txtDeviceNameTitle.setText(selectedDevice.getName());
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Could not get device details", Toast.LENGTH_LONG).show();
        }

        btnConnectDevice = view.findViewById(R.id.btnConnectDevice);
        btnConnectDevice.setOnClickListener(buttonView -> {
            bluetoothViewModel.getAttemptConnectFlag().setValue(true);
        });

        btnViewLogs = view.findViewById(R.id.btnViewLogs);
        btnViewLogs.setOnClickListener(buttonView -> {
            databaseViewModel.getDeviceLogs(selectedDevice.getAddress());
            deviceLogViewModel.getDeviceLogs().setValue(databaseViewModel.getSelectedDeviceLogs().getValue());
            onViewLogsListener.onButtonShowLogsPressed(deviceLogViewModel);
        });

        btnSetAlarm = view.findViewById(R.id.btnSetAlarm);
        btnSetAlarm.setOnClickListener(buttonView -> {
            if(checkAlarmPermissions()) {
                showTimePicker();
            }
        });

        btnSetContact = view.findViewById(R.id.btnSetContact);
        btnSetContact.setOnClickListener(buttonView -> {
            selectContact();
        });

        btnChangeDeviceName = view.findViewById(R.id.btnChangeDeviceName);
        btnChangeDeviceName.setOnClickListener(buttonView -> {
            // TODO
        });
        btnChangeDeviceName.setEnabled(false);


        databaseViewModel.getSelectedDeviceLogs().observe(requireActivity(), observedValue -> {
            deviceLogViewModel.getDeviceLogs().setValue(observedValue);
        });

        databaseViewModel.getDeviceLogs(selectedDevice.getAddress());

        return view;
    }

    private boolean checkAlarmPermissions() {
        if (requireActivity().checkSelfPermission("android.permission.SCHEDULE_EXACT_ALARM") != PackageManager.PERMISSION_GRANTED) {
            requireActivity().requestPermissions(ALARM_PERMS, MainActivity.REQUEST_ALARM_PERMS);
            return false;
        }
        return true;
    }

    private boolean checkSMSPermissions() {
        if (requireActivity().checkSelfPermission("Manifest.permission.SEND_SMS") != PackageManager.PERMISSION_GRANTED) {
            requireActivity().requestPermissions(SMS_PERMS, 7);
            return false;
        }
        return true;
    }

    private void showTimePicker() {
        alarmsHandler.getTimePickerFragment(this).show(getChildFragmentManager(), "Time Picker Dialog");
    }

    public void setOnViewLogsListener(OnViewLogsListener onViewLogsListener) {
        this.onViewLogsListener = onViewLogsListener;
    }

    public interface OnViewLogsListener {
        void onButtonShowLogsPressed(DeviceLogViewModel deviceLogViewModel);
    }

    private void setAlarmStatus(String newStatus) {

    }

    private void selectContact() {
        Log.e("APPDEBUG", "SELECT CONTACT CALLED");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            try {
                activityResultLauncher.launch(intent);
            } catch (SecurityException e) {
                Log.e("APPDEBUG", "Could not launch intent for contact phone number");
            }

//        }
    }

    @Override
    public void UpdateAlarms(int hour, int minute) {
        databaseViewModel.insertAlarmLogs(new LocalDatabase.AlarmLog(selectedDevice.getAddress(), hour, minute));
    }
}