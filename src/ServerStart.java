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
	public static void main(String[] args) throws IOException{
		ServerThread serverStart = new ServerThread(new Server(Integer.parseInt(args[0])));
		serverStart.serverListen();
	}
}