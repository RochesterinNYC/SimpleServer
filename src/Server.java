import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	
	private ArrayList<Account> accounts;
	private ServerSocket serverSocket;
	private ArrayList<ServerThread> currentClients;
	private boolean baseWaiting;
	private ArrayList<ServerThread> broadcastThreads;
	private String broadcast;
	
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
		baseWaiting = true;
	    System.out.println("Server is up and listening on port " + serverPort);
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
	
	public boolean loginCorrect(String userName, String password){
		System.out.println("Attempted login with username " + userName + " and password " + password);
		boolean loginCorrect = false;
		for(Account acc : accounts){
			if(acc.login(userName, password)){
				loginCorrect = true;
			}
		}
		return loginCorrect;
	}

}
