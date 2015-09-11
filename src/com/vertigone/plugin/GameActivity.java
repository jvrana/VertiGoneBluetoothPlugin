package com.vertigone.plugin;


import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class GameActivity extends UnityPlayerActivity {
	private static final int ENABLE_BT_REQUEST_ID = 1;
	private Activity _activity;
	private VertiGoneHelper vh = null;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		debug("onCreate");
		_activity = UnityPlayer.currentActivity;
		//boolean enabled = isBtEnabled();
		debug("Symbalance Initialized");
		vh = new VertiGoneHelper(this);
		if(vh.isBtEnabled() == false) {
			// BT is not turned on - ask user to make it enabled
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
		    // see onActivityResult to check what is the status of our request
		}
		debug("Initializing");
		vh.initialize();
		debug("Starting Search");
		vh.startSearchingForVertiGone();
	}
	
	public void onResume() {
		super.onResume();
		debug("Resume");
	}
	
	public void onPause() {
		super.onPause();
		debug("onPause");
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
	}
	
	public void debug(String message) {
		Log.d("UnityPlugin", message);
	}
}
