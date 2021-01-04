package fhtw.usb_hid;

/**
 * Event handler for new device connection
 *
 * @author Paul Volavsek
 */
public abstract interface DeviceConnectionListener {
	/**
	 * Event handler Method for new device connection
	 */
	public abstract void onDeviceConnection();
}
