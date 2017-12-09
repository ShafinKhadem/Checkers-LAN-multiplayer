package CheckersServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class ServerGui extends Application {
	
	@Override
	public void start (Stage primaryStage) throws Exception {
		new TestServer ();
	}
	
	public static void main (String[] args) {
		launch (args);
	}
}
