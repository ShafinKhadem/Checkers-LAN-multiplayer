package CheckersServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
	private ArrayList<NetworkUtil> ncs = new ArrayList<> ();
//	private int[] itsPair = new int[1000+5];
	
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
						String s = (String) _nc.read ();
						System.out.println (s);
						if (s == null) {
							int sz = ncs.size ();
							for (int i = 0; i<sz; i++) {
								if (ncs.get (i) == _nc) {
									System.out.println (i);
									if ((i^1)<sz) {
										ncs.get (i^1).write ("surrender");
									}
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
							System.out.println (index^1);
							//let's ignore offer draw now.
						}
						try {
							Thread.sleep (300);
						} catch (InterruptedException e) {
							e.printStackTrace ();
						}
					}
					_nc.closeConnection ();
				}).start ();
				Thread.sleep (500);
			}
		}catch(Exception e) {
			System.out.println("Server starts:"+e);
		}
	}
}
