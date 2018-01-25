package mainGame;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class GameController {
	@FXML
	public StackPane rootpane;
	public GridPane checkerBoard;
	public Text turn, nameTitle, opponentTitle, name, opponentName;
	public VBox whitebox, blackbox;
	
	@FXML
	void help () {
		ClientMain.game.showHelp ();
	}
	
	@FXML
	void surrender (){
		ClientMain.game.surrender ();
	}
	
	@FXML
	void showHistory () {
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
