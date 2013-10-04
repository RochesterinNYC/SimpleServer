
public enum ServerThreadType {
	/**
	 * Base Thread (for welcoming)
	 */
	BASE, 

	/**
	 * Thread for broadcasting to clients
	 */
	BROADCAST, 
	
	/**
	 * Thread for operating with clients
	 */
	CLIENT, 
	
	/**
	 * Null
	 */
	NONE
}
