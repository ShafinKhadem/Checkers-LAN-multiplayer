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
					while (true) {
						String s = (String) _nc.read ();
						System.out.println (s);
						if (s == null) {
							//send surrender as he has disconnected
							//but ke disconnect hoise ta bojhar way ki?
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
							//have to implement red waiting for opponenent
							//when opponent disconnects see if s==null
							//to implement New game I think we should send the new client message only
							//let's ignore offer draw now.
						}
						try {
							Thread.sleep (100);
						} catch (InterruptedException e) {
							e.printStackTrace ();
						}
					}
					_nc.closeConnection ();
				}).start ();
			}
		}catch(Exception e) {
			System.out.println("Server starts:"+e);
		}
	}
}
