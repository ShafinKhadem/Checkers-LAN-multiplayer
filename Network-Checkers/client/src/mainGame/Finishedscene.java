package mainGame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class Finishedscene {
	@FXML
	private Text win;
	public Text result;
	
	@FXML
	void rstrt(ActionEvent event) {
		ClientMain.game.reset ();
		try {
			ClientMain.game.start (ClientMain.staticStage);
		} catch (Exception e) {
			e.printStackTrace ();
		}
		ClientMain.game.dialog.hide ();
	}
}
