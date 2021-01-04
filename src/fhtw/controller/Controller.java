/**
 *
 */
package fhtw.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fhtw.usb_hid.DeviceConnectionListener;
import fhtw.usb_hid.ErrorListener;
import fhtw.usb_hid.InputListener;
import fhtw.usb_hid.HID_Device;
import fhtw.util.BasicKnob;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.InputReportListener;
import javafx.scene.control.Alert;

/**
 * @author Paul Volavsek
 *
 */
public class Controller implements Initializable {
	private int frequency = 1;
	private int phase = 0;
	private int wave = 0;

	private boolean connected = false;

	private HID_Device hid_device;

	@FXML
	private BasicKnob knob_fq;

	@FXML
	private BasicKnob knob_ph;

	@FXML
	private BasicKnob knob_wave;

	@FXML
	private Label lbl_knob_fq_value;

	@FXML
	private Label lbl_knob_ph_value;

	@FXML
	private Label lbl_knob_wave_value;

	@FXML
	private Label lbl_statusbar;

	@FXML
	private void handleAboutAction(final ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("BEL5 DMS SignalGenerator");
		alert.setContentText("2020; GNEIST Nico, KIENAST Patrik, VOLAVSEK Paul");
		alert.showAndWait();

	}

	@FXML
	private void handleCloseAction(final ActionEvent event) {
		System.exit(1);
	}

	@FXML
	private void handleStartAction(final ActionEvent event) {
		hid_device.transmitPacket("_n_");
	}

	@FXML
	private void handleStopAction(final ActionEvent event) {
		hid_device.transmitPacket("_f_");
	}

	@FXML
	private void handleReadAction(final ActionEvent event) {
		hid_device.transmitPacket("_r_");
	}

	private void handleSendValuesAction() {
		hid_device.transmitPacket(String.format("_%d_%d_%d_", wave, frequency, phase));
	}

	private void setStatusbar(String message) {
		lbl_statusbar.setText((connected ? "Connected" : "Not Connected") + (message != "" ? " | " + message : ""));
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		knob_fq.setValue(1.0);
		knob_fq.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 2) {
						knob_fq.setValue(1.0);
					}
				}
			}
		});

		knob_fq.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 1) {
						frequency = (int)knob_fq.getValue();
						handleSendValuesAction();
					}
				}
			}
		});

		knob_fq.valueProperty().addListener((obs, oldValue, newValue) -> {
			lbl_knob_fq_value.setText(String.format("%d", newValue.intValue()));
		});

		knob_ph.setValue(0);
		knob_ph.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 2) {
						knob_ph.setValue(0);
					}
				}
			}
		});

		knob_ph.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 1) {
						phase = (int)knob_ph.getValue();
						handleSendValuesAction();
					}
				}
			}
		});

		knob_ph.valueProperty().addListener((obs, oldValue, newValue) -> {
			lbl_knob_ph_value.setText(String.format("%d", newValue.intValue()));
		});

		knob_wave.setValue(0.0);
		knob_wave.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 2) {
						knob_wave.setValue(0);

						wave = Math.min(2, (int)knob_wave.getValue());
						handleSendValuesAction();
					}
				}
			}
		});

		knob_wave.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 1) {
						wave = Math.min(2, (int)knob_wave.getValue());
						handleSendValuesAction();
					}
				}
			}
		});

		knob_wave.valueProperty().addListener((obs, oldValue, newValue) -> {
			String text = "";
			switch (newValue.intValue()) {
			case 0:
				text = "sine";
				break;
			case 1:
				text = "triangle";
				break;
			case 2:
			case 3:
				text = "square";
				break;
			default:
				text = lbl_knob_wave_value.getText();
				break;
			}
			lbl_knob_wave_value.setText(text);
		});

		hid_device = new HID_Device();

		hid_device.registerErrorListener(new ErrorListener() {
			@Override
			public void onError(Throwable error) {
				setStatusbar(error.getMessage());
			}
		});

		hid_device.registerDeviceConnectionListener(new DeviceConnectionListener() {
			@Override
			public void onDeviceConnection(HidDevice source) {
				connected = true;
				setStatusbar("");
				handleReadAction(null);
			}
		});

		hid_device.registerDeviceRemovalListener(new DeviceRemovalListener() {
			@Override
			public void onDeviceRemoval(HidDevice source) {
				connected = false;
				setStatusbar("");
			}
		});

		hid_device.registerInputListener(new InputListener() {
			@Override
			public void onInput(HidDevice source, String data) {
				if(data == "ACK") {
					return;
				}

				if(data == "NACK") {
					setStatusbar("Received NACK");
					return;
				}

				if(data.charAt(0) == '_') {
					Matcher matches = Pattern.compile("^_([0-9]{1})_([0-9]{1,6})_([0-9]{1,3})_$").matcher(data);

					if(!matches.find()) {
						setStatusbar("Clould not parse reply");
						return;
					}

					wave = Integer.parseInt(matches.group(0));
					frequency = Integer.parseInt(matches.group(1));
					phase = Integer.parseInt(matches.group(2));

					knob_wave.setValue(wave);
					knob_fq.setValue(frequency);
					knob_ph.setValue(phase);
				}
			}
		});

		hid_device.start_HID_Device();
	}
}

/* EOF */