package CheckersServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkUtil
{
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public NetworkUtil (Socket s) {//To make object from socket with all necessary methods
		try {
			this.socket = s;
			oos=new ObjectOutputStream(socket.getOutputStream());
			ois=new ObjectInputStream(socket.getInputStream());
		} catch (Exception e) {
			System.out.println("In NetworkUtil : " + e.toString());
		}
	}

	public Object read() {
		Object o = null;
		try {
			o=ois.readObject();
		} catch (Exception e) {
		  	System.out.println("Reading Error in network : " + e.toString());
		}
		return o;
	}
	
	public void write(Object o) {
		try {
			oos.writeObject(o);                        
		} catch (IOException e) {
			System.out.println("Writing  Error in network : " + e.toString());
		}
	}

	public void closeConnection() {
		try {
			ois.close();
			oos.close();
		} catch (Exception e) {
			System.out.println("Closing Error in network : "  + e.toString());
		}
	}
}

