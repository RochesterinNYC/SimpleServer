import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
		byte packetBuffer[];
		DatagramPacket packet;
		int numPacketsSent;
		long numBytesSent;
		int numPacketsResent;
		Packet packetSet[] = new Packet[numPackets];
		packetSet = prepPackets();
		
		while(packetsAcknowledged != numPackets){
			packetReceived = false;
			firstTimePacket = true;

			
			//while(!packetReceived){
				//sendPacket(packetSet[packetsAcknowledged])
				try {
					packetBuffer = packetSet[packetsAcknowledged].getPacketLoad();
					packet = new DatagramPacket(packetBuffer, packetBuffer.length, remoteIP, remotePort);
					packetSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//numPacketsSent++
				//numBytesSent increase
				//if !firstTimePacket
				  //numPacketsResent++
				//getACK
				//if ACK received within timeout && packet wasn't corrupted
				  //packetRecieved = true
				  packetsAcknowledged++;
				  try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  
				//firstTimePacket = false
			//}
		}
		//send FIN
		//numPacketsSent++
		//numBytesSent increase
		//Print Stats
	}

	private Packet[] prepPackets(){
		Packet packetSet[] = new Packet[numPackets];
		byte[] dataBuffer = new byte[556];
		int fileSequenceNumber = 1; 
		try{
			for (int i = 0; i < numPackets; i++){
				if(i == numPackets - 1){//last one
					dataBuffer = new byte[fileReader.available()];
				
				}
				fileReader.read(dataBuffer);
				packetSet[i] = new Packet(ackPort, remotePort, fileSequenceNumber, 0, "DATA", dataBuffer);
				fileSequenceNumber += (SEGSIZE - HEADSIZE);
			}
		}
		catch(IOException e){
			//Fill
		}
		return packetSet;
	}
}
