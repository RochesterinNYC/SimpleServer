import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Socket conn = new Socket("127.0.0.1", 9999);
		Scanner localInput = new Scanner(System.in);
		PrintWriter serverOut = new PrintWriter(conn.getOutputStream(), true);
    	Scanner clientInput = new Scanner(new InputStreamReader(conn.getInputStream()));
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		serverOut.println(localInput.nextLine());
		System.out.println(clientInput.nextLine());
		serverOut.println(localInput.nextLine());
		System.out.println(clientInput.nextLine());
	}

}
