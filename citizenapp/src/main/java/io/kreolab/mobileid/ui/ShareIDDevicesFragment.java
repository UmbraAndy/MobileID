/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.recyclerviewAdapter.BluetoothDeviceAdapter;
import io.kreolab.mobileidsharedlibrary.service.bluetooth.BluetoothService;


public class ShareIDDevicesFragment extends Fragment implements BluetoothDeviceAdapter.ItemClickedListener {


    private OnDeviceSelectedListener mListener;


    private static final int ENABLE_BLUETOOTH_REQUEST = 100;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 101;

    private Context mContext;
    private boolean mBluetoothPermissionDenied = false;

    @BindView(R.id.pairedDeviceRcv)
    RecyclerView mPairedDeviceRcv;

    @BindView(R.id.availableDeviceRcv)
    RecyclerView mScannedDeviceRcv;

    private BluetoothDeviceAdapter mPairedDevicesBluetoothAdapter;
    private BluetoothDeviceAdapter mAvailableDevicesBluetoothAdapter;


    private List<BluetoothDevice> mPairedDevices;
    private final List<BluetoothDevice> mScannedDeviceList = new ArrayList<>();
    private final Set<BluetoothDevice> mScannedDeviceSet = new HashSet<>();

    private final BroadcastReceiver mBluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the action from the intent
            String bluetoothAction = intent.getAction();
            if (bluetoothAction != null && (bluetoothAction.equalsIgnoreCase(BluetoothDevice.ACTION_FOUND))) {
                //get the device from the intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //check if this is allowed device and the device is not already paired.
                if (isAllowedDeviceType(device) && isAlreadyPaired(device)) {
                    mScannedDeviceSet.add(device);
                    mScannedDeviceList.clear();
                    mScannedDeviceList.addAll(mScannedDeviceSet);
                    mAvailableDevicesBluetoothAdapter.notifyDataSetChanged();
                }

            }
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_iddevices, container, false);
        ButterKnife.bind(this, view);

        //setup recycler view for paired devices
        mPairedDeviceRcv.setLayoutManager(new LinearLayoutManager(mContext));

        //setup recycler view for scanned devices
        mAvailableDevicesBluetoothAdapter = new BluetoothDeviceAdapter(mScannedDeviceList, this);
        mScannedDeviceRcv.setAdapter(mAvailableDevicesBluetoothAdapter);
        mScannedDeviceRcv.setLayoutManager(new LinearLayoutManager(mContext));
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothPermissionDenied) {
            return;
        }
        //check if bluetooth is not enabled. Send request to enable it
        if (!BluetoothService.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_REQUEST);
        } else {//bluetooth is enabled,  so start discovery
            //if permission is already granted start discovery
            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                startDiscovery();
            } else {//permission not granted so request
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION_REQUEST);
            }
        }
        //if bluetooth is available, get paired devices
        if(BluetoothService.isSupported()) {
            mPairedDevices = BluetoothService.getPairedDevices();
            mPairedDevicesBluetoothAdapter = new BluetoothDeviceAdapter(mPairedDevices, this);
            mPairedDeviceRcv.setAdapter(mPairedDevicesBluetoothAdapter);
            registerForBluetoothBroadcast();
        }
        else{
            mListener.serviceValidation("Bluetooth not available");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mBluetoothPermissionDenied) {
            unRegisterBluetoothBroadcast();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    mBluetoothPermissionDenied = false;
                    //check is has permission
                    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        startDiscovery();
                    } else {//has no permission so request
                        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION_REQUEST);
                    }
                } else {
                    mBluetoothPermissionDenied = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_COARSE_LOCATION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery();
                }
                break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnDeviceSelectedListener) {
            mListener = (OnDeviceSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeviceSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClicked(BluetoothDevice device) {
        //BluetoothService.clientConnect(device,mHandler);
        mListener.deviceSelected(device);
    }


    private void startDiscovery() {
        BluetoothService.cancelDiscovery();
        BluetoothService.startDiscovery();
    }

    private boolean isAllowedDeviceType(BluetoothDevice device) {
        boolean allowed = false;
        boolean isComputer = device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER;
        boolean isMobile = device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE;
        if (isComputer || isMobile) {
            allowed = true;
        }
        return allowed;
    }

    private boolean isAlreadyPaired(BluetoothDevice device) {
        boolean alreadyPaired = false;
        if (mPairedDevices.contains(device)) {
            alreadyPaired = true;
        }
        return !alreadyPaired;
    }


    private boolean checkPermission(String permission) {
        boolean hasPermission = false;
        if (ContextCompat.checkSelfPermission(mContext, permission)
                == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true;
        }
        return hasPermission;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions((Activity) mContext, new String[]{permission}, requestCode);
    }

    private void registerForBluetoothBroadcast() {
        //filter to listen to bluetooth action found message
        IntentFilter bluetoothIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mBluetoothBroadcastReceiver, bluetoothIntentFilter);
    }

    private void unRegisterBluetoothBroadcast() {
        if(mBluetoothBroadcastReceiver != null && BluetoothService.isSupported()) {
            mContext.unregisterReceiver(mBluetoothBroadcastReceiver);
        }
    }


    //Interface for communication between this fragment and the host
    public interface OnDeviceSelectedListener {
        void deviceSelected(BluetoothDevice device);
        void serviceValidation(String message);
    }


}
