import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
* <b>Client Class</b>
* <p>
* Represents the client that interacts with the server.
* @author James Wen - jrw2175
*/
public class Client {
    private Scanner localInput;
    private PrintWriter clientToServer;
    private Scanner serverToClient;
    private Socket conn;
    private String host;
    private boolean toLogOut;
    private int clientPort;
    private int id;
    
    /**
    * Client constructor
    * <p>
    * Creates a client and connects to the server.
    * @param host - the IP address of the server
    * @param portNumber - the port at the server's IP address to connect to 
    * @throws IOException
    */
    public Client(String host, int clientPort) throws IOException{
    	conn = new Socket(host, clientPort);
		localInput = new Scanner(System.in);
		clientToServer = new PrintWriter(conn.getOutputStream(), true);
		serverToClient = new Scanner(new InputStreamReader(conn.getInputStream()));
		this.host = host;
		this.clientPort = clientPort;
		toLogOut = false;
    }
    
    /**
     * toLogOut
     * <p>
     * Returns whether this client is in the process of logging out
     * This is essential in communicating with the parallel client thread that
     * is listening for and reporting server broadcasts. Once this client is 
     * set to log out, then this method will tell the parallel broadcast thread
     * to end as well.
     * @return toLogOut - whether this client should log out
     */
    public boolean toLogOut(){
    	return toLogOut;
    }
    /**
     * setLogOut
     * <p>
     * Set the client to log out or not.
     * @param toLogOut - whether this client should log out
     */
    private void setLogOut(boolean toLogOut){
    	this.toLogOut = toLogOut;
    }

    /**
    * checkIPBlocked
    * <p>
    * Returns whether the IP Address that the client is on is blocked
    * @return IPBlocked - whether this IP is blocked
    */
    public boolean checkIPBlocked(){
    	String blockResponse = outputFromServer();
    	boolean IPBlocked = false;
    	if(blockResponse.equals("ip blocked")){
    		IPBlocked = true;
    	}
    	else if(blockResponse.equals("ip not blocked")){
    		IPBlocked = false;
    		setClientID(Integer.parseInt(outputFromServer()));
    	}
    	return IPBlocked;
    }
	
    /**
    * login
    * <p>
    * Attempts to login to the server and returns whether the client is
    * successful and closes the client if its IP has become blocked from too
    * many failed login attempts.
    * @return loginSuccessful - whether the login was ultimately successful
    */
	public boolean login(){
		boolean loginSuccessful = false;
		boolean loginBlocked = false;
		//While login has not been successful and IP is not blocked yet
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
	
    /**
    * optionMenu
    * <p>
    * Manages the client interaction with the server options that the client 
    * can undergo.
    */
	public void optionMenu(){
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
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
			else if (command.equals("broadcast")){
				broadcast();
			}
			else if (command.equals("messages")){
				messages();
			}
			else if (command.equals("send")){
				send();
			}
			else if (command.equals("file")){
				file();
			}
			else if (command.equals("logout")){
				logout();
				return;
			}
			
		}
		else if (serverResponse.equals("failure")){
			System.out.println(outputFromServer());
		}
		optionMenu();
	}
	
    /**
    * whoelse
    * <p>
    * See what other clients are logged onto the server. 
    * (Will return this client's username if there is another client on the
    * server who is also using the account).
    */
    public void whoelse(){
    	int numUsers = Integer.parseInt(outputFromServer());
    	for(int i = 0; i < numUsers; i++){
    		System.out.println(outputFromServer());
    	}
    }
    /**
    * wholasthr
    * <p>
    * See what accounts have been logged into on the server in the last hour.
    */
    public void wholasthr(){
    	int numUsers = Integer.parseInt(outputFromServer());
    	for(int i = 0; i < numUsers; i++){
    		System.out.println(outputFromServer());
    	}
    }
    /**
     * broadcast
     * <p>
     * Sends info to the server telling it to broadcast a message to all currently
     * logged on clients.
     */
    public void broadcast(){     
    	System.out.println(outputFromServer());
    	inputToServer(localInput.nextLine());
    	System.out.println(outputFromServer());
    }
    /**
     * messages
     * <p>
     * Allows the client to check the messages that have been sent to the account
     * that it is logged on to the server with.
     */
    public void messages(){
    	int numMessages = Integer.parseInt(outputFromServer());
    	System.out.println(outputFromServer());
    	//Show the messages that this account has received
    	for(int i = 0; i < numMessages; i++){
    		System.out.println(outputFromServer());
    		System.out.println(outputFromServer());
    		System.out.println(outputFromServer());
    	}
    	//Get a valid message ID from client or return to menu
    	boolean validID = false;
    	String serverResponse = "";
    	System.out.println(outputFromServer());
    	System.out.println(outputFromServer());
    	String messageID = localInput.nextLine();
    	if (messageID.equals("menu")){
    		inputToServer(messageID);
    		return;
    	}
    	while(!isValidIDInteger(messageID)){
    		System.out.println("Please enter in a valid ID.");
    		messageID = localInput.nextLine();
    	}
    	while(!validID){
    		inputToServer(messageID);
    		if (messageID.equals("menu")){
        		return;
        	}
    		serverResponse = outputFromServer();
    		if(serverResponse.equals("success")){
    			validID = true;
    		}
    		else if(serverResponse.equals("failure")){
    			System.out.println(outputFromServer());
            	System.out.println(outputFromServer());
            	System.out.println(outputFromServer());
            	messageID = localInput.nextLine();
    		}
    	}
    	//Print message with body content
    	if(serverResponse.equals("success")){
    		System.out.println(outputFromServer());
        	System.out.println(outputFromServer());
        	System.out.println(outputFromServer());
        	System.out.println(outputFromServer());
    	}
    }
    
