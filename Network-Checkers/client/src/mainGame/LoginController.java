package mainGame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginController {
	public TextField user;
	public Button signinButton, signupButton;
	@FXML
	private PasswordField password;
	@FXML
	private Text win, result;
	
	@FXML
	void signIn(ActionEvent event) {
		ClientMain.game.login (user.getText (), password.getText ());
	}
	
	@FXML
	void signUp (ActionEvent event) {
		ClientMain.game.signup (user.getText (), password.getText ());
	}
}
