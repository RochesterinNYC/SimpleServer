import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Server {
	
	private ArrayList<Account> accounts;
	private ServerSocket serverSocket;
	private HashMap<String, ServerThread> currentClients;
	
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
		currentClients = new HashMap<String, ServerThread>();		
	    System.out.println("Server is up and listening on port " + serverPort);
	}
	
	public void addToMap(String userName, ServerThread newClient){
		currentClients.put(userName, newClient);
	}
	public static void printMap(Map mp) {
	    Iterator it = mp.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}
	public String[] getCurrentUsers(){
		String[] currentUsers = new String[currentClients.size()];
		Iterator iterate = currentClients.entrySet().iterator();
		int arrIndex = 0;
		while(iterate.hasNext()){
			Map.Entry pairs = (Map.Entry)iterate.next();
			currentUsers[arrIndex] = (String)pairs.getKey();
			arrIndex++;
			iterate.remove();
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
