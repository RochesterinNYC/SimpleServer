import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;


public class Server {
	public static void main(String[] args) throws IOException{
		Scanner passwordParse = new Scanner(new FileReader("passwords.txt"));
		Scanner input = new Scanner(System.in);
		String userName = "";
		String password = "";
		Account newAcc;
	    ArrayList<Account> accounts = new ArrayList<Account>();
		while (passwordParse.hasNext()) {
			userName = passwordParse.next();
			password = passwordParse.next(); 
			newAcc = new Account(userName, password);
			accounts.add(newAcc);
		}
		for(Account acc : accounts){
			//System.out.println(acc);
		}
		int serverPort = 0;
		boolean successfulPort = false;
		do {
			System.out.println("Which port would you like the server to listen in on?");
			String desiredPort = input.nextLine();
			try{
				serverPort = Integer.parseInt(desiredPort.trim());
				successfulPort = true;
			}
			catch(NumberFormatException e) { 
			        successfulPort = false;
			}
		} while (!successfulPort);
		
		ServerSocket serverSocket = null;
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
        
        while(true){
        	
        }
        
        
	}

}
