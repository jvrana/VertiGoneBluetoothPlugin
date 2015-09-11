package com.vertigone.plugin;


import java.util.UUID;

public class BleDefinedUUIDs {
	
	public static class Service {
		final static public UUID VGONE               = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
		final static public UUID SYMBALANCE          = UUID.fromString("ee0c2080-8786-40ba-ab96-99b91ac981d8");
	};
	
	public static class Characteristic {
		final static public UUID HEART_RATE_MEASUREMENT   = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
		final static public UUID MANUFACTURER_STRING      = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
		final static public UUID MODEL_NUMBER_STRING      = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
		final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
		final static public UUID APPEARANCE               = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
		final static public UUID BODY_SENSOR_LOCATION     = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
		final static public UUID BATTERY_LEVEL            = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
		final static public UUID VGONE_WRITE              = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
		final static public UUID VGONE_READ               = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
	}
	
	public static class Descriptor {
		final static public UUID CHAR_CLIENT_CONFIG       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	}
	
}
