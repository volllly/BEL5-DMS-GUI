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
import fhtw.usb_hid.DeviceRemovalListener;
import javafx.scene.control.Alert;

/**
 * This is the Controller class for the user interface.
 *
 * The whole interface is constructed in the <a href="{@docRoot}/KnobGUI/fxml/Main.fxml">FXML</a> file.
 *
 * @author Paul Volavsek
 */
public class Controller implements Initializable {
	/**
	 * The current frequency.
	 */
	private int frequency = 1;
	/**
	 * The current phase.
	 */
	private int phase = 0;
	/**
	 * The current waveform.
	 * 	1. sine
	 *  2. triangle
	 *  3. square
	 */
	private int wave = 0;

	/**
	 * If connection is established.
	 */
	private boolean connected = false;

	/**
	 * Instance of the HID_Device.
	 */
	private HID_Device hid_device;

	/**
	 * The frequency knob.
	 */
	@FXML
	private BasicKnob knob_fq;


	/**
	 * The phase knob.
	 */
	@FXML
	private BasicKnob knob_ph;


	/**
	 * The waveform knob.
	 */
	@FXML
	private BasicKnob knob_wave;


	/**
	 * The frequency label.
	 */
	@FXML
	private Label lbl_knob_fq_value;


	/**
	 * The phase label.
	 */
	@FXML
	private Label lbl_knob_ph_value;


	/**
	 * The waveform label.
	 */
	@FXML
	private Label lbl_knob_wave_value;


	/**
	 * The statusbar label.
	 */
	@FXML
	private Label lbl_statusbar;

	/**
	 * Handles the Help-About menu action.
	 *
	 * Shows popup window.
	 *
	 * @param event `AvtionEvent` comming from FXML
	 */
	@FXML
	private void handleAboutAction(final ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("BEL5 DMS SignalGenerator");
		alert.setContentText("2020; GNEIST Nico, KIENAST Patrik, VOLAVSEK Paul");
		alert.showAndWait();

	}

	/**
	 * Handles the File-Close menu action.
	 *
	 * Exits the application.
	 *
	 * @param event `AvtionEvent` comming from FXML
	 */
	@FXML
	private void handleCloseAction(final ActionEvent event) {
		System.exit(1);
	}

	/**
	 * Handles the Generator-Start menu action.
	 *
	 * Read values and resent them to start the SignalGenerator.
	 *
	 * @param event `AvtionEvent` coming from FXML
	 */
	@FXML
	private void handleStartAction(final ActionEvent event) {
		hid_device.transmitPacket("_n_");
	}

	/**
	 * Handles the Generator-Stop menu action.
	 *
	 * Sends the stop command to the SignalGenerator.
	 *
	 * @param event `AvtionEvent` coming from FXML
	 */
	@FXML
	private void handleStopAction(final ActionEvent event) {
		hid_device.transmitPacket("_f_");
	}

	/**
	 * Handles the Generator-Read menu action.
	 *
	 * Sends the read command to the SignalGenerator.
	 *
	 * @param event `AvtionEvent` coming from FXML
	 */
	@FXML
	private void handleReadAction(final ActionEvent event) {
		hid_device.transmitPacket("_r_");
	}

	/**
	 * Handler for value updates.
	 *
	 * Sends the current values to the SignalGenerator.
	 * This method needs to be called when the values should be updated on the SignalGenerator.
	 */
	private void handleSendValuesAction() {
		hid_device.transmitPacket(String.format("_%d_%d_%d_", wave + 1, frequency, phase));
	}

	/**
	 * Shows a message on the statusbar.
	 *
	 * @param message The message to show on the statusbar
	 *
	 */
	private void setStatusbar(String message) {
		lbl_statusbar.setText((connected ? "Connected" : "Not Connected") + (message != "" ? " | " + message : ""));
	}

	/**
	 * Initializes the ui and ui controller.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		knob_fq.setValue(1.0); //initial fq knob value
		knob_fq.setOnMouseClicked(new EventHandler<MouseEvent>() { // reset fq value on doubleclick
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 2) {
						knob_fq.setValue(1.0);
						handleSendValuesAction();
					}
				}
			}
		});

		knob_fq.setOnMouseReleased(new EventHandler<MouseEvent>() { // sendvalues on mouserelease
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

		knob_fq.valueProperty().addListener((obs, oldValue, newValue) -> { // update fq label on knob value change
			lbl_knob_fq_value.setText(String.format("%d", newValue.intValue()));
		});

		knob_ph.setValue(0); //initial ph knob value
		knob_ph.setOnMouseClicked(new EventHandler<MouseEvent>() { // reset ph value on doubleclick
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (mouseEvent.getClickCount() == 2) {
						knob_ph.setValue(0);
						handleSendValuesAction();
					}
				}
			}
		});

		knob_ph.setOnMouseReleased(new EventHandler<MouseEvent>() { // sendvalues on mouserelease
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

		knob_ph.valueProperty().addListener((obs, oldValue, newValue) -> { // update ph label on knob value change
			lbl_knob_ph_value.setText(String.format("%d", newValue.intValue()));
		});

		knob_wave.setValue(0.0);  //initial wave knob value
		knob_wave.setOnMouseClicked(new EventHandler<MouseEvent>() { // reset wave value on doubleclick
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

		knob_wave.setOnMouseReleased(new EventHandler<MouseEvent>() { // sendvalues on mouserelease
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

		knob_wave.valueProperty().addListener((obs, oldValue, newValue) -> { // update wave label on knob value change
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

		hid_device.registerErrorListener(new ErrorListener() { // show error message on statusbar
			@Override
			public void onError(Throwable error) {
				setStatusbar(error.getMessage());
			}
		});

		hid_device.registerDeviceConnectionListener(new DeviceConnectionListener() { // register established connection and read values
			@Override
			public void onDeviceConnection() {
				connected = true;
				setStatusbar("");
				handleReadAction(null);
			}
		});

		hid_device.registerDeviceRemovalListener(new DeviceRemovalListener() { // register removed connection
			@Override
			public void onDeviceRemoval() {
				connected = false;
				setStatusbar("");
			}
		});

		hid_device.registerInputListener(new InputListener() {
			@Override
			public void onInput(String data) {
				if(data == "ACK") {
					return;
				}

				if(data == "NACK") {
					setStatusbar("Received NACK");
					return;
				}

				// parse read reply
				if(data.charAt(0) == '_') {
					Matcher matches = Pattern.compile("^_([0-9]{1})_([0-9]{1,6})_([0-9]{1,3})_$").matcher(data);

					if(!matches.find()) {
						setStatusbar("Clould not parse reply");
						return;
					}

					wave = Integer.parseInt(matches.group(0)) - 1;
					frequency = Integer.parseInt(matches.group(1));
					phase = Integer.parseInt(matches.group(2));

					knob_wave.setValue(wave);
					knob_fq.setValue(frequency);
					knob_ph.setValue(phase);

					handleSendValuesAction();
					return;
				}
			}
		});

		hid_device.start_HID_Device();
	}
}

/* EOF */