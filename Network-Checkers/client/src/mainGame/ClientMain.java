package mainGame;

import javafx.application.Application;
import javafx.stage.Stage;

import static mainGame.GameMain.RED;

public class ClientMain extends Application {
	
	public static void main (String[] args) {
		launch (args);
	}
	public static GameMain game;
	public static Stage staticStage;
	
	@Override
	public void start (Stage primaryStage) {
		staticStage = primaryStage;
		game = new GameMain (RED);
		try {
			game.stop ();
			game.start (primaryStage);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
}
