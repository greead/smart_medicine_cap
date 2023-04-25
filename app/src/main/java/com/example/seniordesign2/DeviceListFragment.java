package com.example.seniordesign2;

import static android.app.Activity.RESULT_OK;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.IntentSender;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Fragment or displaying the device list and allowing pairing
 */
public class DeviceListFragment extends Fragment implements DeviceListAdapter.OnNoteListener {

    // View models
    private BluetoothViewModel bluetoothViewModel;
    private DeviceListViewModel deviceListViewModel;

    // View
    private View view;

    // UI Components
    private Button btnScanDevices;
    private RecyclerView lstDeviceList;
    private RecyclerView.LayoutManager layoutManager;
    private DeviceListAdapter deviceListAdapter;

    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    public DeviceListFragment() {
        // EMPTY CONSTRUCTOR
    }

    public static DeviceListFragment newInstance() {
        return new DeviceListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                ScanResult scanResult = result.getData().getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
                if (scanResult != null) {
                    try {
                        Log.e("APPDEBUG", "BOND RESULT: " + scanResult.getDevice().createBond());

                    } catch (SecurityException e) {
                        Toast.makeText(requireActivity(), "Could not pair to device", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_list, container, false);

        bluetoothViewModel = new ViewModelProvider(getActivity()).get(BluetoothViewModel.class);
        deviceListViewModel = new ViewModelProvider(getActivity()).get(DeviceListViewModel.class);

        // "Scan Devices" Button setup
        btnScanDevices = view.findViewById(R.id.btnScanDevices);
        btnScanDevices.setOnClickListener(view -> {
            bluetoothViewModel.getDeviceManager().getValue().associate(bluetoothViewModel.getPairingRequest(), new CompanionDeviceManager.Callback() {
                public void onDeviceFound(@NonNull IntentSender chooserLauncher) {
                    Log.e("APPDEBUG", "DEVICE FOUND");
                    try {
                        activityResultLauncher.launch(
                                new IntentSenderRequest.Builder(chooserLauncher)
                                        .setFillInIntent(null)
                                        .build()
                        );
//                        requireActivity().startIntentSenderForResult(chooserLauncher, bluetoothViewModel.REQUEST_SELECT_DEVICE, null, 0, 0, 0);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Failed to send intent", Toast.LENGTH_LONG).show();
                        Log.e("APPDEBUG", "Exception: " + e.getMessage());
                    }
                }
                @Override
                public void onFailure(@Nullable CharSequence charSequence) {
                    Toast.makeText(getActivity(), "Failed to send intent", Toast.LENGTH_LONG).show();
                }
            }, null);
        });

        layoutManager = new LinearLayoutManager(getContext());
        deviceListAdapter = new DeviceListAdapter(bluetoothViewModel.getPairedDevices().getValue(), this);
        lstDeviceList = view.findViewById(R.id.lstDeviceList);
        lstDeviceList.setAdapter(deviceListAdapter);
        lstDeviceList.setLayoutManager(layoutManager);

        // Device List setup
        final Observer<ArrayList<BluetoothDevice>> deviceListObserver = newList -> {
            try {
                deviceListAdapter = new DeviceListAdapter(bluetoothViewModel.getPairedDevices().getValue(), this);
                lstDeviceList.setAdapter(deviceListAdapter);
                Log.e("APPDEBUG", "RECYCLER UPDATE");
            } catch (NullPointerException e) {
                Log.e("APPDEBUG", "Null Device List");
            }
        };
        bluetoothViewModel.getPairedDevices().observe(getViewLifecycleOwner(), deviceListObserver);

        return view;
    }

    @Override
    public void onNoteClick(int position) {
        deviceListViewModel.getSelectedItem().setValue(position);
        Log.e("APPDEBUG", "GOT POSITION: " + position);
    }


}