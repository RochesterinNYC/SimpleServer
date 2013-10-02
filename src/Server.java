import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		Scanner passwordParse = new Scanner(new FileReader("passwords.txt"));
		String line = "";
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
			System.out.println(acc);
		}

	}

}
