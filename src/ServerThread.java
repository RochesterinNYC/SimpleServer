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
			if(server.getBaseWaiting()){
				st = new ServerThread(server, newSocket, ServerThreadType.CLIENT);
			}
			else{				
				st = new ServerThread(server, newSocket, ServerThreadType.BROADCAST);
    			server.logBroadcastThread();
			}
			
			st.start();
		}
	}
	
    /**
    * run
    * <p>
    * Starts the thread and performs operations based on whether this thread is
    * a broadcast or client thread. 
    * If client, checks if the client's IP is blocked, and if not, initiates login
    * and server options.
    * If broadcast, perform broadcast operations.
    */
	public void run(){
		//Check if client's IP is currently blocked
		if(server.isBlocked(clientSocket.getInetAddress())){
			inputToClient("ip blocked");
		}
		//Client IP not blocked
		else{		
			//Is a client server thread
			if (this.threadType == ServerThreadType.CLIENT){
				inputToClient("ip not blocked");
				inputToClient(Integer.toString(server.getNextClientID()));
				try{
					login();
				}
				catch(IOException e){
					server.printLog("Error occurred with login for client with IP "
				                     + clientSocket.getInetAddress().toString());
				}
			}
			//Is a broadcast server thread
			if (this.threadType == ServerThreadType.BROADCAST){
				broadcastOperate();
			}
		}
	}	
	
    /**
    * broadcastOperate
    * <p>
    * Periodically send broadcast info to client broadcast threads and receive
    * acknowledgment messages from client.
    */
	private void broadcastOperate(){
		//Set the base server thread back to creating server client threads
		server.setBaseWaiting(true);
		String broadcastMessage = "";
		String clientResponse = "";
		//Continually send broadcasts to client broadcast thread and
		//receive acknowledgment messages
		while(true){
			broadcastMessage = server.getBroadcast();
			inputToClient(broadcastMessage);
			clientResponse = outputFromClient();
			//Client has received message
			if(clientResponse.equals("ack")){
				server.logBroadcastReceived();
				try {
					sleep(500);//Reduce server load
				} catch (InterruptedException e) {
					server.printLog("Interruption occurred on broadcast thread " +
							        "for client with IP " + 
							        clientSocket.getInetAddress().toString());
				}
			}
			//Client is to logout
			else if(clientResponse.equals("logout")){
				//End thread
				return;
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
    	//Get a correct command from client
    	while (!correctCommand){ 	
    		//Present client with options
    		inputToClient("These are your options:");
    		inputToClient("- Enter 'whoelse' to see what other users are connected on this server.");
    		inputToClient("- Enter 'wholasthr' to see what users have connected within the last hour.");
    		inputToClient("- Enter 'broadcast' to broadcast a one-line message to other logged-in clients.");
    		inputToClient("- Enter 'messages' to view the messages users have sent to this account.");
    		inputToClient("- Enter 'send' to send a message to an account.");
    		inputToClient("- Enter 'file' to receive a file via simulated TCP from the server.");
    		inputToClient("- Enter 'logout' to logout from this account.");
    		inputToClient("What would you like to do?");
    		//Process client command
    		choice = outputFromClient();
    		if(choice.trim().equals("whoelse") || choice.trim().equals("wholasthr") 
    		   || choice.trim().equals("broadcast") || choice.trim().equals("messages") 
    		   || choice.trim().equals("send") || choice.trim().equals("file") 
    		   || choice.trim().equals("logout")){
    			correctCommand = true;
    			inputToClient("success");
    			//No String switching in < Java 1.7...
    			if(choice.trim().equals("whoelse")){
    				whoelse();
    			}
    			else if(choice.trim().equals("wholasthr")){
    				wholasthr();
    			}
    			else if(choice.trim().equals("broadcast")){
    				broadcast();
    			}
    			else if(choice.trim().equals("messages")){
    				messages();
    			}
    			else if(choice.trim().equals("send")){
    				send();
    			}
    			else if(choice.trim().equals("file")){
    				waitTCPFinish();
    			}
    			else if(choice.trim().equals("logout")){
    				logout();
    				return;
    			}
    		}
    		else{
    			inputToClient("failure");
    			inputToClient("Please enter in a correct command.");
    		}
    	}
    	optionMenu();
    }
    
    //Client Options
    /**
    * broadcast
    * <p>
    * Send a broadcast to all currently logged on clients.
    */
    public void broadcast(){
    	inputToClient("Please enter the message you wish to broadcast to all users (one line only please).");
    	server.broadcast(outputFromClient(), userName);
    	inputToClient("Your message was broadcasted.");
    }

    /**
    * messages
    * <p>
    * Let client view the messages sent to the account that he or she is logged
    * in on.
    */
    public void messages(){
    	ArrayList<Message> accountMessages = server.getAccount(userName).getMessages();
    	inputToClient(Integer.toString(accountMessages.size()));
    	//Present all account messages
    	inputToClient("Here are the messages for " + this.userName + ":");
    	for(Message message : accountMessages){
    		inputToClient("Msg ID: " + message.getID());
    		inputToClient("From: " + message.getSender().getUserName());
    		inputToClient("   Subject: " + message.getSubject());
    	}
    	//Get the message ID of the message the client wishes to view
    	inputToClient("Please enter in the ID of the message you wish to view.");
    	inputToClient("Or enter 'menu' to return to the menu.");
    	String clientResponse = outputFromClient();
    	if(clientResponse.equals("menu")){
    		return;
    	}
    	
    	int messageID = Integer.parseInt(clientResponse);
    	while(!server.isValidMessageOfAccount(messageID, userName)){//while message ID is invalid
    		inputToClient("failure");
    		inputToClient("There is no message with that ID saved to this account.");
    		inputToClient("Please enter in the ID of the message you wish to view.");
        	inputToClient("Or enter 'menu' to return to the menu.");
        	clientResponse = outputFromClient();
        	if(clientResponse.equals("menu")){
        		return;
        	}
        	messageID = Integer.parseInt(clientResponse);
    	}
    	inputToClient("success");
    	//Present message to client
    	Message queryMessage = server.getMessage(messageID, userName);
		inputToClient("Msg ID: " + queryMessage.getID());
		inputToClient("  From: " + queryMessage.getSender().getUserName());
		inputToClient("   Subject: " + queryMessage.getSubject());
		inputToClient("   Body: " + queryMessage.getBody());
    }
    
    /**
    * send
    * <p>
    * Let client send a message from the account that he or she is logged in on
    * to another account
    */
    public void send(){
    	//Get recipient account username
    	inputToClient("Please enter in the username of the account you wish to send a message to.");
    	String recipient = outputFromClient();
    	while(!server.isValidAccount(recipient)){
    		inputToClient("failure");
    		inputToClient("Please enter the username of an account on this server."); 
    		recipient = outputFromClient();
    	}
    	inputToClient("success");
    	//Get message subject and content body
    	inputToClient("Please enter the subject line.");
    	String subject = outputFromClient();
    	inputToClient("Please enter the message body (one line only as of Simple Server version 1.0).");
        String body = outputFromClient(); 
        //Send message
        server.processNewMessage(new Message(server.getAccount(this.userName), 
				 				 server.getAccount(recipient), subject, body));
        inputToClient("Your message has been sent!");
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
    }  
    
    /**
    * wholasthr
    * <p>
    * Sends what accounts have been logged into on the server in the last hour.
    */
    public void wholasthr(){
    	ArrayList<String> users = server.getUsersLastHr();
    	inputToClient(Integer.toString(users.size()));
    	for (String user : users){
    		inputToClient(user);   
    	}
    }
    
    /**
    * logout
    * <p>
    * Logs the client out.
    */ 
    public void logout(){
    	server.logout(this);
    	server.broadcastLogout();
    	server.printLog("Logout Successful. User " + this.userName + " logged out");
    	inputToClient("You are now logged out from SimpleServer and the account under " + this.userName);
    	inputToClient("Have a nice day!");
    	//Close all IO and socket connection
    	serverToClient.close();
    	clientToServer.close();
    	try {
			clientSocket.close();
		} catch (IOException e) {
			server.printLog("Error occurred in disconnecting from client with IP "
							+ clientSocket.getInetAddress().toString());
		}
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
    			//Broadcaster Thread
    			server.setBaseWaiting(false);
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
    
    
    public void waitTCPFinish(){
    	//tcpFileSend();
    }
    
}
