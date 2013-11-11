import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class TCPReceiver {
	
	private String fileName;
	private int listenPort;
	private InetAddress remoteIP; //server IP
	private int remotePort; //server port to receive 
	private String logFileName;
	private DatagramSocket packetSocket;
	
	private DatagramPacket bufferPacket;
	private byte[] buffer;
	
	public TCPReceiver(String fileName, int listenPort, InetAddress remoteIP, int remotePort, String logFileName){
		this.fileName = fileName;
		this.listenPort = listenPort;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.logFileName = logFileName;
		this.buffer = new byte[576];
		this.bufferPacket = new DatagramPacket(buffer, buffer.length);
		try {
			this.packetSocket = new DatagramSocket(this.listenPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void receive(){
		boolean tcpComplete = false;
		while(!tcpComplete){
			try {
				packetSocket.receive(bufferPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(bufferPacket);
			System.out.println(buffer);
			//receive a packet
			//if packet is corrupt
			  //send corrupt ACK
		    //if packet is fine
			  //send regular ACK
			//if packet is FIN
			  //tcpComplete = true
		}
		//Print stats
	}
}