    /**
     * isValidIDInteger
     * <p>
     * Small integer checking method for checking user input to see if it could
     * match a message
     * @param - s - the string version of the id to be checked
     * @return whether the id could be a valid message ID
     */
    public static boolean isValidIDInteger(String s) {
        boolean isInteger = true;
    	try { 
            int i = Integer.parseInt(s);
            if(i <= 0){
            	isInteger = false;
            }
        } catch(NumberFormatException e) { 
            isInteger = false; 
        }
        return isInteger;
    }
    /**
     * send
     * <p>
     * Allows the client to send a message  with a one line subject and body to
     * another account (has to be a valid accout on the server).
     */
    public void send(){
    	boolean isValidAccount = false;
    	String serverResponse;
    	while(!isValidAccount){
        	System.out.println(outputFromServer());
    		String recipientUsername = localInput.nextLine();
        	inputToServer(recipientUsername);
        	serverResponse = outputFromServer();
        	if(serverResponse.equals("success")){
        		isValidAccount = true;
        	}
    	}
    	System.out.println(outputFromServer());
    	inputToServer(localInput.nextLine());
    	System.out.println(outputFromServer());
    	inputToServer(localInput.nextLine());
    	System.out.println(outputFromServer());
    }
    
    /**
    * logout
    * <p>
    * Logout of the server. (Also ends the concurrently running client thread 
    * that has been listening for serve broadcasts).
    */
	public void logout(){
		setLogOut(true);
		System.out.println(outputFromServer());
		System.out.println(outputFromServer());
		localInput.close();
    	serverToClient.close();
    	clientToServer.close();
    	try {
			conn.close();
		} catch (IOException e) {
			System.out.println("Error occurred in disconnecting from server.");
		}
	}
    
    /**
    * getConn
    * <p>
    * Get the socket representing the connection between this client and the
    * server.
    * @return conn - the connection
    */
    public Socket getConn(){
    	return conn;
    }
    /**
    * getPortNumber
    * <p>
    * Get the port number that this client is connected to.
    * @return portNumber - the port number
    */
    public int getPortNumber(){
    	return this.clientPort;
    }
    /**
    * getHost
    * <p>
    * Get the server IP that this client is connected to.
    * @return host - the server IP
    */
    public String getHost(){
    	return this.host;
    }
    /**
     * setClientID
     * <p>
     * Sets the clientID of this client. (No two clients will have same ID.)
     * @param clientID - ID
     */
     private void setClientID(int clientID){
     	this.id = clientID;
     }
     
     /**
     * setClientID
     * <p>
     * Sets the clientID of this client. (No two clients will have same ID.)
     * @param clientID - ID
     */
     public int getClientID(){
     	return id;
     }
    /**
    * inputToServer
    * <p>
    * Input a query or response to the server.
    * @param input - what is to be sent to the server
    */
	public void inputToServer(String input){
		clientToServer.println(input);
	}
    /**
    * outputFromServer
    * <p>
    * Read the server's response
    * @return the server's response
    */
    public String outputFromServer(){
    	return serverToClient.nextLine();
	}
    
    
    public void file(){
    	boolean receiverCommandValid = false;
		String[] arguments;
    	do{
    		System.out.println("Please enter in the proper command for initiating " +
    						   "the file transfer through tcp simulation. The format is as follows:");
    		System.out.println("receiver [file name] [listening port] [remote IP] [remote port] [log file name]");
    		System.out.println("Example:"); 
    		System.out.println("receiver file.txt 20000 128.59.15.37 20001 logfile.txt ");
    		String senderCommand = localInput.nextLine();
			arguments = senderCommand.split(" ");
			if (arguments.length == 6){
				receiverCommandValid = true;
			}
    	}while(!receiverCommandValid);
    	try {
			TCPReceiver receiver = new TCPReceiver(arguments[1], Integer.parseInt(arguments[2]), InetAddress.getByName(arguments[3]), Integer.parseInt(arguments[4]), arguments[5]);
			receiver.receive();
    	} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
        
}
