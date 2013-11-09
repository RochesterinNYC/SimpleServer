import java.net.InetAddress;


public class TCPReceiver {
	
	private String fileName;
	private int listenPort;
	private InetAddress remoteIP;
	private int remotePort;
	private String logFileName;
	
	public TCPReceiver(String fileName, int listenPort, InetAddress remoteIP, int remotePort, String logFileName){
		this.fileName = fileName;
		this.listenPort = listenPort;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.logFileName = logFileName;
	}
}
