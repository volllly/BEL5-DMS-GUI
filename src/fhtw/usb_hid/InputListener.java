package fhtw.usb_hid;

import purejavahidapi.HidDevice;

public abstract interface InputListener {
	public abstract void onInput(HidDevice source, String data);
}
