import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;

/**
* <b>Server Class</b>
* <p>
* Represents the server that interacts with clients and keeps track of messages,
* connected clients, blocked IPs, and user accounts
* @author James Wen - jrw2175
*/
public class Server {
	private ServerSocket serverSocket;
	private ArrayList<Account> accounts;
	private ArrayList<ServerThread> currentClients;
	private ArrayList<BlockedIP> blockedIPs;   
	private ArrayList<Message> masterMessageList;
	private PrintWriter logger;
	//if baseWaiting is true, then base server thread is currently waiting 
	//to create a client thread (false --> broadcast thread)
	private boolean baseWaiting;
	private static int clientID = 1;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> unblockerHandle;	
	//IP Block Related
	private long blocktime = 60000;
	private int numLoginAttempts = 3;
	//Broadcast Protocol Related
	private String broadcast;
	private int broadcastThreadCount;
	private int numBroadcastReceived;	
	
    /**
    * Server constructor
    * <p>
    * Creates the server, connects it to a port, and sets up the server logger,
    * user accounts, and periodic task for unblocking IPs.
    * @param serverPort - the port at the server's IP address to connect to 
    * @throws IOException
    */
	public Server(int serverPort) throws IOException{
		setupLogin();
		try {
			serverSocket = new ServerSocket(serverPort);
	    }
	    catch (IOException e) {
	        System.err.println("Can not listen on port: " + serverPort);
	        System.exit(1);
	    }
	    catch (IllegalArgumentException e) {
	        System.err.println("Can not listen on port: " + serverPort);
	        System.exit(1);
	    }
		currentClients = new ArrayList<ServerThread>();
		blockedIPs = new ArrayList<BlockedIP>();
		masterMessageList = new ArrayList<Message>();
		baseWaiting = true;
		broadcast = "";
		broadcastThreadCount = 0;
		numBroadcastReceived = 0;
		logger = new PrintWriter(new BufferedWriter(new FileWriter("server_log.txt", true)));
	    printLog("Server is up and listening on port " + serverPort);
	    setUpUnblocker();
	}
	
    /**
    * setupLogin
    * <p>
    * Sets up the server accounts by reading in data from the accounts.txt
    * file.
    * @throws FileNotFoundException
    */
	private void setupLogin() throws FileNotFoundException{
		Scanner passwordParse = new Scanner(new FileReader("accounts.txt"));
		String userName = "";
		String password = "";
		Account newAcc;
	    accounts = new ArrayList<Account>();
		while (passwordParse.hasNext()) {
			userName = passwordParse.next();
			password = passwordParse.next();
			newAcc = new Account(userName, password);
			accounts.add(newAcc);
		}
	}
	
	//IP Blocking-related Methods
    /**
    * setUpUnblocker
    * <p>
    * Creates the scheduler that periodically (every 1 second) initiates the 
    * unblocker task that unblocks any IPs that have been blocked for longer
    * than the server blocktime (at the time that the IP was originally blocked,
    * not current server blocktime).
    */
	public void setUpUnblocker(){
	    scheduler = Executors.newScheduledThreadPool(1);
	    unblockerHandle = scheduler.scheduleAtFixedRate(new Unblocker(this, logger), 1, 1, SECONDS);
	}

    /**
    * isBlocked
    * <p>
    * Returns whether the inputed ip is blocked by the server or not.
    * @param ip - IP address to check
    * @return isBlocked - whether that IP is blocked
    */
	public boolean isBlocked(InetAddress ip){
		boolean isBlocked = false;
		for(BlockedIP client : blockedIPs){
			if(ip.toString().equals(client.getIP().toString())){
				isBlocked = true;
			}
		}
		return isBlocked;
	}
	
    /**
    * blockIP
    * <p>
    * Blocks the inputed IP for an amount of time specified by the current
    * server blocktime.
    * @param ip - IP Address to block
    */
	public void blockIP(InetAddress ip){
		blockedIPs.add(new BlockedIP(ip, blocktime));
		printLog("IP Address " + ip.toString() + " is now blocked for " + viewBlocktime() + " seconds");
	}
	
    /**
    * removeBlockedIP
    * <p>
    * Unblocks the inputed IP
    * @param ip - IP Address to unblock
    */
	public void removeBlockedIP(BlockedIP ip){
		blockedIPs.remove(ip);
	}
	
	//Client Operations-related methods
	
