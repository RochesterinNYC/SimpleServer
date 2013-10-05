import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private Scanner localInput;
    private PrintWriter clientToServer;
    private Scanner serverToClient;
    private Socket conn;
    private String host;
    private int portNumber;
	public void inputToServer(String input){
		clientToServer.println(input);
	}
    public String outputFromServer(){
    	return serverToClient.nextLine();
	}
    public Socket getConn(){
    	return conn;
    }
    public Client(String host, int portNumber) throws IOException{
    	conn = new Socket(host, portNumber);
		localInput = new Scanner(System.in);
		clientToServer = new PrintWriter(conn.getOutputStream(), true);
		serverToClient = new Scanner(new InputStreamReader(conn.getInputStream()));
		this.host = host;
		this.portNumber = portNumber;
    }
    public boolean checkIPBlocked(){
    	String blockResponse = outputFromServer();
    	boolean IPBlocked = false;
    	if(blockResponse.equals("ip blocked")){
    		IPBlocked = true;
    	}
    	else if(blockResponse.equals("ip not blocked")){
    		IPBlocked = false;
    	}
    	return IPBlocked;
    }
    public int getPortNumber(){
    	return this.portNumber;
    }
    public String getHost(){
    	return this.host;
    }
	
	public boolean login(){
		boolean loginSuccessful = false;
		boolean loginBlocked = false;
		while(!loginSuccessful && !loginBlocked){
			System.out.println(outputFromServer());
			System.out.println(outputFromServer());
			System.out.println(outputFromServer());
			String userName = localInput.nextLine();
			inputToServer(userName);
			System.out.println(outputFromServer());
			inputToServer(localInput.nextLine());
			//Response Message from Server
			String responseMessage = outputFromServer();
			//Client Response
			if(responseMessage.equals("success")){
				loginSuccessful = true;
				System.out.println("Hi! You are now logged into SimpleServer as " + userName);
			}
			else if(responseMessage.equals("failure")){
				System.out.println("Login failed. Please try again.");
			}
			else if(responseMessage.equals("blocked")){
				loginBlocked = true;
				System.out.println("Too many failed login attempts. Your IP has been blocked from connecting for 60 seconds.");
			}
		}
		return loginSuccessful;
	}
	
	public void optionMenu(){
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		String command = localInput.nextLine();
		inputToServer(command);
		String serverResponse = outputFromServer();
		if(serverResponse.equals("success")){
			if(command.equals("whoelse")){
				whoelse();
			}
			else if (command.equals("wholasthr")){
				wholasthr();
			}
			else if (command.equals("logout")){
				logout();
			}
		}
		else if (serverResponse.equals("failure")){
			System.out.println(outputFromServer());
			optionMenu();
		}
	}
	
	public void logout(){
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
	}
	
    public void whoelse(){
    	int numUsers = Integer.parseInt(outputFromServer());
    	for(int i = 0; i < numUsers; i++){
    		System.out.println(outputFromServer());
    	}
    	optionMenu();
    }
    public void wholasthr(){
    	int numUsers = Integer.parseInt(outputFromServer());
    	for(int i = 0; i < numUsers; i++){
    		System.out.println(outputFromServer());
    	}
    	optionMenu();
    }

}
