import java.io.IOException;

public class Start {
	public static void main(String[] args) throws IOException{
		ServerThread serverStart = new ServerThread(new Server(9998));
		serverStart.serverListen();
	}

}
