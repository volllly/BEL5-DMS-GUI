/**
 *
 */
package fhtw.usb_hid;

import java.util.ArrayList;
import java.util.List;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

/**
 * @author Markus Lechner
 *
 */
public class HID_Device implements Runnable {
	private final short HID_PRODUCT_ID = 0x0081;
	private final String HID_SERIAL_NR = ("OpenLab-Multimeter");

	private boolean hid_dev_opened = false;
	private HidDevice hid_device = null;
	private HidDeviceInfo hidDevInfo = null;
	private Thread th_HID_device = null;

	private byte id = 0;
	private List<Byte> queue = new ArrayList<Byte>();

	private DeviceRemovalListener deviceRemovalListener;
	private DeviceConnectionListener deviceConnectionListener;
	private InputReportListener inputReportListener;
	private ErrorListener errorListener;

	/**
	 *
	 */
	public HID_Device() {
		th_HID_device = new Thread(this);
		th_HID_device.setName("HID_Device");
		th_HID_device.setDaemon(true);
	}

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

								queue.clear();
								deviceRemovalListener.onDeviceRemoval(source);
							}
						});

						hid_device.setInputReportListener(new InputReportListener() {
							@Override
							public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
								if(!queue.contains(Id)) {
									errorListener.onError(new Throwable("reply id not found."));
									return;
								}
								queue.remove(queue.indexOf(Id));

								inputReportListener.onInputReport(source, Id, data, len);
							}
						});

						hid_dev_opened = true;
						deviceConnectionListener.onDeviceConnection(hid_device);
					}

				} else {
					Thread.sleep(10);
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				queue.clear();
				errorListener.onError(e);
			}
		}
	}

	public void registerErrorListener(ErrorListener listener) {
		errorListener = listener;
	}

	public void registerDeviceConnectionListener(DeviceConnectionListener listener) {
		deviceConnectionListener = listener;
	}

	public void registerDeviceRemovalListener(DeviceRemovalListener listener) {
		deviceRemovalListener = listener;
	}

	public void registerInputReportListener(InputReportListener listener) {
		inputReportListener = listener;
	}

	public boolean transmitPacket(byte[] payload) {
		if (hid_device == null) {
			errorListener.onError(new Throwable("No Device Connected"));
			return false;
		}

		if(queue.size() >= 255) {
			errorListener.onError(new Throwable("To many commands sent."));
			return false;
		}

		hid_device.setOutputReport(id, payload, payload.length);
		queue.add(id);
		id++;
		return true;
	}
}

/* EOF */
