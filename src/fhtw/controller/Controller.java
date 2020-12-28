/**
 *
 */
package fhtw.controller;

import java.net.URL;
import java.util.ResourceBundle;

import fhtw.util.BasicKnob;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;

/**
 * @author Markus Lechner
 *
 */
public class Controller implements Initializable {
	private int frequency = 1;
	private int phase = 0;
	private int wave = 0;

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

	}

	@FXML
	private void handleStopAction(final ActionEvent event) {

	}

	@FXML
	private void handleReadAction(final ActionEvent event) {

	}

	private void handleSendValuesAction() {
		System.out.println(String.format("[2; 5; 7; %d; %d;;;; %d;;]", wave, frequency, phase));
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
			}
			lbl_knob_wave_value.setText(text);
		});
	}
}

/* EOF */