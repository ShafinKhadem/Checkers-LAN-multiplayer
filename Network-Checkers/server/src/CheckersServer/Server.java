package CheckersServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	private ServerSocket ServSock;
	private ArrayList<NetworkUtil> ncs = new ArrayList<> ();
//	private int[] itsPair = new int[1000+5];
	
	Server () {
		try {
			ServSock = new ServerSocket(33333);
			Socket clientSock;
			NetworkUtil nc;
			while (true) {
				clientSock = ServSock.accept();
				nc=new NetworkUtil(clientSock);
				final NetworkUtil _nc = nc;
				new Thread (() -> {
					try {
						while (true) {
							String s = (String) _nc.read ();
							System.out.println (s);
							/*if (s == null) {
								break;
							}*/
							if (s.equals ("new client")) {
								ncs.add (_nc);
								_nc.write ("index"+" "+(ncs.size ()-1));
							}
							else {
								String[] os = s.split (" ");
								int index = Integer.parseInt (os[2]);
								NetworkUtil i = ncs.get (index^1);
								i.write (s);
								System.out.println (index^1);
								//temporary solution as zodi majhe keu disconnect kore tahole shesh
								//have to implement waiting for opponenent and handle previous line
							}
						}
					} catch (Exception e) {
						System.out.println (e);
					}
					try {
						Thread.sleep (200);
					} catch (InterruptedException e) {
						e.printStackTrace ();
					}
				}).start ();
			}
		}catch(Exception e) {
			System.out.println("Server starts:"+e);
		}
	}
	
	public static void main(String args[]) {
		Server objServer = new Server ();
	}
}
