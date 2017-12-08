package mainGame;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	public static void main (String[] args) {
		launch (args);
	}
	
	@Override
	public void start (Stage primaryStage) {
		GameMain game = new GameMain (GameMain.RED);
		try {
			game.start (primaryStage);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
}
