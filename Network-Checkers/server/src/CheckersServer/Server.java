package CheckersServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Nafiur Rahman Khadem
 */

public class Server extends Thread {
	private ArrayList<NetworkUtil> ncs = new ArrayList<> ();
	private ArrayList<UserId> userIds = new ArrayList<> ();
	private boolean[] off = new boolean[1000+5];
	private String whitePlayer = "anonymous";
	private final String FILE_NAME = "users.txt";
	
	private void readFile () {
		try {
			BufferedReader br;
			try {
				br = new BufferedReader (new FileReader (FILE_NAME));
			} catch (FileNotFoundException e) {
				InputStream configStream = getClass().getResourceAsStream("/users.txt");
				br = new BufferedReader(new InputStreamReader(configStream));
				e.printStackTrace (System.out);
			}
			String line = br.readLine ();
			String[] ss;
			while (line != null) {
				ss = line.split (" ");
				userIds.add (new UserId (ss[0], ss[1], Integer.parseInt (ss[2]), Integer.parseInt (ss[3])));
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	void saveFile () {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter (FILE_NAME))) {
			for (UserId userId : userIds) {
				bw.write (userId.getUsername ()+" "+userId.getPassword ()+" "+userId.getGamesPlayed ()+" "+userId.getGamesWon ()+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace (System.out);
		}
	}
	
	@Override
	public void run () {
		readFile ();
		try {
			ServerSocket servSock = new ServerSocket (33333);
			Socket clientSock;
			NetworkUtil nc;
			while (true) {
				clientSock = servSock.accept ();
				nc = new NetworkUtil (clientSock);
				final NetworkUtil _nc = nc;
				new Thread (() -> {
					int idxUserIds = -1;
					UserId thisUser = null;
					while (true) {
						String s = _nc.readString ();
						System.out.println (s);
						if (s == null) {
							int sz = ncs.size ();
							for (int i = sz-1; i >= 0; i--) {
								if (ncs.get (i) == _nc && !off[i]) {
									System.out.println ("paisi "+i);
									if ((i^1)<sz) {
										ncs.get (i^1).write ("surrender");
									}
									else {
										ncs.remove (i);
									}
									break;
								}
							}
							break;
						}
						if (s.startsWith ("new")) {
							String[] os = s.split (" ");
							if (s.startsWith ("new signup")) {
								boolean unique = true;
								for (UserId userId : userIds) {
									if (userId.getUsername ().equals (os[2])) {
										unique = false;
										break;
									}
								}
								if (unique) {
									idxUserIds = userIds.size ();
									userIds.add (new UserId (os[2], os[3]));
								}
								else {
									_nc.write ("invalid");
								}
							}
							else {
								idxUserIds = userIds.indexOf (new UserId (os[2], os[3]));
								if (idxUserIds == -1) {
									_nc.write ("invalid");
								}
							}
							if (idxUserIds != -1) {
								thisUser = userIds.get (idxUserIds);
								_nc.write ("valid "+thisUser.getGamesPlayed ()+" "+thisUser.getGamesWon ());
								thisUser.setGamesPlayed (thisUser.getGamesPlayed ()+1);
								ncs.add (_nc);
								int indexNcs = (ncs.size ()-1);
								_nc.write ("index"+" "+indexNcs);
								if ((indexNcs&1) == 0) {
									whitePlayer = os[2];
									System.out.println ("whitePlayer "+whitePlayer);
								}
								else {
									NetworkUtil nu = ncs.get (indexNcs^1);
									nu.write ("pair "+whitePlayer+" "+os[2]);
									_nc.write ("pair "+whitePlayer+" "+os[2]);
								}
							}
						}
						else if (s.equals ("win")) {
							thisUser.setGamesWon (thisUser.getGamesWon ()+1);
						}
						else {
							String[] os = s.split (" ");
							int index = Integer.parseInt (os[2]);
							NetworkUtil nu = ncs.get (index^1);
							nu.write (s);
							//let's ignore offer draw and separate message class now.
						}
						if (s.startsWith ("surrender")) {
							int sz = ncs.size ();
							for (int i = sz-1; i >= 0; i--) {
								if (ncs.get (i) == _nc) {
									off[i] = true;
									off[i^1] = true;
									System.out.println ("offed "+i+" and "+(i^1));
								}
							}
						}
						try {
							Thread.sleep (300);
						} catch (InterruptedException e) {
							System.out.println ("server thread interrupted");
							e.printStackTrace (System.out);
						}
					}
					_nc.closeConnection ();
				}).start ();
				Thread.sleep (500);
			}
		} catch (Exception e) {
			System.out.println ("Server error:");
			e.printStackTrace (System.out);
		}
		saveFile ();
	}
}
