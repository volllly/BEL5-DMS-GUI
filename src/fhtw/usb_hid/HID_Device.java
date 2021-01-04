package fhtw.usb_hid;

import java.util.ArrayList;
import java.util.List;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

/**
 * Manages communication with the SignalGenerator.
 *
 * Based on the provided implementation in `USB HID System Software` from the BEL-VZ-5-WS2020-DMS moodle course.
 *
 * @author Paul Volavsek
 */
public class HID_Device implements Runnable {
	private final short HID_PRODUCT_ID = 0x0081;
	private final String HID_SERIAL_NR = ("OpenLab-Multimeter");

	private boolean hid_dev_opened = false;
	private HidDevice hid_device = null;
	private HidDeviceInfo hidDevInfo = null;
	private Thread th_HID_device = null;

	/**
	 * Instance of the registered `DeviceRemovalListener` event handler
	 */
	private fhtw.usb_hid.DeviceRemovalListener deviceRemovalListener;

	/**
	 * Instance of the registered `DeviceConnectionListener` event handler
	 */
	private DeviceConnectionListener deviceConnectionListener;

	/**
	 * Instance of the registered `InputListener` event handler
	 */
	private InputListener inputListener;

	/**
	 * Instance of the registered `ErrorListener` event handler
	 */
	private ErrorListener errorListener;

	public HID_Device() {
		th_HID_device = new Thread(this);
		th_HID_device.setName("HID_Device");
		th_HID_device.setDaemon(true);
	}

	/**
	 * Starts the `HID_Device`.
	 *
	 * Needs to be called to operate.
	 */
	public void start_HID_Device() {
		th_HID_device.start();
	}

	@Override
	public void run() {
		List<HidDeviceInfo> hidDevList = null;

		while (true) {
			try {
				if (!hid_dev_opened && (hid_device == null)) {
					hidDevList = PureJavaHidApi.enumerateDevices();

					for (HidDeviceInfo hid_info : hidDevList) {
						if (HID_SERIAL_NR.equals(hid_info.getSerialNumberString())
								&& (HID_PRODUCT_ID == hid_info.getProductId())) {
							hidDevInfo = hid_info;
							System.out.println("USB-HID device found! Product ID: "
									+ Integer.toHexString(hid_info.getProductId()));
							break;
						}
					}

					if (hidDevInfo == null) {
						Thread.sleep(100);
					} else {
						hid_device = PureJavaHidApi.openDevice(hidDevInfo);

						hid_device.setDeviceRemovalListener(new DeviceRemovalListener() {
							@Override
							public void onDeviceRemoval(HidDevice source) {
								hid_device = null;
								hidDevInfo = null;
								hid_dev_opened = false;

								if(deviceRemovalListener != null) {
									deviceRemovalListener.onDeviceRemoval(); // call the registered {@link fhtw.usb_hid.DeviceRemovalListener}
								}
							}
						});

						hid_device.setInputReportListener(new InputReportListener() {
							@Override
							public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
								if(inputListener != null) {
									inputListener.onInput(data.toString()); // call the registered {@link fhtw.usb_hid.InputListener}
								}
							}
						});

						hid_dev_opened = true;
						if(deviceConnectionListener != null) {
							deviceConnectionListener.onDeviceConnection(); // call the registered {@link fhtw.usb_hid.DeviceConnectionListener}
						}
					}

				} else {
					Thread.sleep(10);
				}
			} catch (Throwable e) {
				if(errorListener != null) {
					errorListener.onError(e);
				}
			}
		}
	}

	/**
	 * Register an {@link fhtw.usb_hid.ErrorListener}
	 * @param listener The ErrorListener to register
	 */
	public void registerErrorListener(ErrorListener listener) {
		errorListener = listener;
	}

	/**
	 * Register an {@link fhtw.usb_hid.DeviceConnectionListener}
	 * @param listener The DeviceConnectionListener to register
	 */
	public void registerDeviceConnectionListener(DeviceConnectionListener listener) {
		deviceConnectionListener = listener;
	}

	/**
	 * Register an {@link purejavahidapi.DeviceRemovalListener}
	 * @param listener The DeviceRemovalListener to register
	 */
	public void registerDeviceRemovalListener(fhtw.usb_hid.DeviceRemovalListener listener) {
		deviceRemovalListener = listener;
	}

	/**
	 * Register an {@link fhtw.usb_hid.InputListener}
	 * @param listener The InputListener to register
	 */
	public void registerInputListener(InputListener listener) {
		inputListener = listener;
	}

	/**
	 * Transmits a Message to the SignalGenerator
	 * @param payload The command to send
	 * @return Indicates transmission success.
	 */
	public boolean transmitPacket(String payload) {
		if (hid_device == null) {
			errorListener.onError(new Throwable("No Device Connected"));
			return false;
		}

		payload += "\0";

		hid_device.setOutputReport((byte)0, payload.getBytes(), payload.getBytes().length);

		return true;
	}
}

/* EOF */
