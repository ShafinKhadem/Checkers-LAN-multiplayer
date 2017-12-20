package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{
	@FXML
	private Button button;
	
	@FXML
	private Label label;
	
	@FXML
	void buttontipsi(ActionEvent event) {
		label.setText ("ba");
		button.setText ("ta");
	}
	
	@Override
	public void initialize (URL location, ResourceBundle resources) {
		label.setOnMouseClicked (event -> {
			label.setText ("chap");
		});
	}
	
	//how to detect click on the label?
	
}
