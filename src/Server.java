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
* <b>Client Class</b>
* <p>
* Represents the client that interacts with the server.
* @author James Wen - jrw2175
*/
public class Server {
	private ServerSocket serverSocket;
	private ArrayList<Account> accounts;
	private ArrayList<ServerThread> currentClients;
	private ArrayList<BlockedIP> blockedIPs;   
	private ArrayList<Message> masterMessageList;
	
	private PrintWriter logger;
	//if baseWaiting is true, then currently waiting to create a client thread
	private boolean baseWaiting;
	private String broadcast;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> unblockerHandle;	
	private long blocktime = 60000;
	private int numLoginAttempts = 3;
	private static int clientID = 1;
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
	
	public boolean getBaseWaiting(){
		return baseWaiting;
	}
	public void setBaseWaiting(boolean isWaiting){
		baseWaiting = isWaiting;
	}
	public void setBroadcast(String broadcast){
		this.broadcast = broadcast;
	}
	public String getBroadcast(){
		return broadcast;
	}
	public void logBroadcastThread(){
		this.broadcastThreadCount++;
	}
	public void broadcastLogout(){
		this.broadcastThreadCount--;
	}
	
	public void broadcast(String broadcast, String userName){
		this.broadcast = broadcast;
		printLog(userName + " made a broadcast: " + broadcast);
		this.numBroadcastReceived = 0;
	}
	public void logBroadcastReceived(){
		if(++numBroadcastReceived == broadcastThreadCount){
			broadcast = "";
			numBroadcastReceived = 0;
		}
	}

	public int getNextClientID(){
		return clientID++;
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
    * @param isBlocked - whether that IP is blocked
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
	
	
	public boolean isValidAccount(String accountName){
		boolean isValid = false;
		for(Account account : accounts){
			if(account.getUserName().equals(accountName)){
				isValid = true;
			}
		}
		return isValid;
	}
	//userName required to prevent client from accessing messages on other accounts
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
	public ArrayList<Message> getAllMessages(){
		return masterMessageList;
	}
	public Account getAccount(String accountName){
		Account queryAccount = null;
		for(Account account : accounts){
			if(account.getUserName().equals(accountName)){
				queryAccount = account;
			}
		}
		return queryAccount;
	}
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
}
