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
		GameMain.reset ();
		GameMain.dialog.hide ();
	}
}
