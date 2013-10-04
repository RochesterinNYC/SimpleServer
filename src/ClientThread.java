import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientThread extends Thread{
	
	private Client client;
	
	public ClientThread(Client client){
		this.client = client;
	}
	
	public void begin(){
		if(client.login()){
			ClientThread ct = new ClientThread(client);
			ct.start();
	    	client.optionMenu();
		}
	}
	
	public void run() {
		try {
			Socket broadcastSocket = new Socket(client.getHost(), client.getPortNumber());
			Scanner broadcastToClient = new Scanner(new InputStreamReader(broadcastSocket.getInputStream()));
			System.out.println(broadcastToClient.nextLine());
		}
		catch (IOException e){
			System.out.println("oops!");
		}
	}
	
	
	
}
