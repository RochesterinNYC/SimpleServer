import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client extends Thread{
    private Scanner localInput;
    private PrintWriter clientToServer;
    private Scanner serverToClient;
    private Socket conn;
	public void inputToServer(String input){
		clientToServer.println(input);
	}
    public String outputFromServer(){
    	return serverToClient.nextLine();
	}
    public Client(String host, int portNumber) throws IOException{
    	conn = new Socket(host, portNumber);
		localInput = new Scanner(System.in);
		clientToServer = new PrintWriter(conn.getOutputStream(), true);
		serverToClient = new Scanner(new InputStreamReader(conn.getInputStream()));
    }
	
	public boolean login(){
		boolean loginSuccessful = false;
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		String userName = localInput.nextLine();
		inputToServer(userName);
		System.out.println(outputFromServer());
		inputToServer(localInput.nextLine());
		//Response Message from Server
		String responseMessage = outputFromServer();
		if(responseMessage.equals("success")){
			loginSuccessful = true;
			System.out.println("Hi! You are now logged into SimpleServer as " + userName);
		}
		else if(responseMessage.equals("failure")){
			System.out.println("Login failed.");
		}
		return loginSuccessful;
	}
	
	public void menuOptions(){
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		inputToServer(localInput.nextLine());
		System.out.println(outputFromServer());
	}

}
