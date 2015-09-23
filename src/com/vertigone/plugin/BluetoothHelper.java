package com.vertigone.plugin;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BluetoothHelper {
	public boolean mScanning = false;
    public Handler mHandler;
    public static final int RSSI_UPDATE_TIME_INTERVAL = 1500; // 1.5 seconds
    public String mDeviceAddress;	
	public ArrayList<BluetoothDevice> mDevices;
    // Stops scanning after 10 seconds.
    public static final long SCAN_PERIOD = 10000;
    
    public BluetoothAdapter mBluetoothAdapter = null;
	public BluetoothManager mBluetoothManager = null;
    public BluetoothDevice  mBluetoothDevice = null;
    public BluetoothGatt    mBluetoothGatt = null;
    public BluetoothGattService mBluetoothSelectedService = null;
    public List<BluetoothGattService> mBluetoothGattServices = null;	
    
    public Handler mTimerHandler = new Handler();
    public boolean mTimerEnabled = false;
    public boolean mConnected = false;
    
	public Activity mParent = null;  
	/* creates BleWrapper object, set its parent activity and callback object */
    public BluetoothHelper(Activity parent) {
    	this.mParent = parent;
    }
    
	
	public boolean initialize() {
		debug("Initialized");
		mDevices  = new ArrayList<BluetoothDevice>();
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        if(mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;    	
    }
	
	/* defines callback for scanning results */
    private BluetoothAdapter.LeScanCallback mDeviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        	handleFoundDevice(device, rssi, scanRecord);
        }
    };
	
    public void handleFoundDevice(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
    	debug("Device Found");
    	if(mDevices.contains(device) == false) {
			mDevices.add(device);
	    	debug(device.getName());
	    	if (device.getName().equals("Adafruit Bluefruit LE")) {
	    		debug("SYMBALANCE DEVICE DISCOVERED!");
	    		debug("Connecting to SYMBALANCE");
	    		connect(device.getAddress());
	    		debug("Finished Connecting");
	    	}
		}
    }
    
    /* disconnect the device. It is still possible to reconnect to it later with this Gatt client */
    public void diconnect() {
    	if(mBluetoothGatt != null) mBluetoothGatt.disconnect();
    	 //mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice);
    }
    
    /* close GATT client completely */
    public void close() {
    	if(mBluetoothGatt != null) mBluetoothGatt.close();
    	mBluetoothGatt = null;
    }   
    
    /* request new RSSi value for the connection*/
    public void readPeriodicalyRssiValue(final boolean repeat) {
    	mTimerEnabled = repeat;
    	// check if we should stop checking RSSI value
    	if(mConnected == false || mBluetoothGatt == null || mTimerEnabled == false) {
    		mTimerEnabled = false;
    		return;
    	}
    	
    	mTimerHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mBluetoothGatt == null ||
				   mBluetoothAdapter == null ||
				   mConnected == false)
				{
					mTimerEnabled = false;
					return;
				}
				
				// request RSSI value
				mBluetoothGatt.readRemoteRssi(); 
				// add call it once more in the future
				readPeriodicalyRssiValue(mTimerEnabled);
			}
    	}, RSSI_UPDATE_TIME_INTERVAL);
    }    
    
    /* starts monitoring RSSI value */
    public void startMonitoringRssiValue() {
    	readPeriodicalyRssiValue(true);
    }
    
    /* stops monitoring of RSSI value */
    public void stopMonitoringRssiValue() {
    	readPeriodicalyRssiValue(false);
    }
    /* connect to the device with specified address */
    public boolean connect(final String deviceAddress) {
        if (mBluetoothAdapter == null || deviceAddress == null) return false;
        mDeviceAddress = deviceAddress;
        
        // check if we need to connect from scratch or just reconnect to previous device
        if(mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
        	// just reconnect
        	debug("Reconnecting...");
        	return mBluetoothGatt.connect();
        }
        else {
        	// connect from scratch
            // get BluetoothDevice object for specified address
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
            if (mBluetoothDevice == null) {
                // we got wrong address - that device is not available!
            	debug("Wrong address, device is not available");
                return false;
            }
            // connect with remote device
            debug("Connecting with remote device");
            debug(mBluetoothDevice.getAddress());
         // adding to the UI have to happen in UI thread
    		mParent.runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, false, mBleCallback);
    			}
    		});
        }
        return true;
    }

    public void getFeed() {
    	mParent.runOnUiThread(new Runnable() {
    		public void run() {
            	BluetoothGattService service = mBluetoothGatt.getService(BleDefinedUUIDs.Service.SYMBALANCE);
            	debug(service.getUuid().toString());
            	BluetoothGattCharacteristic ch = service.getCharacteristic(BleDefinedUUIDs.Characteristic.SYMBALANCE_READ);
            	debug(ch.getUuid().toString());
            	Log.d("BleStatus", "RequestingCharacteristicValue");
                mBluetoothGatt.readCharacteristic(ch);
    		}
    	});
    }

	public void stopScanning() {
    	mScanning = false;
		debug("Scan stopped");
        mBluetoothAdapter.stopLeScan(mDeviceFoundCallback);
	}
    
	public void startScanning() {
		mScanning = true;
		debug("Scan started");
        mBluetoothAdapter.startLeScan(mDeviceFoundCallback);
	}
    
	public boolean isBtEnabled() {
		final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
		if(manager == null) return false;
		
		final BluetoothAdapter adapter = manager.getAdapter();
		if(adapter == null) return false;
		
		
		
		return adapter.isEnabled();
	}
	
	public void debug(String message) {
		Log.d("Plugin", message);
	}
	
	/* request to discover all services available on the remote devices
     * results are delivered through callback object aka the mBleCallback via on services discovered */
    public void startServicesDiscovery() {
    	debug("Starting service discovery");
    	if(mBluetoothGatt != null) mBluetoothGatt.discoverServices();
    }
    
    

    // Called from onServicesDiscovered Callback (mBleCallback)
    public void getSupportedServices() {
    	if(mBluetoothGattServices != null && mBluetoothGattServices.size() > 0) mBluetoothGattServices.clear();
    	// keep reference to all services in local array:
        if(mBluetoothGatt != null) {
        	debug("Saving services");
        	mBluetoothGattServices = mBluetoothGatt.getServices();
        	debug(Integer.toString(mBluetoothGattServices.size()));
        }
    }
    
    
    
    public void getCharacteristicsForService(final BluetoothGattService service) {
    	if(service == null) return;
    	List<BluetoothGattCharacteristic> chars = null;
    	
    	chars = service.getCharacteristics();
    	// keep reference to the last selected service
    	mBluetoothSelectedService = service;
    }
    
    /* get characteristic's value (and parse it for some types of characteristics) 
     * before calling this You should always update the value by calling requestCharacteristicValue() */
    public void getCharacteristicValue(BluetoothGattCharacteristic ch) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null) return;
        debug("Getting Value");
        byte[] rawValue = ch.getValue();
        String strValue = null;
        int intValue = 0;
        for (byte b : rawValue) {
        	debug(Byte.toString(b));
        }
    }
	
    /* callbacks called for any action on particular Ble Device */
    private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
            	debug("Connected");
            	mConnected = true;
            	//mUiCallback.uiDeviceConnected(mBluetoothGatt, mBluetoothDevice);
            	
            	// now we can start talking with the device, e.g.
            	mBluetoothGatt.readRemoteRssi();
            	// response will be delivered to callback object!
            	
            	// in our case we would also like automatically to call for services discovery
            	startServicesDiscovery();
            	
            	// and we also want to get RSSI value to be updated periodically
            	startMonitoringRssiValue();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            	debug("Disconnected");
            	mConnected = false;
            	//mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	// now, when services discovery is finished, we can call getServices() for Gatt
            	getSupportedServices();
            	getFeed();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
        	debug("Charactertistic Read");
        	// we got response regarding our request to fetch characteristic value
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	// and it success, so we can get the value
            	debug("SUCCESS");
            	getCharacteristicValue(characteristic);
            } else {
            	debug("Failed");
            }
        }
//
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
        	// characteristic's value was updated due to enabled notification, lets get this value
        	// the value itself will be reported to the UI inside getCharacteristicValue
        	getCharacteristicValue(characteristic);
        	debug("Characteristic Changed");
        	// also, notify UI that notification are enabled for particular characteristic
        	//mUiCallback.uiGotNotification(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic);
        }
        
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//        	String deviceName = gatt.getDevice().getName();
//        	String serviceName = BleNamesResolver.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
//        	String charName = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
//        	String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + charName;
//        	
//        	// we got response regarding our request to write new value to the characteristic
//        	// let see if it failed or not
//        	if(status == BluetoothGatt.GATT_SUCCESS) {
//        		 mUiCallback.uiSuccessfulWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description);
//        	}
//        	else {
//        		 mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice, mBluetoothSelectedService, characteristic, description + " STATUS = " + status);
//        	}
//        };
//        
//        @Override
//        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//        	if(status == BluetoothGatt.GATT_SUCCESS) {
//        		// we got new value of RSSI of the connection, pass it to the UI
//        		 mUiCallback.uiNewRssiAvailable(mBluetoothGatt, mBluetoothDevice, rssi);
//        	}
        };
    };
}
