package mainGame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginScene {
	public TextField user;
	@FXML
	private Text win;
	public Text result;
	
	@FXML
	void rstrt(ActionEvent event) {
		GameMain.playerName = user.getText ();
		System.out.println ("Player's name: "+GameMain.playerName);
		ClientMain.game.loginSuccess ();
	}
	
	@FXML
	void quit (ActionEvent event) {
		System.exit (0);
	}
}
