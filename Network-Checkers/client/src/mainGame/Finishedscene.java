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
		ClientMain.game.dialog.hide ();
		ClientMain.game.reset ();
		try {
			ClientMain.game.stop ();
			ClientMain.game.start (ClientMain.staticStage);
		} catch (Exception e) {
			System.out.println ("game couldn't be restarted");
			e.printStackTrace (System.out);
		}
	}
	
	@FXML
	void quit (ActionEvent event) {
		System.exit (0);
	}
}
