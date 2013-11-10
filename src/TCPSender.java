import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class TCPSender {
	private String fileName;
	private InetAddress remoteIP; //IP of proxy
	private int remotePort; //port of proxy
	private int ackPort;
	private int windowSize;
	private String logFileName;
	private final int fileSendPort = 9999;
	private DatagramSocket ackSocket;
	private DatagramSocket sendSocket;
	
	public TCPSender(String fileName, InetAddress remoteIP, int remotePort, int ackPort, int windowSize, String logFileName){
		this.fileName = fileName;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.ackPort = ackPort;
		this.windowSize = windowSize;
		this.logFileName = logFileName;
		try {
			this.ackSocket = new DatagramSocket(this.ackPort);
			this.sendSocket = new DatagramSocket(this.fileSendPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}
	
	public void send(){
		
	}
}
