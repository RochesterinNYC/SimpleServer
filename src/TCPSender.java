import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
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
	private DatagramSocket packetSocket;
	
	private final int SEGSIZE = 576;
	private final int HEADSIZE = 20;
	
	private File queryFile;
	private FileInputStream fileReader;
	private int numPackets;
	
	public TCPSender(String fileName, InetAddress remoteIP, int remotePort, int ackPort, int windowSize, String logFileName){
		this.fileName = fileName;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.ackPort = ackPort;
		this.windowSize = windowSize;
		this.logFileName = logFileName;
		try {
			this.packetSocket = new DatagramSocket(this.ackPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		processFile();
	}
	private void processFile(){
		try {
			queryFile = new File(fileName);
			fileReader = new FileInputStream(queryFile);
			numPackets = (int) Math.ceil(queryFile.length() / (double)(SEGSIZE - HEADSIZE));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void send(){
		int packetsAcknowledged = 0;
		boolean packetReceived;
		boolean firstTimePacket;
		byte buffer[];
		DatagramPacket packet;
		int numPacketsSent;
		long numBytesSent;
		int numPacketsResent;
		Packet packetSet[] = new Packet[numPackets];
		packetSet = prepPackets();
		
		while(packetsAcknowledged != numPackets){
			packetReceived = false;
			firstTimePacket = true;
			//prepNextPacket();
			while(!packetReceived){
				//sendPacket
				//numPacketsSent++
				//numBytesSent increase
				//if !firstTimePacket
				  //numPacketsResent++
				//getACK
				//if ACK received within timeout && packet wasn't corrupted
				  //packetRecieved = true
				  //packetsAcknowledged++
				//firstTimePacket = false
			}
		}
		//send FIN
		//numPacketsSent++
		//numBytesSent increase
		//Print Stats
	}
	int sourcePortNumber, int destPortNumber, int sequenceNumber, 
	  int ackNumber, int checkSum, String purposeCode, byte[] data
	private Packet[] prepPackets(){
		Packet packetSet[] = new Packet[numPackets];
		for (int i = 0; i < numPackets; i++){
			new Packet(ackPort, remotePort, i, , , , )
		}
	}
}