	//Broadcast-related methods
    /**
    * getBaseWaiting
    * <p>
    * Returns whether the server/base server thread is waiting to create a 
    * client server thread (or a broadcast server thread if false)
    * @return baseWaiting - whether to create client server thread
    */
	public boolean getBaseWaiting(){
		return baseWaiting;
	}
    /**
    * setBaseWaiting
    * <p>
    * Sets whether the server/base server thread is waiting to create a 
    * client server thread (or a broadcast server thread if false)
    * @param isWaiting - whether server is waiting to create client server thread
    */
	public void setBaseWaiting(boolean isWaiting){
		baseWaiting = isWaiting;
	}
    /**
    * getBroadcast
    * <p>
    * Gets the current broadcast info that the server wants to send
    * @return broadcast - current broadcast info
    */
	public String getBroadcast(){
		return broadcast;
	}
    /**
    * logBroadcastThread
    * <p>
    * Logs another client broadcast thread that's connected to one of this server's
    * broadcast threads
    */
	public void logBroadcastThread(){
		this.broadcastThreadCount++;
	}
    /**
    * broadcastLogOut
    * <p>
    * Logs that a client broadcast thread that's connected to one of this server's
    * broadcast threads has logged out
    */
	public void broadcastLogout(){
		this.broadcastThreadCount--;
	}
    /**
    * broadcast
    * <p>
    * Sets the broadcast info and reinitializes the counter for how many client
    * broadcast threads have received and acknowledged the broadcast
    * @param broadcast - the content of the broadcast
    * @param userName - username of account that requested the broadcast to be sent
    */
	public void broadcast(String broadcast, String userName){
		this.broadcast = broadcast;
		printLog(userName + " made a broadcast: " + broadcast);
		this.numBroadcastReceived = 0;
	}
    /**
    * logBroadcastReceived
    * <p>
    * Logs that a broadcast was received and acknowledged by a client broadcast
    * thread.
    * Reinitializes the server broadcast info and status when all client broadcast
    * threads have received and acknowledged the message (when # acknowledgments 
    * is equal to number of connected client broadcast threads).
    */
	public void logBroadcastReceived(){
		if(++numBroadcastReceived == broadcastThreadCount){
			broadcast = "";
			numBroadcastReceived = 0;
		}
	}
	
	//Messages-related methods
    /**
    * isValidAccount
    * <p>
    * Returns whether an account username is valid (belongs to an account on
    * the server).
    * @param accountName - username to check if valid
    * @return isValid - whether the requested accoutName is valid
    */
	public boolean isValidAccount(String accountName){
		boolean isValid = false;
		for(Account account : accounts){
			if(account.getUserName().equals(accountName)){
				isValid = true;
			}
		}
		return isValid;
	}
    /**
    * isValidMessageOfAccount
    * <p>
    * Checks whether the message with the ID id was sent to and belongs to the 
    * account with the username userName.
    * Required to prevent client from accessing messages that were sent to
    * other accounts.
    * @param id - ID of message
    * @param userName - username of account to check
    * @return isValid - whether the message with that id belongs to account
    */
	public boolean isValidMessageOfAccount(int id, String userName){
		boolean isValid = false;
		Account account = getAccount(userName);
		for(Message message : account.getMessages()){
			if(message.getID() == (id)){
				isValid = true;
			}
		}
		return isValid;
	}
    /**
    * getAccount
    * <p>
    * Returns the account that has the queried username.
    * @param accountName - the requested username
    * @return queryAccount - the account that has the username
    */
	public Account getAccount(String accountName){
		Account queryAccount = null;
		for(Account account : accounts){
			if(account.getUserName().equals(accountName)){
				queryAccount = account;
			}
		}
		return queryAccount;
	}
    /**
    * getMessage
    * <p>
    * Returns the message of that id that the user received.
    * @param id - the id of the message
    * @param userName - username of account that message was sent to
    * @return queryMessage - the requested message
    */
	public Message getMessage(int id, String userName){
		Message queryMessage = null;
		Account account = getAccount(userName);
		for(Message message : account.getMessages()){
			if(message.getID() == (id)){
				queryMessage = message;
			}
		}
		return queryMessage;
	}
    /**
    * processNewMessage
    * <p>
    * Adds the newly sent message to the server's master list of all messages sent
    * and adds the message to the account that it was sent to.
    * Message sending is logged in server log.
    * @param message - the message that a client just sent
    */
	public void processNewMessage(Message message){
		masterMessageList.add(message);
		Account account = message.getRecipient();
		account.newMessage(message);
		printLog(message.getLogMessage());
	}
	
    /**
    * addToClients
    * <p>
    * Add ServerThread that is handling a newly connected client.
    * Keeps track of the current clients essentially.
    * @param newClient - ServerThread handling new client
    */
	public void addToClients(ServerThread newClient){
		currentClients.add(newClient);
	}
	/**
    * getCurrentUsers
    * <p>
    * Returns the usernames the accounts that current user clients are logged in
    * with.
    * @return currentUsers - usernames
    */
	public String[] getCurrentUsers(){
		String[] currentUsers = new String[currentClients.size()];
		int arrIndex = 0;
		for (ServerThread client : currentClients){
			currentUsers[arrIndex] = client.getUserName();
			arrIndex++;
		}
		return currentUsers;
	}
	
    /**
    * getUsersLastHr
    * <p>
    * Returns the usernames of the accounts that clients have logged in with 
    * in the last hour.
    * @param currentUsers - usernames
    */
	public ArrayList<String> getUsersLastHr(){
		ArrayList<String> currentUsers = new ArrayList<String>();
		for (Account account : accounts){
			if (System.currentTimeMillis() - account.getLastLoginTime() < 3600000){
				if(account.isLoggedIn()){
					currentUsers.add(account.getUserName() + " - active");
				}
				else{
					currentUsers.add(account.getUserName());
				}
			}
		}
		return currentUsers;
	}
	
