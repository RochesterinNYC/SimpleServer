import java.net.InetAddress;


public class BlockedIP {
	private InetAddress ip;
	private long timeBlocked;
	public BlockedIP(InetAddress ip){
		this.ip = ip;
		this.timeBlocked = System.currentTimeMillis();
	}
	public boolean toUnblock(){
		boolean toUnblock = false;
		if (System.currentTimeMillis() - timeBlocked > 60000){
			toUnblock = true;
		}
		return toUnblock;
	}
	
	public InetAddress getIP(){
		return ip;
	}

}
