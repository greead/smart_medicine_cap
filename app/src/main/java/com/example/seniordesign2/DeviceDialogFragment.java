package com.example.seniordesign2;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Fragment for displaying device dialog and BLE transactions
 */
public class DeviceDialogFragment extends Fragment {
    private static final String ARG_POSITION = "POSITION";
    private int position;
    private BluetoothDevice selectedDevice;
    private BluetoothViewModel bluetoothViewModel;

    // UI Components
    private Button btnConnectDevice;
    private Button btnViewLogs;
    private Button btnSetContact;
    private Button btnSetAlarm;
    private Button btnChangeDeviceName;
    private TextView txtDeviceNameTitle;

    public DeviceDialogFragment() {
        // Required empty public constructor
    }

    public static DeviceDialogFragment newInstance(int position) {
        DeviceDialogFragment fragment = new DeviceDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);

            // Get bluetooth view model from main activity
            bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
            Log.e("APPDEBUG", "onCreate");

            // Observe change in device connection status from bluetooth vm
            Observer<Integer> connectionStatusObserver = state -> {
                if(state == BluetoothProfile.STATE_CONNECTED) {
                    btnConnectDevice.setText("Connected");
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    btnConnectDevice.setText("Disconnected");
                } else {
                    btnConnectDevice.setText("Click to Connect");
                }
            };
            bluetoothViewModel.getConnectionStatus().observe(this, connectionStatusObserver);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("APPDEBUG", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_device_dialog, container, false);
        selectedDevice = bluetoothViewModel.getPairedDevices().getValue().get(position);
        bluetoothViewModel.getBluetoothDevice().setValue(selectedDevice);

        txtDeviceNameTitle = view.findViewById(R.id.txtDeviceAlias);
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
            // TODO
        });

        btnSetAlarm = view.findViewById(R.id.btnSetAlarm);
        btnSetAlarm.setOnClickListener(buttonView -> {
            // TODO
        });

        btnSetContact = view.findViewById(R.id.btnSetContact);
        btnSetContact.setOnClickListener(buttonView -> {
            // TODO
        });

        btnChangeDeviceName = view.findViewById(R.id.btnChangeDeviceName);
        btnChangeDeviceName.setOnClickListener(buttonView -> {
            // TODO
        });

        return view;
    }





}