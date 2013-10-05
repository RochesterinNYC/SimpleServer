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
	 * - The server thread that interacts with the client's thread that is listening
	 * for broadcasts.
	 */
	BROADCAST, 
	
	/**
	 * Client Thread
	 * - The server thread that interacts with the primary client thread and 
	 * accepts client user input and sends server output.
	 */
	CLIENT, 
	
	/**
	 * Placeholder null
	 */
	NONE
}
