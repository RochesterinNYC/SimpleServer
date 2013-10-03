import java.io.IOException;


public class Start {
	public static void main(String[] args) throws IOException{
		Server test = new Server(9998);
		test.run();
	}

}
