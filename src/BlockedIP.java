import java.net.InetAddress;
/**
* <b>BlockedIP Class</b>
* <p>
* Represents an IP that has been blocked by a server.
* timeBlocked is the time that the IP was blocked.
* @author James Wen - jrw2175
*/

public class BlockedIP {
	private InetAddress ip;
	private long timeBlocked;
	/**
	* <b>BlockedIP Constructor</b>
	* <p>
	* Creates a BlockedIP and records the time at which the blocking occurred.
	* @param ip - the IP Address that is to be blocked
	*/
	public BlockedIP(InetAddress ip){
		this.ip = ip;
		this.timeBlocked = System.currentTimeMillis();
	}
	/**
	* <b>toUnblock</b>
	* <p>
	* Gets whether this IP should now be unblocked (according to BLOCKTIME of
	* server). If the (current time of this method query) - (the time at which the
	* IP was blocked) is greater than the server's BLOCKTIME, then the IP should
	* be unblocked.
	* @return toUnblock - whether this IP is to be unblocked
	*/
	public boolean toUnblock(){
		boolean toUnblock = false;
		if (System.currentTimeMillis() - timeBlocked > 60000){
			toUnblock = true;
		}
		return toUnblock;
	}
	/**
	* <b>getIP</b>
	* <p>
	* Gets the IP Address of this Blocked IP.
	* @return ip - the IP Address of this BlockedIP
	*/
	public InetAddress getIP(){
		return ip;
	}

}
