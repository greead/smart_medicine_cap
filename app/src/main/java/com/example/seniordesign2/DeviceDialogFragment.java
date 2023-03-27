package com.example.seniordesign2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class DeviceDialogFragment extends Fragment {
    private static final String ARG_POSITION = "POSITION";
    private int position;
    private BluetoothDevice selectedDevice;
    private BluetoothViewModel bluetoothViewModel;
    private Button btnConnectDevice;
    private TextView txtDeviceNameTitle;
    private Observer<Integer> connectionStatusObserver;
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
            connectionStatusObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer state) {
                    if(state == BluetoothProfile.STATE_CONNECTED) {
                        btnConnectDevice.setText("Connected");
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        btnConnectDevice.setText("Disconnected");
                    } else {
                        btnConnectDevice.setText("Click to Connect");
                    }
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

        txtDeviceNameTitle = view.findViewById(R.id.txtDeviceNameTitle);
        try {
            txtDeviceNameTitle.setText(selectedDevice.getName());
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Could not get device details", Toast.LENGTH_LONG).show();
        }

        btnConnectDevice = view.findViewById(R.id.btnConnectDevice);
        btnConnectDevice.setOnClickListener(buttonView -> {
            bluetoothViewModel.getAttemptConnectFlag().setValue(true);
        });

        return view;
    }



}