import java.util.ArrayList;
import java.util.Scanner;


public class ConsoleThread extends Thread{
	private Server server;
	private String currentCommand;
	private Scanner consoleScanner;
	private String[] commandList;
	public ConsoleThread(Server server){
		this.server = server;
		//List of currently implemented commands
		this.commandList = new String[]{"help", "blocked_ips", 
			"unblock_all", "accounts", "view_blocktime","change_blocktime",
			"view_number_login_attempts", "change_number_login_attempts",
			"number_threads", "current_ips","last_hr", "version", "shut_logger"};
	}
	
	public void run(){
		consoleScanner = new Scanner(System.in);
		consolePrint("Welcome to SimpleServer v1.0! Enter 'help' for a list of commands.");
		consolePrint("Please also remember to shut the logger ('shut_logger') before you exit!");
		
		prompt();
	}
	
	private void prompt(){
		System.out.print("> ");
		currentCommand = consoleScanner.nextLine();
		if(!properCommand(currentCommand.trim())){
			System.out.println("Not a recognized command.");
		}
		else if(!completeCommand(currentCommand.trim())){
			System.out.println("That command cannot be completed at this time.");
		}
		prompt();
	}
	
	private boolean properCommand(String query){
		boolean commandComplete = false;
		for(String command : commandList){
			if(query.equals(command)){
				commandComplete = true;
			}
		}
		return commandComplete;		
	}
	private boolean completeCommand(String command){
		boolean commandComplete = true;
		try{
			if(command.equals("help")){
				help();
			}
			else if(command.equals("blocked_ips")){
				viewBlockedIPs();
			}
			else if(command.equals("unblock_all")){
				unblockAllIPs();
			}
			else if(command.equals("accounts")){
				viewAccounts();
			}
			else if(command.equals("view_blocktime")){
				viewBlockTime();
			}
			else if(command.equals("change_blocktime")){
				changeBlockTime();
			}
			else if(command.equals("view_number_login_attempts")){
				viewNumberLoginAttempts();
			}
			else if(command.equals("change_number_login_attempts")){
				changeNumberLoginAttempts();
			}
			else if(command.equals("number_threads")){
				numberThreads();
			}
			else if(command.equals("current_ips")){
				viewCurrentIPs();
			}
			else if(command.equals("last_hr")){
				viewLastHr();
			}
			else if(command.equals("version")){
				version();
			}
			else if(command.equals("shut_logger")){
				shutLogger();
			}
		}
		catch (Exception e){
			commandComplete = false;
		}
		return commandComplete;
	}
	private void help(){
		consolePrint("help");
		consolePrint("- View all commands and what they do.");
		consolePrint("blocked_ips");
		consolePrint("- View all currently blocked IPs.");
		consolePrint("unblock_all");
		consolePrint("- Unblock all currently blocked IPs.");
		consolePrint("accounts");
		consolePrint("- View all usernames that clients can login to this server with.");
		consolePrint("view_blocktime");
		consolePrint("- View how long IPs are blocked for after " + server.getNumLoginAttempts() + " attempts");
		consolePrint("change_blocktime");
		consolePrint("- Change how long IPs are blocked for after " + server.getNumLoginAttempts() + " attempts");
		consolePrint("view_number_login_attempts");
		consolePrint("- View how many failed login attempts are allowed before blocking IP for " + server.viewBlocktime() + " seconds");
		consolePrint("change_number_login_attempts");
		consolePrint("- Change how many failed login attempts are allowed before blocking IP for " + server.viewBlocktime() + " seconds");
		consolePrint("number_threads");
		consolePrint("- View the current number of threads running on this server.");
		consolePrint("current_ips");
		consolePrint("- View all currently connected IPs.");
		consolePrint("last_hr");
		consolePrint("- View the usernames used by clients currently connected.");
		consolePrint("version");
		consolePrint("- View the version of this server.");
		consolePrint("shut_logger");
		consolePrint("- Safely close the logging stream.");
	}
	private void viewBlockedIPs(){
		ArrayList<BlockedIP> blockedIPs = server.getBlockedIPs();
		for(BlockedIP ip : blockedIPs){
			consolePrint(ip.getIP().toString());
		}
	}
	private void unblockAllIPs(){
		server.removeAllBlockedIPs();
		consolePrint("All IPs are now unblocked.");
	}
	private void viewAccounts(){
		ArrayList<Account> accounts = server.getAccounts();
		for(Account account : accounts){
			consolePrint(account.getUserName());
		}
	}		
	private void viewNumberLoginAttempts(){
		consolePrint(Integer.toString(server.getNumLoginAttempts()) + " attempts allowed");
	}
	private void changeNumberLoginAttempts(){
		consolePrint("What is the new number of allowed login attempts?");
		int newNumber = Integer.parseInt(consoleScanner.nextLine());
		System.out.print("    > ");
		server.changeBlockTime(newNumber);
		consolePrint("The number of login attempts allowed is now " + newNumber);
	}
	private void viewBlockTime(){
		consolePrint(Integer.toString(server.viewBlocktime()) + " seconds");
	}
	private void changeBlockTime(){
		consolePrint("What is the new block time in seconds?");
		int newBlockTime = Integer.parseInt(consoleScanner.nextLine());
		System.out.print("    > ");
		server.changeBlockTime(newBlockTime);
		consolePrint("The block time is now " + newBlockTime + " seconds");
	}
	private void numberThreads(){
		consolePrint(Thread.activeCount() + " threads currently running");
	}	
	private void viewCurrentIPs(){
		ArrayList<ServerThread> conns = server.getCurrentClients();
		for(ServerThread client : conns){
			consolePrint(client.getSocket().getInetAddress().toString());
		}
	}	
	private void viewLastHr(){
		ArrayList<String> usersLastHr = server.getUsersLastHr();
		for(String user : usersLastHr){
			consolePrint(user);
		}
	}
	private void version(){
		consolePrint("Simple Server version is 1.0");
	}
	private void shutLogger(){
		consolePrint("Logger was safely closed");
		server.closeLogger();
		consolePrint("You may now close the server");
	}
	private void consolePrint(String print){
		System.out.println("    " + print);
	}
}
