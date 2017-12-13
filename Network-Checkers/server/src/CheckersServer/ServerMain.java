package CheckersServer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {
	
	@Override
	public void start (Stage primaryStage) throws Exception {
		Scene scene = new Scene (FXMLLoader.load (getClass ().getResource ("serverscene.fxml")));
		primaryStage.setScene (scene);
		primaryStage.show ();
		primaryStage.setOnCloseRequest (event -> System.exit (1));
		new Server ().start ();
	}
	
	public static void main (String[] args) {
		launch (args);
	}
}
