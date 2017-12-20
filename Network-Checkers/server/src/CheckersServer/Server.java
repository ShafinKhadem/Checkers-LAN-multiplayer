package CheckersServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
	private ArrayList<NetworkUtil> ncs = new ArrayList<> ();
	private boolean[] off = new boolean[1000+5];
	
	@Override
	public void run () {
		try {
			ServerSocket servSock = new ServerSocket (33333);
			Socket clientSock;
			NetworkUtil nc;
			while (true) {
				clientSock = servSock.accept();
				nc=new NetworkUtil(clientSock);
				final NetworkUtil _nc = nc;
				new Thread (() -> {
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
						if (s.equals ("new client")) {
							ncs.add (_nc);
							int index = (ncs.size ()-1);
							_nc.write ("index"+" "+index);
							if ((index&1) != 0) {
								NetworkUtil nu = ncs.get (index^1);
								nu.write ("pair done");
							}
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
		}catch(Exception e) {
			System.out.println ("Server error:");
			e.printStackTrace (System.out);
		}
	}
}
