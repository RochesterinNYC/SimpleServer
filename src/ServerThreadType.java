/**
 * <b>ServerThreadType enum</b>
 * <p>
 * The possible server thread types that can be running.
 * @author James Wen - jrw2175
 */
public enum ServerThreadType {
		
	/**
	 * Base Thread
	 * - The server thread that listens in and greets clients and creates new 
	 * broadcast and client threads accordingly.
	 * - Only one thread of this type operating at any one time.
	 */
	BASE, 
	
	/**
	 * Broadcast Thread
	 * - Server thread that sends broadcast info and messages to clients
	 */
	BROADCAST, 
	
	/**
	 * Client Thread
	 * - The server thread that interacts with the primary client thread and 
	 * accepts client user input and sends server output.
	 */
	CLIENT, 
	
	/**
	 * CommandLine Thread
	 * - The command line thread that interacts with the server administrator
	 * to perform basic tasks.
	 */
	COMMANDLINE, 
	
	/**
	 * Placeholder null
	 */
	NONE
}
