package fhtw.usb_hid;

import purejavahidapi.HidDevice;

public abstract interface DeviceConnectionListener {
	public abstract void onDeviceConnection(HidDevice source);
}
