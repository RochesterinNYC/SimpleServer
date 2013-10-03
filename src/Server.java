import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server extends Thread {
	
	private ArrayList<Account> accounts;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter serverOutput;
	private Scanner clientInput;
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
	    System.out.println("Server is up and listening on port " + serverPort);
	    run();
	}
	public Server (ArrayList<Account>accounts, ServerSocket serverSocket){
		this.accounts = accounts;
		this.serverSocket = serverSocket;
	}
    public void run(){   
        while(true){
        	try {
        		clientSocket = serverSocket.accept();
        		Server s = new Server(accounts, serverSocket);
        		Thread t = new Thread(s);
        		t.start();
        		System.out.println(this.toString());
        		serverOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        		clientInput = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
        		serverOutput.println("Hi! Welcome to SimpleServer!");
        		serverOutput.println("Please enter your login info");
        		serverOutput.println("Username: ");
        		String userNameAttempt = clientInput.nextLine();
        		serverOutput.println("Password: ");
        		String passwordAttempt = clientInput.nextLine();
        		if(loginCorrect(userNameAttempt, passwordAttempt, accounts)){
        			serverOutput.println("Hi! You are now logged into SimpleServer as " + userNameAttempt);
        		}
        		optionMenu();
        	}
        	catch (IOException e){
        		System.exit(1);
        	}
        }
	}
	
    private void optionMenu(){
    	String choice = "";
    	boolean correctCommand = false;
    	while (!correctCommand){
    		serverOutput.println("These are your options:");
    		serverOutput.println("- Enter 'whoelse' to see what other users are connected on this server.");
    		serverOutput.println("- Enter 'wholasthr' to see who else is on this server.");
    		serverOutput.println("- Enter 'broadcast' to broadcast a message to all connected users.");
    		serverOutput.println("What would you like to do?");
    		choice = clientInput.nextLine();
    		if(choice.trim().equals("whoelse") || choice.trim().equals("wholasthr") || choice.trim().equals("broadcast")){
    			correctCommand = true;
    		}
    		else{
    			serverOutput.println("Please enter in a correct command.");
    		}
    	}
    	serverOutput.println("You chose " + choice);
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
	
	private boolean loginCorrect(String userName, String password, ArrayList<Account> list){
		boolean loginCorrect = false;
		for(Account acc : list){
			if(acc.login(userName, password)){
				loginCorrect = true;
			}
		}
		return loginCorrect;
	}

}
