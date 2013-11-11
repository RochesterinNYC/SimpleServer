import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;


public class TCPSender {
	private String fileName;
	private InetAddress remoteIP; //IP of proxy
	private int remotePort; //port of proxy
	private int ackPort;
	private int windowSize;
	private String logFileName;
	private DatagramSocket packetSocket;
	
	//for ACK handling
	private DatagramPacket responsePacket;
	private byte[] responseBuffer;
		
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
		this.responseBuffer = new byte[20];
		this.responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
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
		int numPacketsSent = 0;
		long numBytesSent = 0;
		int numPacketsResent = 0;
		Packet packetSet[] = new Packet[numPackets];
		packetSet = prepPackets();
		int currentSequenceNumber = 1;
		
		try{
			while(packetsAcknowledged != numPackets){//While file data packets have not all been acknowledged
				packetReceived = false;
				firstTimePacket = true;					
				
				while(!packetReceived){ //While packet in current window has not been acknowledged
					//send data packet
					packetBuffer = packetSet[packetsAcknowledged].getPacketLoad();
					packet = new DatagramPacket(packetBuffer, packetBuffer.length, remoteIP, remotePort);
					packetSocket.send(packet);
					if(firstTimePacket){
						currentSequenceNumber += (packet.getLength() - HEADSIZE);
					}
					
					//log stats
					numPacketsSent++;
					numBytesSent += packetSet[packetsAcknowledged].getPacketLoad().length;
					if(!firstTimePacket){
						numPacketsResent++;
					}
					
					//wait for ACK or CORR response
					packetSocket.receive(responsePacket);
					
					//if ACK is for correct sequence number && received within timeout && packet wasn't corrupted && response packet wasn't corrupted itself
					if(Packet.getACKNumber(responsePacket.getData()) == currentSequenceNumber && Packet.getPurpose(responsePacket.getData()[12]) == "ACK" && !Packet.isCorrupt(responsePacket.getData())){
					  packetReceived = true;
					  packetsAcknowledged++;
					}
					firstTimePacket = false;
				}
			}
			//send FIN
			Packet finPacket = new Packet(ackPort, remotePort, 0, 0, "FIN", null);
			packetBuffer = finPacket.getPacketLoad();
			packet = new DatagramPacket(packetBuffer, packetBuffer.length, remoteIP, remotePort);
			packetSocket.send(packet);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		//numPacketsSent++
		//numBytesSent increase
		//Print Stats
		packetSocket.close();
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
				packetSet[i] = new Packet(ackPort, remotePort, fileSequenceNumber, fileSequenceNumber + dataBuffer.length, "DATA", dataBuffer);
				fileSequenceNumber += (SEGSIZE - HEADSIZE);
			}
		}
		catch(IOException e){
			//Fill
		}
		return packetSet;
	}
}
