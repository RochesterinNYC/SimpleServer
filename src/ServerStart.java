import java.io.IOException;

/**
 * <b>ServerStart Class</b>
 * <p>
 * Starts the server that will be connected to the local port specified
 * in the argument.
 * - Argument 1 = local server port to start server and have it listen on
 * @author James Wen - jrw2175
 */
public class ServerStart {
	/**
	* Main method
	* <p>
	* Starts the base server thread that will initiate the server operations
	* and start listening on the specified port for client connection attempts
	*/
	public static void main(String[] args) throws IOException{
		ServerThread serverStart = new ServerThread(new Server(Integer.parseInt(args[0])));
		serverStart.serverListen();
	}
}