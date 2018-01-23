package mainGame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class GameController {
	@FXML
	public GridPane checkerBoard;
	public Text turn;
	
	@FXML
	void help(ActionEvent event) {
		ClientMain.game.showHelp ();
	}
	
	@FXML
	void surrender(ActionEvent event){
		ClientMain.game.surrender ();
	}
	
	@FXML
	void showHistory (ActionEvent actionEvent) {
		Stage history = new Stage ();
		try {
			Scene historyscene = new Scene (FXMLLoader.load (GameMain.class.getResource ("historyscene.fxml")));
			history.setScene (historyscene);
			Text played = (Text) historyscene.lookup ("#played");
			Text won = (Text) historyscene.lookup ("#won");
			played.setText (Integer.toString (GameMain.itsGamesPlyed));
			won.setText (Integer.toString (GameMain.itsGamesWon));
		} catch (IOException e) {
			System.out.println ("fxml file could not be loaded");
			e.printStackTrace (System.out);
		}
		history.show ();
	}
}
