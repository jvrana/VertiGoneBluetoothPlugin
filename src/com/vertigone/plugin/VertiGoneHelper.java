package com.vertigone.plugin;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

/* this activity's purpose is to show how to use particular type of devices in easy and fast way */
public class VertiGoneHelper extends BluetoothHelper {
	
	public VertiGoneHelper(Activity parent) {
		super(parent);
		debug("VertiGoneHelper Created");
		// TODO Auto-generated constructor stub
	}
	final static String DEVICENAME = "SYMBIO";
	final static private UUID mVertiGoneServiceUuid = BleDefinedUUIDs.Service.SYMBALANCE;
	final static private UUID mVertiGoneCharacteristicUuid = BleDefinedUUIDs.Characteristic.SYMBALANCE_READ;
	private BluetoothGattCharacteristic mBTValueCharacteristic = null;
	private EditText mConsole = null;
	private TextView mTextView  = null;
	private BluetoothGattService mBluetoothService;
	public boolean scanning = false;
	public boolean device_found = false;
	public boolean device_connected = false;
	private StringBuffer inBuffer = new StringBuffer(40);
	public boolean initialize() {
		Log.d("UnityPlugin", "Helper Initializing");
		Status.updateBLEStatus(Status.BleStatus.Start);
		mDevices  = new ArrayList<BluetoothDevice>();
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
            	Log.d("UnityPlugin", "Error: BluetoothManager not found");
                return false;
            }
        }

        if(mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
        	Log.d("UnityPlugin", "Error: BluetoothAdapter not found");
            return false;
        }
        Status.updateBLEStatus(Status.BleStatus.Initialized);	
        Log.d("UnityPlugin", "Helper Initialized");
        return true;    	
    }

	public void startSearchingForVertiGone() {
		Log.d("UnityPlugin", "Starting Search for BLE Device");
		scanning = true;
		// we define what kind of services found device needs to provide. In our case we are interested only in
		// VertiGone service
		final UUID[] uuids = new UUID[] { mVertiGoneServiceUuid };
		debug(uuids[0].toString());
		mBluetoothAdapter.startLeScan(mDeviceFoundCallback);
		// results will be returned by callback
		Status.updateBLEStatus(Status.BleStatus.Scanning);	
	}

	public void stopSearchingForVertiGone() {
		mBluetoothAdapter.stopLeScan(mDeviceFoundCallback);
		scanning = false;
		Status.updateBLEStatus(Status.BleStatus.ScanningStopped);
	}
	
	private void connectToDevice() {
		Status.updateBLEStatus(Status.BleStatus.Connecting);
		mParent.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, true, mGattCallback);
			}
		});
	}
	
	private void disconnectFromDevice() {
		Status.updateBLEStatus(Status.BleStatus.Disconnected);
		if(mBluetoothGatt != null) mBluetoothGatt.disconnect();
	}
	
	private void closeGatt() {
		if(mBluetoothGatt != null) mBluetoothGatt.close();
		mBluetoothGatt = null;
	}
	
	private void discoverServices() {
		debug("Starting discovering services");
		mBluetoothGatt.discoverServices();
	}
	
	private void getVertiGoneService() {
		Status.updateBLEStatus("Getting Symbalance Service");
		Status.updateBLEStatus("Getting list of available services:");
		List<BluetoothGattService> services = mBluetoothGatt.getServices();
		for (BluetoothGattService service : services) {
			Status.updateBLEStatus(service.toString());
			Status.updateBLEStatus(service.getUuid().toString());
		}
		mBluetoothService = mBluetoothGatt.getService(mVertiGoneServiceUuid);
		if(mBluetoothService == null) {
			Status.updateBLEStatus(Status.BleStatus.ServiceFailed);
		}
		else {
			Status.updateBLEStatus(Status.BleStatus.ServiceRetreived);
			getVertiGoneCharacteristic();
		}
	}
	/// Somewhere around here is the problem!!!
	private void getVertiGoneCharacteristic() {
		Status.updateBLEStatus("Getting list of characteristics");
		List<BluetoothGattCharacteristic> characteristics= mBluetoothService.getCharacteristics();
		for (BluetoothGattCharacteristic ch : characteristics) {
			Status.updateBLEStatus(ch.toString());
			Status.updateBLEStatus(ch.getUuid().toString());
		}
		Status.updateBLEStatus("Getting Symbalance Characteristic");
		mBTValueCharacteristic = mBluetoothService.getCharacteristic(mVertiGoneCharacteristicUuid);
		if(mBTValueCharacteristic == null) {
			Status.updateBLEStatus(Status.BleStatus.ValuesFailed);
		}
		else {
			Status.updateBLEStatus(Status.BleStatus.ValuesRetreived);
			Status.updateBLEStatus("Characteristic found, enabling notifications");
			enableNotificationForVertiGone();
		}
	}
	
	private void enableNotificationForVertiGone() {
		Status.updateBLEStatus(Status.BleStatus.EnablingNotification);
        boolean success = mBluetoothGatt.setCharacteristicNotification(mBTValueCharacteristic, true);
        if(!success) {
        	Status.updateBLEStatus(Status.BleStatus.EnablingNotificationFailed);
        	return;
        }

        BluetoothGattDescriptor descriptor = mBTValueCharacteristic.getDescriptor(BleDefinedUUIDs.Descriptor.CHAR_CLIENT_CONFIG);
        if(descriptor != null) {
	        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);
	        Status.updateBLEStatus(Status.BleStatus.Activated);
        }		
        else {
        	Status.updateBLEStatus(Status.BleStatus.Deactivated);
        }
	}

	private void disableNotificationForVertiGone() {
		debug("Disabling notification for VertiGone");
        boolean success = mBluetoothGatt.setCharacteristicNotification(mBTValueCharacteristic, false);
        if(!success) {
        	Status.updateBLEStatus(Status.BleStatus.EnablingNotificationFailed);
        	return;
        }

        BluetoothGattDescriptor descriptor = mBTValueCharacteristic.getDescriptor(BleDefinedUUIDs.Descriptor.CHAR_CLIENT_CONFIG);
        if(descriptor != null) {
	        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
	        mBluetoothGatt.writeDescriptor(descriptor);
	        Status.updateBLEStatus(Status.BleStatus.Deactivated);
        }		
        else {
        	debug("Could not get descriptor for characteristic! Notification could be still enabled.");
        }
	}
	
	private void updateValues(String s) {
		//inBuffer.append(s);
		try {
			String[] tokens = s.split(",");
			SensorValues.x = Float.parseFloat(tokens[0]);
			SensorValues.y = Float.parseFloat(tokens[1]);
			SensorValues.z = Float.parseFloat(tokens[2]);
			/*
			String[] tokens = inBuffer.toString().split(":");
			if (tokens.length >= 3) {
				String values = tokens[tokens.length-2];
				Status.updateBLEStatus(values);
				Status.updateBLEStatus("OK");
				inBuffer = new StringBuffer(tokens[tokens.length - 1]);
				String[] quat = values.split(",");
				SensorValues.x = Float.parseFloat(quat[0]);
				SensorValues.y = Float.parseFloat(quat[1]);
				SensorValues.z = Float.parseFloat(quat[2]);
			} */
		} catch(Exception e) {
			Status.updateBLEStatus(s);
		}
	}
	
	private String getAndDisplayVertiGoneValue() {
    	final String values = mBTValueCharacteristic.getStringValue(0);
    	mParent.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				debug(values);
			}
    	});
    	updateValues(values);
    	return values;
	}
	
    private BluetoothAdapter.LeScanCallback mDeviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        	// here we found some device with VertiGone service, lets save it:
        	if (device.getName().equals(DEVICENAME)) {
        		VertiGoneHelper.this.mBluetoothDevice = device;
        		Status.updateBLEStatus(Status.BleStatus.DeviceFound);
            	stopSearchingForVertiGone();
            	connectToDevice();
        	}
        }
    };	
	
    /* callbacks called for any action on VertiGone Device */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
            	Status.updateBLEStatus(Status.BleStatus.Connected);
            	discoverServices();
            	device_connected = true;
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            	Status.updateBLEStatus(Status.BleStatus.Disconnected);
            	device_connected = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        	if(status == BluetoothGatt.GATT_SUCCESS) {
        		Status.updateBLEStatus("Service Discovered");
        		getVertiGoneService();
        	}
        	else {
        		debug("Unable to discover services");
        	}
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
        	if(characteristic.equals(mBTValueCharacteristic)) {
        		getAndDisplayVertiGoneValue();
        	}
        }       
        
        /* the rest of callbacks are not interested for us */
        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {}


        
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {};
        
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {};
    };
}
