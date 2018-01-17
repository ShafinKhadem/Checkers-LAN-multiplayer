package mainGame;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
	//<editor-fold defaultstate="collapsed" desc="variable declarations">
	static final byte NONE = 0, WHITE = 1, BLACK = 2, WHITE_KING = 3, BLACK_KING = 4, MOVE = 5, JUMP = 6;
	Stage game_window, dialog;
	private Scene game_scene, scene;
	private ObjectInputStream in_server;
	private ObjectOutputStream out_server;
	private int itsIndex;
	private boolean singlePlayer = false;
	private boolean jumpOnly = false;
	private Text turn_text;
	private GridPane checkerboard;
	private VBox whitebox, blackbox;
	private final byte GRID_BASEX = 5, GRID_BASEY = 65, GRID_DIMENSION = 60;
	private StackPane[][] grid = new StackPane[10][10];
	private byte[][] state = new byte[10][10];
	private byte[][] valid_to = new byte[10][10];//move and jump ke alada korar jonyo
	private byte selectedRow, selectedCol;
	private Image[] checker_images = new Image[10];
	private final Image white_piece = new Image ("images/chips_white.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image black_piece = new Image ("images/chips_black.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image white_king = new Image ("images/chips_white_king.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image black_king = new Image ("images/chips_black_king.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	private final Image bg_black = new Image ("images/bg_black.png", GRID_DIMENSION, GRID_DIMENSION, false, true, true);
	private final Image bg_white = new Image ("images/bg_white.png", GRID_DIMENSION, GRID_DIMENSION, false, true, true);
	private byte next = WHITE, this_player = WHITE;
	private byte whitePieces = 12, blackPieces = 12;
	private byte dircol[]={1, -1, 1, -1}, dirrow[] = {-1, -1, 1, 1};
	private boolean selected = false;
	private String playerName = "anonymous";
	//</editor-fold>
	
	
	
	public GameMain (byte player) {
		this_player = player;
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
				result.setText ("Black won");
			}
			else {
				result.setText ("White won");
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
		Rectangle rectangle = new Rectangle (60, 3, Color.TRANSPARENT);
		(state == BLACK || state == BLACK_KING ?blackbox:whitebox).getChildren ().add (rectangle);
		rectangle = new Rectangle (60, 12, (state == BLACK || state == BLACK_KING ? Color.BLACK : Color.WHEAT));
		(state == BLACK || state == BLACK_KING ?blackbox:whitebox).getChildren ().add (rectangle);
	}
	
	void reset () {
		whitePieces = 12;
		blackPieces = 12;
		if (selected) {
			grid[selectedRow][selectedCol].getChildren ().remove (2);
		}
		for (byte col = 0; col<8; col++) {
			for (byte row = 0; row<8; row++) {
				if (state[row][col] != NONE) {
					grid[row][col].getChildren ().remove (1);
					state[row][col] = NONE;
				}
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
	
	private void login () {
		
		new Thread(() -> {
			try {
				Socket socket = new Socket ("127.0.0.1", 33333);
				in_server = new ObjectInputStream (socket.getInputStream());
				out_server = new ObjectOutputStream (socket.getOutputStream());
				out_server.writeObject ("new client");
				String s;
				StringTokenizer st;
				while (true) {
					s = (String) in_server.readObject ();
					st = new StringTokenizer (s);
					if (s.startsWith ("index")) {
						st.nextToken ();
						itsIndex = Integer.parseInt (st.nextToken ());
						if ((itsIndex&1) != 0) {
							this_player = BLACK;
							Platform.runLater (() -> {
								turn_text.setText ("White's turn");
								for (int row = 0; row<8; row++) {
									for (int col = 0; col<8; col++) {
										checkerboard.add (grid[row][col], 7-col, 7-row);
									}
								}
							});
						}
						System.out.println (itsIndex+" "+(itsIndex^1));
					}
					else if (s.equals ("pair done")) {
						this_player = WHITE;
						Platform.runLater (() -> {
							turn_text.setText ("White's turn");
							for (int row = 0; row<8; row++) {
								for (int col = 0; col<8; col++) {
									checkerboard.add (grid[row][col], col, row);
								}
							}
						});
					}
					else if (s.startsWith ("surrender")) {
						if (next == this_player) {
							changeNext ();
						}
						Platform.runLater(() -> {
							finish ();
						});
					}
					else {
						final byte _a = Byte.parseByte (st.nextToken ()), _b = Byte.parseByte (st.nextToken ());
						System.out.println (_a+" "+_b);
						Platform.runLater(() -> {
							click (_a, _b);
						});
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
				Platform.runLater (()->{
					turn_text.setText ("White's turn");
					for (int row = 0; row<8; row++) {
						for (int col = 0; col<8; col++) {
							checkerboard.add (grid[row][col], col, row);/*else show waiting*/
						}
					}
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
		for (int i = 0; i<8; i++) {
			for (int j = 0; j<8; j++) {
				valid_to[i][j] = NONE;
			}
		}
	}
	
	
	private boolean mate () {
		for (byte i = 0; i<8; i++) {
			for (byte j = 0; j<8; j++) {
				if ((state[i][j] == next || state[i][j] == next+2) && set_valids (i, j)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private void changeNext(){
		if (next == WHITE) {
			next = BLACK;
			turn_text.setText ("Black's turn");
		}
		else if (next== BLACK) {
			next = WHITE;
			turn_text.setText ("White's turn");
		}
		if (mate () && (singlePlayer || this_player==next)) {
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
		}
		return flg_jmp || flg_mv;
	}
	
	private void move (byte toRow, byte toCol) {
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
				grid[selectedRow][selectedCol].getChildren ().remove (2);
				select_cell (_row, _col);
				reset_validTo ();
				set_valids (_row, _col);
			}
		}
		else {
			if (state[_row][_col] == next || state[_row][_col] == next+2) {
				select_cell (_row, _col);
				reset_validTo ();
				set_valids (_row, _col);
			}
		}
	}
	
	@Override
	public void start (Stage primaryStage) throws Exception {
		checker_images[WHITE] = white_piece;
		checker_images[BLACK] = black_piece;
		checker_images[WHITE_KING] = white_king;
		checker_images[BLACK_KING] = black_king;
		next = WHITE;
		game_window = primaryStage;
		Parent root = FXMLLoader.load (getClass ().getResource ("mainscene.fxml"));
		game_scene = new Scene (root);
		turn_text = (Text) game_scene.lookup ("#turn");
		turn_text.setText ("Waiting for opponent");
		checkerboard = (GridPane) game_scene.lookup ("#checkerBoard");
		whitebox = (VBox) game_scene.lookup ("#whitebox");
		blackbox = (VBox) game_scene.lookup ("#blackbox");
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
		game_window.show ();
		primaryStage.setResizable (false);
		primaryStage.setOnCloseRequest(e -> System.exit(1));
		login ();
	}
}
