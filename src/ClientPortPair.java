
public class ClientPortPair {
	private int interfacePort;
	private int broadcastPort;
	private int clientID;
	
	public ClientPortPair(int interfacePort, int clientID){
		this.interfacePort = interfacePort;
		this.clientID = clientID;
	}
	
	public void setBroadcastPort(int broadcastPort){
		this.broadcastPort = broadcastPort;
	}
	
	public int getInterfacePort(){
		return interfacePort;
	}
	
	public int getBroadcastPort(){
		return broadcastPort;
	}
}
