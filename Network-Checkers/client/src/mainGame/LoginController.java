package mainGame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginController {
	@FXML
	private TextField user, password;
	@FXML
	private Text win, result;
	
	@FXML
	void signIn(ActionEvent event) {
		GameMain.playerName = user.getText ();
		GameMain.passWord = password.getText ();
		System.out.println ("Entered name: "+GameMain.playerName+" password: "+GameMain.passWord);
		ClientMain.game.login ();
	}
	
	@FXML
	void signUp (ActionEvent event) {
		GameMain.playerName = user.getText ();
		GameMain.passWord = password.getText ();
		ClientMain.game.signup ();
	}
}
