package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
		Scene scene = new Scene (root, 600, 475);
		primaryStage.setScene (scene);
		Button btn = (Button) scene.lookup ("#button");
		btn.setText ("Main");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
