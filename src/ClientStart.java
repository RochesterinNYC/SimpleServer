import java.io.IOException;

/**
* <b>ClientStart Class</b>
* <p>
* Starts a client that will connect to the server that's listening on the 
* specified port at the specified IP Address.
* - Argument 1 = IP Address that server is at
* - Argument 2 = Port at IP that this client wishes to connect to
* @author James Wen - jrw2175
*/
public class ClientStart {
	/**
	* Main method
	* <p>
	* Starts the client thread with the newly created client that will connect
	* to the server specified by the arguments
	*/
	public static void main(String[] args) throws IOException{
		ClientThread clientStart = new ClientThread(new Client(args[0], Integer.parseInt(args[1])));
		clientStart.start();
	}
}
