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
        		Socket socket = serverSocket.accept();
        		Server s = new Server(accounts, serverSocket);
        		Thread t = new Thread(s);
        		t.start();
        		System.out.println(this.toString());
        		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		Scanner clientInput = new Scanner(new InputStreamReader(socket.getInputStream()));
        		out.println("Hi! Welcome to SimpleServer!");
        		out.println("Please enter your login info");
        		out.println("Username: ");
        		String userNameAttempt = clientInput.nextLine();
        		out.println("Password: ");
        		String passwordAttempt = clientInput.nextLine();
        		if(loginCorrect(userNameAttempt, passwordAttempt, accounts)){
        			out.println("Hi! You are now logged in as " + userNameAttempt);
        		}
        	}
        	catch (IOException e){
        		System.exit(1);
        	}
        }
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
