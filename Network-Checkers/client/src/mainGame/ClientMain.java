package mainGame;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	public static void main (String[] args) {
		launch (args);
	}
	public static GameMain game;
	public static Stage staticStage;
	
	@Override
	public void start (Stage primaryStage) {
		staticStage = primaryStage;
		game = new GameMain ();
		try {
			game.start (primaryStage);
		} catch (Exception e) {
			System.out.println ("Game starting error");
			e.printStackTrace ();
		}
	}
}
