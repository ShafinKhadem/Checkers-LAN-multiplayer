package mainGame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class Controller {
	@FXML
	public GridPane checkerBoard;
	public Text turn;
	
	@FXML
	void help(ActionEvent event) {
		GameMain.showHelp ();
	}
	
	@FXML
	void surrender(ActionEvent event){
		GameMain.surrender ();
	}
	
	/*@FXML
	void offerdraw (ActionEvent actionEvent) {
	}*/
}
