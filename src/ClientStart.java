import java.io.IOException;


public class ClientStart {
	public static void main(String[] args) throws IOException{
		ClientThread clientStart = new ClientThread(new Client(args[0], Integer.parseInt(args[1])));
		clientStart.begin();
	}

}
