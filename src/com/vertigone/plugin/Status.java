package com.vertigone.plugin;


import java.util.UUID;

import android.util.Log;

public class Status {
	
	public static String CurrentBluetoothStatus = "Welcome to VertiGone";
	public static String CurrentVertiGoneStatus = "Waiting for bluetooth device connection";
	
	public class BleStatus {
		final static public String Start   = "Welcome to Symbalance";
		final static public String Initialized   = "Initialized";
		final static public String Scanning   = "Scanning for BLE Symbalance Devices";
		final static public String ScanningStopped   = "Scanning Stopped";
		final static public String DeviceFound   = "VertiGone Device Found";
		final static public String Connecting   = "Connecting...";
		final static public String Disconnected   = "VertiGone Device Disconnected";
		final static public String Reconnecting = "VertiGone Device Attempting Reconnection";
		final static public String Connected   = "VertiGone Device Connected";
		final static public String ServiceDiscovered   = "VertiGone Services Discovered";
		final static public String RetreivingService   = "Getting VertiGone Service";
		final static public String ServiceRetreived   = "VertiGoneService Retreived";
		final static public String ServiceFailed   = "VertiGoneService Retreival Failed";
		final static public String RetreivingValues = "Accessing VertiGone Position and Orientation Data";
		final static public String ValuesRetreived = "VertiGone Position and Orientation Data Retreived";
		final static public String ValuesFailed = "VertiGone Position and Orientation Data Retreival Failure";
		final static public String EnablingNotification = "Enabling live feed";
		final static public String EnablingNotificationFailed = "Failed to enable live feed";
		final static public String Deactivated = "Live feed deactivated";
		final static public String Activated = "Live feed activated! Welcome to VertiGone!";
		final static public String DataRetreival = "Data Being Received";
		final static public String DataError = "Data format not recognized";
	}
	
	public class VertiGoneStatus {
		final static public String Waiting = "Waiting for VertiGone Bluetooth Device Connection";
		final static public String Detecting = "Performing Fall Detection Algorithms";
		final static public String FallAnticipated = "Fall Anticipated! Activating 3D Audio Simulation";
		final static public String MajorFallDetected = "Major Fall Detected! Calling Emergency";
		final static public String MinorFallDetected = "Minor Fall Detected. Click disable to cancel emergency.";
		final static public String Returning = "Fall avoided, returning to detection mode.";
	}

	public static void updateBLEStatus(String update) {
		CurrentBluetoothStatus = update;
		Log.d("BleStatus", update);
	}
	
	public static void updateVertiGoneStatus(String update) {
		CurrentVertiGoneStatus = update;
		Log.d("BleStatus", update);
	}
	
	public static void printBLEStatus() {
		Log.d("Status", CurrentBluetoothStatus);
	}
}
	