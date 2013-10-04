import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;
public class Server {
	
	private ArrayList<Account> accounts;
	private ServerSocket serverSocket;
	private ArrayList<ServerThread> currentClients;
	private boolean baseWaiting;
	private ArrayList<ServerThread> broadcastThreads;
	private String broadcast;
	private ArrayList<BlockedIP> blockedIPs;
	
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> unblockerHandle;	       
	
	
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
		baseWaiting = true;
	    System.out.println("Server is up and listening on port " + serverPort);
	    setUpUnblocker();
	}
	public void setUpUnblocker(){
	    scheduler = Executors.newScheduledThreadPool(1);
	    unblockerHandle = scheduler.scheduleAtFixedRate(new Unblocker(this), 1, 1, SECONDS);
	}
	
	public ArrayList<BlockedIP> getBlockedIPs(){
		return blockedIPs;
	}
	
	public void setBroadcast(String broadcast){
		this.broadcast = broadcast;
	}
	public String getBroadcast(){
		return this.broadcast;
	}
	public void broadcast(){
		synchronized(this){
    		this.notifyAll();
    	}
	}
	
	public void waitThread(ServerThread thread){
		synchronized(thread){
			try {
				thread.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isBlocked(InetAddress ip){
		boolean isBlocked = false;
		for(BlockedIP client : blockedIPs){
			if(ip.toString().equals(client.getIP().toString())){
				isBlocked = true;
			}
		}
		return isBlocked;
	}
	
	public void blockIP(InetAddress ip){
		blockedIPs.add(new BlockedIP(ip));
		System.out.println("IP Address " + ip.toString() + " is now blocked for 60 seconds");
	}
	
	public void removeBlockedIP(BlockedIP ip){
		blockedIPs.remove(ip);
	}
	
	public boolean getBaseWaiting(){
		return baseWaiting;
	}
	public void setBaseWaiting(boolean isWaiting){
		baseWaiting = isWaiting;
	}
	
	public void addToClients(ServerThread newClient){
		currentClients.add(newClient);
	}
	public String[] getCurrentUsers(){
		String[] currentUsers = new String[currentClients.size()];
		int arrIndex = 0;
		for (ServerThread client : currentClients){
			currentUsers[arrIndex] = client.getUserName();
			arrIndex++;
		}
		return currentUsers;
	}
	
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
    
    public ServerSocket getServerSocket(){
    	return serverSocket;
    }
    
	private void setupLogin() throws FileNotFoundException{
		Scanner passwordParse = new Scanner(new FileReader("passwords.txt"));
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
	
	public boolean loginCorrect(String userName, String password){
		System.out.println("Attempted login with username " + userName + " and password " + password);
		boolean loginCorrect = false;
		for(Account acc : accounts){
			if(acc.login(userName, password)){
				loginCorrect = true;
				acc.markLoginStatus(true);
			}
		}
		return loginCorrect;
	}
}
