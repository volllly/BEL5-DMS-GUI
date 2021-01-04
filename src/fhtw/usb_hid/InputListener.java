package fhtw.usb_hid;

/**
 * Event handler for received messages
 *
 * @author Paul Volavsek
 */

public abstract interface InputListener {
	/**
	 * Method for received messages
	 * @param data The received message
	 */
	public abstract void onInput(String data);
}
