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
	* run
	* <p>
	* Runs the thread, checks if the IP it's running on is blocked by the server
	* and if not, then initiates login protocol
	* @param client - the client 
	*/
	public void run(){
		if(client.checkIPBlocked()){
			System.out.println("This IP Address is currently blocked due to repeated failed login attempts.");
		}
		//IP is not blocked - initiate login
		else if(client.login()){
	    	client.optionMenu();
		}
	}
	
	
	
}
