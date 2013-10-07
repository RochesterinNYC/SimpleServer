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
	
	/**
	* begin
	* <p>
	* Starts the client interaction with the server by checking if the client
	* IP is blocked and if not, initiating login protocol 
	*/
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

	/**
	* run
	* <p>
	* Starts the operations for the broadcasting client thread (that listens for
	* broadcasts)
	* - Has a separate connection with the server
	* - Thread ends when the parallel client thread (that the user is using to
	* interact with the server) logouts
	* - Constantly receives broadcast information from the server, but only
	* prints the broadcast if the broadcast message is not blank
	* - Sends back an acknowledgment message ("ack") every time it receives
	* server broadcast info
	*/
	public void run() {
		try {
			Socket broadcastSocket = new Socket(client.getHost(), client.getPortNumber());
			Scanner broadcastFromServer = new Scanner(new InputStreamReader(broadcastSocket.getInputStream()));
			PrintWriter sendBackToServer = new PrintWriter(broadcastSocket.getOutputStream(), true);
			String broadcastMessage = "";
			//while client is logged in
			while(!client.toLogOut()){
				//receive server broadcast info and acknowledge receiving it
				broadcastMessage = broadcastFromServer.nextLine();
				sendBackToServer.println("ack");
				if(!broadcastMessage.equals("")){
					System.out.println("Broadcast: " + broadcastMessage);
				}
				sleep(500); //Half a second sleep time to reduce server load
				            //due to continual broadcast info
			}
			//logout for client initiated so this broadcast thread ends
			broadcastMessage = broadcastFromServer.nextLine();
			sendBackToServer.println("logout");
			broadcastFromServer.close();
			sendBackToServer.close();
			broadcastSocket.close();
		}
		catch (IOException e){
			System.out.println("An error occurred with the client broadcast channel!");
		} 
		catch (InterruptedException e) {
			System.out.println("An error occurred with the client broadcast channel!");
		}
	}
	
}
