import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client extends Thread{
    private Scanner localInput;
    private PrintWriter serverOutput;
    private Scanner clientInput;
    private Socket conn;
	
    public Client(String host, int portNumber) throws IOException{
    	conn = new Socket(host, portNumber);
		localInput = new Scanner(System.in);
		serverOutput = new PrintWriter(conn.getOutputStream(), true);
    	clientInput = new Scanner(new InputStreamReader(conn.getInputStream()));
    }
    
	public void run(){
    	login();
    	menuOptions();
	}
	
	public void login(){
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		serverOutput.println(localInput.nextLine());
		System.out.println(clientInput.nextLine());
		serverOutput.println(localInput.nextLine());
		System.out.println(clientInput.nextLine());
	}
	
	public void menuOptions(){
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		System.out.println(clientInput.nextLine());
		serverOutput.println(localInput.nextLine());
		System.out.println(clientInput.nextLine());
	}

}
