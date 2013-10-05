import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
/**
* <b>ServerThread Class</b>
* <p>
* Represents the server thread that runs on the server.
* Has different thread types as enumerated by ServerThreadType.
* @author James Wen - jrw2175
*/
public class ServerThread extends Thread{
	private Server server;
	private PrintWriter serverToClient;
	private Scanner clientToServer;
	private Socket clientSocket;
	private String userName;
	private ServerThreadType threadType;
	
    /**
    * ServerThread constructor
    * <p>
    * Creates a server thread.
    * Used for creating base server thread (that listens for connections).
    * @param server - the main server
    */
	public ServerThread(Server server){
		this.server = server;
		this.threadType = ServerThreadType.BASE;
	}
    /**
    * ServerThread constructor
    * <p>
    * Creates a server thread.
    * @param server - the main server
    * @param clientSocket - connection to a client
    * @param type - the type of this ServerThread
    * @throws IOException
    */
	public ServerThread(Server server, Socket clientSocket, ServerThreadType type) throws IOException{
		this.server = server;
		this.clientSocket = clientSocket;
		this.serverToClient = new PrintWriter(this.clientSocket.getOutputStream(), true);
		this.clientToServer = new Scanner(new InputStreamReader(this.clientSocket.getInputStream()));
		this.threadType = type;
	}
	
    /**
    * serverListen
    * <p>
    * Initiates the server operations (starts running the server).
    * @throws IOException
    */
	public void serverListen() throws IOException{
		//Starts the console thread
		ConsoleThread console = new ConsoleThread(server);
		console.start();
		while(true){
			//Creates login/client handler thread
			Socket newSocket = server.getServerSocket().accept();
			ServerThread st;
			st = new ServerThread(server, newSocket, ServerThreadType.CLIENT);
    		st.start();
		}
	}

    /**
    * run
    * <p>
    * Starts the thread and checks if the client's IP is blocked, and if not,
    * initiates login.
    */
	public void run(){
		//Check is client's IP is currently blocked
		if(server.isBlocked(clientSocket.getInetAddress())){
			inputToClient("ip blocked");
		}
		else{			
			if (this.threadType == ServerThreadType.CLIENT){
				inputToClient("ip not blocked");
				try{
					login();
				}
				catch(IOException e){
	    		
				}
			}
		}
	}	

    /**
    * optionMenu
    * <p>
    * Presents client with option menu and performs operations based
    * on user response.
    */
    public void optionMenu(){
    	String choice = "";
    	boolean correctCommand = false;
    	while (!correctCommand){ 		
    		inputToClient("These are your options:");
    		inputToClient("- Enter 'whoelse' to see what other users are connected on this server.");
    		inputToClient("- Enter 'wholasthr' to see what users have connected within the last hour.");
    		inputToClient("- Enter 'logout' to logout from this account.");
    		inputToClient("What would you like to do?");
    		choice = outputFromClient();
    		if(choice.trim().equals("whoelse") || choice.trim().equals("wholasthr") || choice.trim().equals("logout")){
    			correctCommand = true;
    			inputToClient("success");
    			//No String switching in < Java 1.7...
    			if(choice.trim().equals("whoelse")){
    				whoelse();
    			}
    			else if(choice.trim().equals("wholasthr")){
    				wholasthr();
    			}
    			else if(choice.trim().equals("logout")){
    				logout();
    			}
    		}
    		else{
    			inputToClient("failure");
    			inputToClient("Please enter in a correct command.");
    		}
    	}
    }

    /**
    * logout
    * <p>
    * Logs the client out.
    */ 
    public void logout(){
    	server.logout(this);
    	server.printLog("Logout Successful. User " + this.userName + " logged out");
    	inputToClient("You are now logged out from SimpleServer and the account under " + this.userName);
    	inputToClient("Have a nice day!");
    }
    
    /**
    * whoelse
    * <p>
    * Sends the client what other clients are logged onto the server.
    */    
    public void whoelse(){
    	boolean userScreened = false;
    	String[] users = server.getCurrentUsers();
    	inputToClient(Integer.toString(users.length - 1));
    	for (String user : users){
    		if(user.equals(userName)){
    			if(userScreened){
    				inputToClient(user);   
    			}
    			else{
    				userScreened = true;
    			}
    		}
    		else{
    			inputToClient(user);
    		}	
    	}
    	optionMenu();
    }
    
    /**
    * whoelse
    * <p>
    * Sends what accounts have been logged into on the server in the last hour.
    */
    public void wholasthr(){
    	ArrayList<String> users = server.getUsersLastHr();
    	inputToClient(Integer.toString(users.size()));
    	for (String user : users){
    		inputToClient(user);   
    	}
    	optionMenu();
    }
    
    /**
    * login
    * <p>
    * Handles the client login operations.
    * @throws IOException
    */
    public void login() throws IOException{
		boolean loginSuccess = false;
		int loginAttempts = 0;
    	while(!loginSuccess && loginAttempts < server.getNumLoginAttempts()){
    		inputToClient("Hi! Welcome to SimpleServer!");
    		inputToClient("Please enter your login info.");
    		inputToClient("Username: ");
    		String userNameAttempt = outputFromClient();
    		inputToClient("Password: ");
    		String passwordAttempt = outputFromClient();
    		loginSuccess = server.loginCorrect(userNameAttempt, passwordAttempt);
    		loginAttempts++;
    		if (loginSuccess){
    			inputToClient("success");
    			this.userName = userNameAttempt;
    			server.addToClients(this);
    			server.printLog("Login Successful. User " + this.userName + " logged in");
    			optionMenu();
    		}
    		else if (loginAttempts >= 3){
    			inputToClient("blocked");
    			server.blockIP(clientSocket.getInetAddress());
    		}
    		else {
    			inputToClient("failure");
    		}
		}
	}
    
    /**
    * getSocket
    * <p>
    * Returns the socket representing this server thread's connection
    * with the client.
    * @return clientSocket - connection with client
    */
    public Socket getSocket(){
		return clientSocket;
	}
    /**
    * getUserName
    * <p>
    * Returns the username that the client logged in with.
    * @return userName - username
    */
    public String getUserName(){
    	return userName;
    }
    /**
    * inputToClient
    * <p>
    * Input a query or response to the client.
    * @param input - what is to be sent to the client
    */
	public void inputToClient(String input){
		serverToClient.println(input);
	}
    /**
    * outputFromClient
    * <p>
    * Read the client's response
    * @return the client's response
    */
    public String outputFromClient(){
    	return clientToServer.nextLine();
	}
    
}
