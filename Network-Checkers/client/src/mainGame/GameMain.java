package mainGame;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * @author Nafiur Rahman Khadem
 */

public class GameMain extends Application {
	//<editor-fold defaultstate="collapsed" desc="Declarations">
	private static final byte NONE = 0, WHITE = 1, BLACK = 2, WHITE_KING = 3, BLACK_KING = 4, MOVE = 5, JUMP = 6;
	private static String whiteName, blackName, playerName, passWord;
	static int itsIndex, itsGamesPlayed, itsGamesWon;
	Stage game_window, dialog;
	private Scene game_scene, scene;
	private ObjectInputStream in_server;
	private ObjectOutputStream out_server;
	private boolean jumpOnly, signIn, selected, singlePlayer;
	private Text turn_text, name, nameTitle, opponent, opponentTitle;
	private GridPane checkerboard;
	private VBox whitebox, blackbox;
	private StackPane[][] grid = new StackPane[10][10];
	private final byte /*GRID_BASEX = 5, GRID_BASEY = 65, */GRID_DIMENSION = 60, dircol[]={1, -1, 1, -1}, dirrow[] = {-1, -1, 1, 1};
	private byte[][] state = new byte[10][10];
	private byte[][] valid_to = new byte[10][10];//move and jump ke alada korar jonyo
	private byte selectedRow, selectedCol, next, this_player, whitePieces, blackPieces;
	private Image[] checker_images = new Image[10];
	private final Image white_piece = new Image ("images/chips_white.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image black_piece = new Image ("images/chips_black.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image white_king = new Image ("images/chips_white_king.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image black_king = new Image ("images/chips_black_king.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image bg_black = new Image ("images/bg_black.png", GRID_DIMENSION, GRID_DIMENSION, false, true, true);
	private final Image bg_white = new Image ("images/bg_white.png", GRID_DIMENSION, GRID_DIMENSION, false, true, true);
	//</editor-fold>
	
	
	
	//<editor-fold defaultstate="collapsed" desc="Gameplay logics and helper functions">
	GameMain () {
		this_player = WHITE;
	}
	
	private void set_scene (Stage window, String sceneFile) {
		try {
			scene = new Scene (FXMLLoader.load (GameMain.class.getResource (sceneFile)));
			window.setScene (scene);
		} catch (IOException e) {
			System.out.println ("fxml file could not be loaded");
			e.printStackTrace (System.out);
		}
	}
	
	void showHelp () {
		dialog = new Stage ();
		dialog.initModality (Modality.NONE);
		dialog.initOwner (game_window);
		set_scene (dialog, "helpscene.fxml");
		dialog.show ();
	}
	
	private void finish () {
		dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(game_window);
		set_scene (dialog, "finishedscene.fxml");
		Text result = (Text) scene.lookup ("#result");
		if (blackPieces <= 2 && blackPieces==whitePieces) {
			result.setText ("Tie");
		}
		else {
			if (next == WHITE) {
				result.setText (blackName+" (Black) won");
			}
			else {
				result.setText (whiteName+" (White) won");
			}
		}
		dialog.setOnCloseRequest (event -> System.exit (1));
		dialog.show();
	}
	
	
	void surrender () {
		if (!singlePlayer) {
			try {
				out_server.writeObject ("surrender this"+" "+itsIndex);
			} catch (IOException e) {
				System.out.println ("surrender couldn't be sent to server");
				e.printStackTrace (System.out);
			}
			next = this_player;
			finish ();
		}
		else finish ();
	}
	
	private void add_piece (byte row, byte col, byte state) {
		grid[row][col].getChildren ().add (new ImageView (checker_images[state]));
	}
	
	void reset () {
		//<editor-fold defaultstate="collapsed" desc="initializations">
		checker_images[WHITE] = white_piece;
		checker_images[BLACK] = black_piece;
		checker_images[WHITE_KING] = white_king;
		checker_images[BLACK_KING] = black_king;
		next = WHITE;
		whiteName = playerName = blackName = passWord = "";
		itsGamesPlayed = itsGamesWon = 0;
		whitePieces = blackPieces = 12;
		jumpOnly = selected = singlePlayer = false;
		signIn = true;
		turn_text = (Text) game_scene.lookup ("#turn");
		name = (Text) game_scene.lookup("#name");
		opponent = (Text) game_scene.lookup("#opponentName");
		opponentTitle = (Text) game_scene.lookup("#opponentTitle");
		nameTitle = (Text) game_scene.lookup("#nameTitle");
		opponentTitle.setVisible(false);
		nameTitle.setVisible(false);
		name.setVisible (false);
		opponent.setVisible (false);
		turn_text.setText ("Waiting for opponent...");
		checkerboard = (GridPane) game_scene.lookup ("#checkerBoard");
		whitebox = (VBox) game_scene.lookup ("#whitebox");
		blackbox = (VBox) game_scene.lookup ("#blackbox");
		//</editor-fold>\
		for (byte col = 0; col<8; col++) {
			for (byte row = 0; row<8; row++) {
				valid_to[row][col] = NONE;
				state[row][col] = NONE;
				if (((row+col)&1) != 0) {
					if (row >= 5) {
						state[row][col] = WHITE;
					}
					else if (row<=2) {
						state[row][col] = BLACK;
					}
					if (state[row][col] != NONE) {
						add_piece (row, col, state[row][col]);
					}
				}
			}
		}
	}
	
	private void captureAnimation (byte row, byte col, byte state) {
		if (blackPieces+whitePieces<24) checkerboard.getChildren ().remove (64);
		ImageView capturedPiece = new ImageView (checker_images[state]);
		if (singlePlayer || this_player == WHITE) {
			checkerboard.add (capturedPiece, col, row);
		}
		else {
			checkerboard.add (capturedPiece, 7-col, 7-row);
		}
		TranslateTransition tt = new TranslateTransition(Duration.millis(400), capturedPiece);
		tt.setToX (600);
		tt.play();
		ImageView imageView = new ImageView (checker_images[state]);
		imageView.setFitHeight (40);
		imageView.setFitWidth (40);
		(state == BLACK || state == BLACK_KING ? blackbox : whitebox).getChildren ().add (imageView);
	}
	private void select_cell (byte row, byte col) {
		Rectangle rectangle = new Rectangle (55, 55, Color.TRANSPARENT);
		rectangle.setStroke (Color.GREEN);
		rectangle.setStrokeWidth (5);
		grid[row][col].getChildren ().add (rectangle);
		selectedRow = row;
		selectedCol = col;
		selected = true;
	}
	
	private boolean valid_index (byte row, byte col) {
		return row >= 0 && row<8 && col >= 0 && col<8;
	}
	
	private void reset_validTo () {
		for (int row = 0; row<8; row++) {
			for (int col = 0; col<8; col++) {
				valid_to[row][col] = NONE;
				if (state[row][col] == NONE && grid[row][col].getChildren ().size ()>1) {
					grid[row][col].getChildren ().remove (1);
				}
			}
		}
	}
	
	
	private boolean mate () {
		for (byte row = 0; row<8; row++) {
			for (byte col = 0; col<8; col++) {
				if ((state[row][col] == next || state[row][col] == next+2) && set_valids (row, col)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private void changeNext(){
		if (next == WHITE) {
			next = BLACK;
			turn_text.setText (blackName+" (Black)'s turn");
		}
		else if (next== BLACK) {
			next = WHITE;
			turn_text.setText (whiteName+" (White)'s turn");
		}
		if (mate () && (singlePlayer || this_player == next)) {
			surrender ();
		}
	}
	
	private boolean set_valids (byte row, byte col) {
		byte strt = 0, nd = 2, stt = state[row][col];//strt and nd by default (for white)
		if (stt == WHITE_KING || stt == BLACK_KING) {
			nd = 4;
		}
		else if (stt == BLACK) {
			strt = 2;
			nd = 4;
		}
		boolean flg_jmp = false, flg_mv = false;
		for (byte i = strt; i<nd; i++) {
			byte trgtrow = (byte) (row+dirrow[i]), trgtcol = (byte) (col+dircol[i]), trgtstt;
			if (!valid_index (trgtrow, trgtcol)) {
				continue;
			}
			trgtstt = state[trgtrow][trgtcol];
			if (!jumpOnly && trgtstt == NONE) {
				valid_to[trgtrow][trgtcol] = MOVE;
				flg_mv = true;
			}
			else {
				byte trgtrow2 = (byte) (trgtrow+dirrow[i]), trgtcol2 = (byte) (trgtcol+dircol[i]);
				boolean vld_jmp = (valid_index (trgtrow2, trgtcol2) && state[trgtrow2][trgtcol2] == NONE);
				if (vld_jmp) {
					if ((stt == BLACK || stt == BLACK_KING) && (trgtstt == WHITE || trgtstt== WHITE_KING)) {
						valid_to[trgtrow2][trgtcol2] = JUMP;
						flg_jmp = true;
					} else if ((stt == WHITE || stt == WHITE_KING) && (trgtstt == BLACK || trgtstt== BLACK_KING)) {
						valid_to[trgtrow2][trgtcol2] = JUMP;
						flg_jmp = true;
					}
				}
			}
		}
		if (jumpOnly && !flg_jmp) {
			jumpOnly = false;
			changeNext ();
		}
		else if (jumpOnly) {
			select_cell (selectedRow, selectedCol);
			showPossibleMoves ();
		}
		return flg_jmp || flg_mv;
	}
	
	private void showPossibleMoves () {
		for (int row = 0; row<8; row++) {
			for (int col = 0; col<8; col++) {
				if (valid_to[row][col] != NONE) {
					Rectangle rectangle = new Rectangle (55, 55, Color.TRANSPARENT);
					rectangle.setStroke (Color.YELLOW);
					rectangle.setStrokeWidth (5);
					grid[row][col].getChildren ().add (rectangle);
				}
			}
		}
	}
	
	private void move (byte toRow, byte toCol) {
		reset_validTo ();
		grid[selectedRow][selectedCol].getChildren ().remove (1, 3);
		if (state[selectedRow][selectedCol] == WHITE && toRow == 0) {
			state[selectedRow][selectedCol] = WHITE_KING;
		} else if (state[selectedRow][selectedCol] == BLACK && toRow == 7) {
			state[selectedRow][selectedCol] = BLACK_KING;
		}
		add_piece (toRow, toCol, state[selectedRow][selectedCol]);
		state[toRow][toCol] = state[selectedRow][selectedCol];
		state[selectedRow][selectedCol] = NONE;
		selected = false;
		if (Math.abs (toRow-selectedRow)>1) {
			byte removeRow = (byte) ((selectedRow+toRow)/2), removeCol = (byte) ((selectedCol+toCol)/2);
			grid[removeRow][removeCol].getChildren ().remove (1);
			captureAnimation (removeRow, removeCol, state[removeRow][removeCol]);
			state[removeRow][removeCol] = NONE;
			if (next == WHITE) {
				blackPieces--;
				if (blackPieces == 0) {
					changeNext ();
//					finish ();
				}
			} else if (next == BLACK) {
				whitePieces--;
				if (whitePieces == 0) {
					changeNext ();
//					finish ();
				}
			}
			reset_validTo ();
			jumpOnly = true;
			selectedRow = toRow;
			selectedCol = toCol;
			set_valids (toRow, toCol);
		}
		else {
			changeNext ();
		}
	}
	
	private void click(byte _row, byte _col){
		if (selected) {
			if (valid_to[_row][_col] != NONE) {
				move (_row, _col);
			}
			else if (state[_row][_col] == next || state[_row][_col] == next+2) {
				reset_validTo ();
				grid[selectedRow][selectedCol].getChildren ().remove (2);
				select_cell (_row, _col);
				set_valids (_row, _col);
				showPossibleMoves ();
			}
		}
		else {
			if (state[_row][_col] == next || state[_row][_col] == next+2) {
				reset_validTo ();
				select_cell (_row, _col);
				set_valids (_row, _col);
				showPossibleMoves ();
			}
		}
	}
	//</editor-fold>
	
	
	
	private void showLogin () {
		dialog = new Stage();
		FXMLLoader loader = new FXMLLoader (getClass ().getResource ("loginScene.fxml"));
		try {
			//<editor-fold defaultstate="collapsed" desc="Making the window scaling">
			Region contentRootRegion = loader.load();//if non-scaling window then Parent parent=loader.load()
			//to access controller class's variable from outside: (this statement must be after loader.load())
			LoginController controller = loader.getController ();
			//Set a default "standard" or "100%" resolution
			double origW = 600, origH = 400;
			//If the Region containing the GUI does not already have a preferred width and height, set it.
			//But, if it does, we can use that setting as the "standard" resolution.
			if ( contentRootRegion.getPrefWidth() == Region.USE_COMPUTED_SIZE ) {
				contentRootRegion.setPrefWidth( origW );
			}
			else {
				origW = contentRootRegion.getPrefWidth();
			}
			if ( contentRootRegion.getPrefHeight() == Region.USE_COMPUTED_SIZE ) {
				contentRootRegion.setPrefHeight( origH );
			}
			else {
				origH = contentRootRegion.getPrefHeight();
			}
			//Wrap the resizable content in a non-resizable container (Group)
			Group group = new Group( contentRootRegion );
			//Place the Group in a StackPane, which will keep it centered
			StackPane rootPane = new StackPane();
			rootPane.getChildren().add(group);
			dialog.setTitle( "Login" );
			//Create the scene initally at the "100%" size
			Scene scene = new Scene( rootPane, origW, origH );
			//as rootPane is being used as scene root instead of parent, you have to lookup using rootPane.lookup()
			//Bind the scene's width and height to the scaling parameters on the group
			group.scaleXProperty().bind( scene.widthProperty().divide( origW ) );
			group.scaleYProperty().bind( scene.heightProperty().divide( origH ) );
			//next 4 lines make sure that aspect ratio is maintained
			dialog.setMaxHeight (700);
			dialog.setMaxWidth (700*origW/origH);
			dialog.minHeightProperty ().bind (scene.widthProperty ().multiply (origH).divide (origW));
			dialog.minWidthProperty ().bind (scene.heightProperty ().multiply (origW).divide (origH));
			//</editor-fold>
			
			//to make button press using enter
			controller.signinButton.defaultButtonProperty ().bind (controller.signinButton.focusedProperty ());
			controller.signupButton.defaultButtonProperty ().bind (controller.signupButton.focusedProperty ());
			
			dialog.setScene (scene);
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(game_window);
			dialog.setOnCloseRequest (event -> System.exit (1));
			dialog.show();
			//to make this text field selected by default
			controller.user.requestFocus ();
		} catch (IOException e) {
			e.printStackTrace ();
		}
	}
	
	void signup (String usr, String pswrd) {
		signIn = false;
		login (usr, pswrd);
	}
	
	void login (String usr, String pswrd) {
		playerName = usr;
		passWord = pswrd;
		System.out.println ("Entered name: "+GameMain.playerName+" password: "+GameMain.passWord);
		new Thread (() -> {
			try {
				Socket socket = new Socket ("127.0.0.1", 33333);
				in_server = new ObjectInputStream (socket.getInputStream());
				out_server = new ObjectOutputStream (socket.getOutputStream());
				String s;
				StringTokenizer st;
				if (signIn) {
					out_server.writeObject ("new client "+playerName+" "+passWord);
				}
				else {
					out_server.writeObject ("new signup "+playerName+" "+passWord);
				}
				s = (String) in_server.readObject ();
				while (s.equals ("invalid")) {
					System.out.println (signIn);
					Platform.runLater (()->{
						Alert alert = new Alert (Alert.AlertType.WARNING);
						if (signIn) {
							alert.setContentText ("Invalid login");
						}
						else {
							alert.setContentText ("Username already taken");
						}
						alert.show ();
						signIn = true;
					});
					s = (String) in_server.readObject ();
				}
				String[] os = s.split (" ");
				itsGamesPlayed = Integer.parseInt (os[1]);
				itsGamesWon = Integer.parseInt (os[2]);
				Platform.runLater (()->{
					dialog.hide ();
					game_window.show ();
				});
				StackPane rootpane = (StackPane) game_scene.lookup ("#rootpane");
				ProgressIndicator pi = new ProgressIndicator ();
				pi.setMinSize (200, 200);
				VBox box = new VBox (pi);
				box.setAlignment (Pos.CENTER);
				Platform.runLater (()->rootpane.getChildren ().add (box));
				while (true) {
					s = (String) in_server.readObject ();
					st = new StringTokenizer (s);
					if (s.startsWith ("index")) {
						st.nextToken ();
						itsIndex = Integer.parseInt (st.nextToken ());
						System.out.println ("Pair: "+itsIndex+" "+(itsIndex^1));
					}
					else if (s.startsWith ("pair")) {
						System.out.println (s);
						os = s.split (" ");
						whiteName = os[1];
						blackName = os[2];
						if ((itsIndex&1) == 0) {
							this_player = WHITE;
							turn_text.setText (whiteName+" (White)'s turn");
							nameTitle.setVisible(true);
							opponentTitle.setVisible(true);
							name.setVisible(true);
							opponent.setVisible(true);
							Platform.runLater (() -> {
								rootpane.getChildren ().remove (box);
								for (int row = 0; row<8; row++) {
									for (int col = 0; col<8; col++) {
										checkerboard.add (grid[row][col], col, row);
									}
								}
							});
						}
						else {
							this_player = BLACK;
							turn_text.setText (whiteName+" (White)'s turn");
							nameTitle.setVisible(true);
							opponentTitle.setVisible(true);
							name.setVisible(true);
							opponent.setVisible(true);
							Platform.runLater (() -> {
								rootpane.getChildren ().remove (box);
								for (int row = 0; row<8; row++) {
									for (int col = 0; col<8; col++) {
										checkerboard.add (grid[row][col], 7-col, 7-row);
									}
								}
							});
						}
					}
					else if (s.startsWith ("surrender")) {
						if (next == this_player) {
							changeNext ();
						}
						Platform.runLater(() -> finish ());
						out_server.writeObject ("win");
					}
					else {
						final byte _a = Byte.parseByte (st.nextToken ()), _b = Byte.parseByte (st.nextToken ());
						System.out.println (_a+" "+_b);
						Platform.runLater(() -> click (_a, _b));
					}
					if (this_player == WHITE) {
						name.setText(whiteName);
						opponent.setText(blackName);
					}
					else {
						name.setText(blackName);
						opponent.setText(whiteName);
					}
					Thread.sleep (300);
				}
			} catch (InterruptedException e) {
				System.out.println ("Game's data processingThread is interrepted");
				e.printStackTrace (System.out);
			} catch (Exception e) {
				System.out.println ("Game's data processing error");
				e.printStackTrace (System.out);
				singlePlayer = true;
				whiteName = blackName = playerName;
				name.setText(whiteName);
				nameTitle.setVisible(true);
				name.setVisible(true);
				turn_text.setText (whiteName+" (White)'s turn");
				Platform.runLater (()->{
					for (int row = 0; row<8; row++) {
						for (int col = 0; col<8; col++) {
							checkerboard.add (grid[row][col], col, row);
						}
					}
					dialog.hide ();
					game_window.show ();
				});
				System.out.println ("No more communicating with server as an offline game");
				Thread.currentThread ().interrupt ();
				return;
			}
			try {
				if (!singlePlayer) {
					in_server.close ();
					out_server.close ();
				}
			} catch (IOException e) {
				System.out.println ("socket's input or output stream couldn't be closed");
				e.printStackTrace (System.out);
			}
		}).start ();
	}
	
	@Override
	public void start (Stage primaryStage) throws Exception {
		game_window = primaryStage;
		Parent root = FXMLLoader.load (getClass ().getResource ("mainscene.fxml"));
		game_scene = new Scene (root);
		for (byte col = 0; col<8; col++) {
			for (byte row = 0; row<8; row++) {
				final byte _col = col, _row = row;
				grid[row][col] = new StackPane (new ImageView (((row+col)&1) != 0 ? bg_black : bg_white));
				grid[row][col].setOnMouseClicked (event -> {
					if ((next == this_player || singlePlayer) && !(jumpOnly && valid_to[_row][_col] == NONE)) {
						System.out.println (_row+" "+_col+" "+valid_to[_row][_col]+" "+next+" "+state[_row][_col]+" "+jumpOnly);
						if (!singlePlayer) {
							try {
								out_server.writeObject (Byte.toString (_row)+" "+Byte.toString (_col)+" "+Integer.toString (itsIndex));
							} catch (IOException e) {
								System.out.println ("sending to server error");
								e.printStackTrace (System.out);
							}
						}
						click (_row, _col);
					}
				});
				state[row][col] = NONE;
			}
		}
		reset ();
		game_window.setTitle ("Multiplayer Checkers");
		game_window.getIcons ().add (new Image ("images/icon.png"));
		game_window.setScene (game_scene);
		primaryStage.setResizable (false);
		primaryStage.setOnCloseRequest(e -> System.exit(1));
		showLogin ();
	}
}
