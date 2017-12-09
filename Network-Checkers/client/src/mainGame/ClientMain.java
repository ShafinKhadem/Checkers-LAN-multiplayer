package mainGame;

import javafx.application.Application;
import javafx.stage.Stage;

import static mainGame.GameMain.BLUE;
import static mainGame.GameMain.RED;

public class ClientMain extends Application {
	
	public static void main (String[] args) {
		launch (args);
	}
	
	@Override
	public void start (Stage primaryStage) {
		GameMain game = new GameMain (RED);
		try {
			game.start (primaryStage);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
}
