package fhtw.usb_hid;

public abstract interface ErrorListener {
	public abstract void onError(Throwable error);
}
