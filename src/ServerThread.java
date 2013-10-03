import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ServerThread extends Thread{
	private Server server;
	private PrintWriter serverToClient;
	private Scanner clientToServer;
	private Socket clientSocket;
	
	public ServerThread(Server server){
		this.server = server;
	}
	public ServerThread(Server server, Socket clientSocket) throws IOException{
		this.server = server;
		this.clientSocket = clientSocket;
		this.serverToClient = new PrintWriter(this.clientSocket.getOutputStream(), true);
		this.clientToServer = new Scanner(new InputStreamReader(this.clientSocket.getInputStream()));
	}
	
	public void serverListen() throws IOException{
		while(true){
			Socket newSocket = server.getServerSocket().accept();
			ServerThread st = new ServerThread(server, newSocket);
    		st.start();
		}
	}
	
	public void run(){
    	login();
	}
	
	public void inputToClient(String input){
		serverToClient.println(input);
	}
    public String outputFromClient(){
    	return clientToServer.nextLine();
	}
    
    public void optionMenu(){
    	String choice = "";
    	boolean correctCommand = false;
    	while (!correctCommand){ 		
    		inputToClient("These are your options:");
    		inputToClient("- Enter 'whoelse' to see what other users are connected on this server.");
    		inputToClient("- Enter 'wholasthr' to see who else is on this server.");
    		inputToClient("- Enter 'broadcast' to broadcast a message to all connected users.");
    		inputToClient("What would you like to do?");
    		choice = outputFromClient();
    		if(choice.trim().equals("whoelse") || choice.trim().equals("wholasthr") || choice.trim().equals("broadcast")){
    			correctCommand = true;
    		}
    		else{
    			inputToClient("Please enter in a correct command.");
    		}
    	}
    	inputToClient("You chose " + choice);
    }
    
    public void login(){
		inputToClient("Hi! Welcome to SimpleServer!");
		inputToClient("Please enter your login info");
		inputToClient("Username: ");
		String userNameAttempt = outputFromClient();
		inputToClient("Password: ");
		String passwordAttempt = outputFromClient();
		if(server.loginCorrect(userNameAttempt, passwordAttempt)){
			inputToClient("success");
			optionMenu();
		}
		else{
			inputToClient("failure");
		}
	}

}
