package fhtw.usb_hid;

/**
 * Event handler for errors in the `HID_Device`
 *
 * @author Paul Volavsek
 */
public abstract interface ErrorListener {
	/**
	 * Method for errors
	 * @param error The error which occured
	 */
	public abstract void onError(Throwable error);
}
