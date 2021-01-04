package fhtw.usb_hid;

/**
 * Event handler for device connection loss
 *
 * @author Paul Volavsek
 */
public abstract interface DeviceRemovalListener {
	/**
	 * Event handler Method for device connection loss
	 */
	public abstract void onDeviceRemoval();
}