    /**
    * logout
    * <p>
    * Logs a client out of the server and marks the account they were logged into
    * as active or inactive depending on whether there was another client
    * logged in using that account.
    * @param client - the client to be logged out
    */
	public void logout(ServerThread client){
		currentClients.remove(client);
		boolean userNameStillUsed = false;
		for(ServerThread thread : currentClients){
			if (thread.getUserName().equals(client.getUserName())){
				userNameStillUsed = true;
			}
		}
		//Client was only one using the username
		if(!userNameStillUsed){
			//Mark that account as not logged in anymore
			for(Account account : accounts){
				if (account.getUserName().equals(client.getUserName())){
					account.markLoginStatus(false);
				}
			}
		}
	}
	
    /**
    * loginCorrect
    * <p>
    * Returns whether a login attempt with a specified username and password
    * is correct.
    * @param userName - login attempted username
    * @param password - login attempted password
    * @return loginCorrect - whether login attempt is correct
    */
	public boolean loginCorrect(String userName, String password){
		printLog("Attempted login with username " + userName + " and password " + password);
		boolean loginCorrect = false;
		for(Account acc : accounts){
			if(acc.login(userName, password)){
				loginCorrect = true;
				acc.markLoginStatus(true);
			}
		}
		return loginCorrect;
	}

    /**
    * getServerSocket
    * <p>
    * Returns the primary socket that the server is connected to on its own IP.
    * @return serverSocket - the server socket
    */
    public ServerSocket getServerSocket(){
    	return serverSocket;
    }
    /**
     * getNextClientID
     * <p>
     * Returns the next client ID to be assigned to a client that's just logged
     * onto the server.
     * @return next client ID
     */
	public int getNextClientID(){
		return clientID++;
	}
	
    //Mostly console related operations
    /**
    * getBlocktime
    * <p>
    * Returns how long newly blocked blockedIPs are to be blocked for
    * @return blocktime - server's current blocktime
    */	
	public long getBlocktime(){
		return blocktime;
	}
    /**
    * changeBlocktime
    * <p>
    * Change how long newly blocked blockedIPs are to be blocked for
    * @param blocktime - server's new blocktime
    */	
	public void changeBlockTime(int seconds){
		this.blocktime = (long) seconds * 1000;
	}
    /**
    * getNumLoginAttempts
    * <p>
    * Returns how many failed login attempts are allowed before an IP is blocked
    * @return numLoginAttempts - server's allowed # login attempts
    */	
	public int getNumLoginAttempts(){
		return numLoginAttempts;
	}
    /**
    * changeNumLoginAttempts
    * <p>
    * Change how many failed login attempts are allowed before an IP is blocked
    * @param numLoginAttempts - new number of allowed failed login attempts
    */	
	public void changeNumLoginAttempts(int numLoginAttempts){
		this.numLoginAttempts = numLoginAttempts;
	}	
    /**
    * removeAllBlockedIPs
    * <p>
    * Unblocks all currently blocked IPs
    */	
	public void removeAllBlockedIPs(){
		blockedIPs = new ArrayList<BlockedIP>();
	}
    /**
    * getAllMessages
    * <p>
    * Returns all messages that have been sent between users on the server.
    * @return masterMessageList - all messages
    */
	public ArrayList<Message> getAllMessages(){
		return masterMessageList;
	}
    /**
    * getAccounts
    * <p>
    * Returns a list all the server accounts
    * @return accounts - the server accounts
    */	
	public ArrayList<Account> getAccounts(){
		return accounts;
	}	
    /**
    * getCurrentClients
    * <p>
    * Returns a list all the currently connected clients
    * @return currentClients - currently connected clients
    */	
	public ArrayList<ServerThread>getCurrentClients(){
		return currentClients;
	}
    /**
    * viewBlocktime
    * <p>
    * Returns the current server block time as a integer (how many seconds)
    * @return blocktime in seconds
    */	
	public int viewBlocktime(){
		return (int) blocktime/1000;
	}
    /**
    * getBlockedIPs
    * <p>
    * Returns the currently blocked IP Addresses
    * @return currently blocked IP Addresses
    */	
	public ArrayList<BlockedIP> getBlockedIPs(){
		return blockedIPs;
	}
    /**
    * changeNumLoginAttempts
    * <p>
    * Change how many failed login attempts are allowed before an IP is blocked
    * @param numLoginAttempts - new number of allowed failed login attempts
    */
	
	//Logger Operations
    /**
    * printLog
    * <p>
    * Prints to the server log.
    * @param logMessage - message to be logged
    */	
	public void printLog(String logMessage){
		logger.println("[ " + System.currentTimeMillis() + " ] - " + logMessage);
		logger.flush();
	}
    /**
    * closeLogger
    * <p>
    * Closes the server logger. (Necessary for server logger to keep printing
    * to same server log file in different server sessions).
    */	
	public void closeLogger(){
		logger.close();
	}

    public void tcpFileSend(){
    	
    }
}
