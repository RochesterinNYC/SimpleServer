import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
* <b>ClientThread Class</b>
* <p>
* A thread for the client that will connect to the server that's listening on the 
* specified port at the specified IP Address.
* @author James Wen - jrw2175
*/
public class ClientThread extends Thread{
	
	private Client client;
	/**
	* ClientThread constructor
	* <p>
	* Creates a client thread with a client
	* @param client - the client 
	*/
	public ClientThread(Client client){
		this.client = client;
	}	
	
	public void begin(){
		if(client.checkIPBlocked()){
			System.out.println("This IP Address is currently blocked due to repeated failed login attempts.");
		}
		//IP is not blocked - initiate login
		if(client.login()){
			ClientThread broadcastThread = new ClientThread(client);
			broadcastThread.start();
	    	client.optionMenu();
		}
	}

	public void run() {
		try {
			Socket broadcastSocket = new Socket(client.getHost(), client.getPortNumber());
			Scanner broadcastFromServer = new Scanner(new InputStreamReader(broadcastSocket.getInputStream()));
			PrintWriter sendBackToServer = new PrintWriter(broadcastSocket.getOutputStream(), true);
			String broadcastMessage = "";
			while(!client.toLogOut()){
				broadcastMessage = broadcastFromServer.nextLine();
				sendBackToServer.println("ack");
				if(!broadcastMessage.equals("")){
					System.out.println("Broadcast: " + broadcastMessage);
				}
				sleep(500);
			}
			broadcastMessage = broadcastFromServer.nextLine();
			sendBackToServer.println("logout");
		}
		catch (IOException e){
			System.out.println("oops!");
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
