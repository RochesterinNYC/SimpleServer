import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
	
	private PrintWriter logger;
	
	private long timeOut;
	private long roundTripTime;
	
	private void adjustTimeOut(){
		this.timeOut = (long) (0.25 * timeOut + 0.75 * roundTripTime);
		try {
			packetSocket.setSoTimeout((int) timeOut);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private void markRoundTripTime(){
		roundTripTime = System.currentTimeMillis() - roundTripTime;
	}
	
	public TCPSender(String fileName, InetAddress remoteIP, int remotePort, int ackPort, int windowSize, String logFileName){
		this.fileName = fileName;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.ackPort = ackPort;
		this.windowSize = windowSize;
		this.logFileName = logFileName;
		this.responseBuffer = new byte[20];
		this.responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
		this.timeOut = 5000;
		this.roundTripTime = 5000;
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
			this.logger = new PrintWriter(new BufferedWriter(new FileWriter(this.logFileName, true)));
			while(packetsAcknowledged != numPackets){//While file data packets have not all been acknowledged
				packetReceived = false;
				firstTimePacket = true;
				adjustTimeOut();
				while(!packetReceived){ //While packet in current window has not been acknowledged
					//send data packet
					packetBuffer = packetSet[packetsAcknowledged].getPacketLoad();
					packet = new DatagramPacket(packetBuffer, packetBuffer.length, remoteIP, remotePort);
					packetSocket.send(packet);
					roundTripTime = System.currentTimeMillis();
					logPacket(packet, true);
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
					try{
						packetSocket.receive(responsePacket);
						markRoundTripTime();
						logPacket(responsePacket, false);
						//if ACK is for correct sequence number && received within timeout && packet wasn't corrupted && response packet wasn't corrupted itself
						if(Packet.getACKNumber(responsePacket.getData()) == currentSequenceNumber && Packet.getPurpose(responsePacket.getData()[12]) == "ACK" && !Packet.isCorrupt(responsePacket.getData())){
							packetReceived = true;
						  packetsAcknowledged++;
						}
						
					}
					catch (SocketTimeoutException e) {
		                // timeout
						firstTimePacket = false;
		            }					
				}//End of individual packet loop
			}//End of all packets loop
			//send FIN
			Packet finPacket = new Packet(ackPort, remotePort, 0, 0, "FIN", null);
			packetBuffer = finPacket.getPacketLoad();
			packet = new DatagramPacket(packetBuffer, packetBuffer.length, remoteIP, remotePort);
			packetSocket.send(packet);
			logPacket(packet, true);
			logger.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		packetSocket.close();
		
		//Print stats
		System.out.println("    Delivery completed successfully");
		System.out.println("    Total bytes sent = " + numBytesSent);
		System.out.println("    Segments sent = " + numPacketsSent);
		System.out.println("    Segments retransmitted = " + numPacketsResent);
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
	//timestamp, source, destination, Sequence #, ACK #, and the flags
	private void logPacket(DatagramPacket packet, boolean sendingOut){
		InetAddress sourceIP;
		int sourcePort;
		InetAddress destinationIP;
		int destinationPort;
		
		if(sendingOut){//packet sent out
			sourceIP = packetSocket.getLocalAddress();
			sourcePort = packetSocket.getLocalPort();
			destinationIP = packet.getAddress();
			destinationPort = packet.getPort();
		}
		else{//packet received
			sourceIP = packet.getAddress();
			sourcePort = packet.getPort();
			destinationIP = packetSocket.getLocalAddress();
			destinationPort = packetSocket.getLocalPort();
		}	
		
		String source = sourceIP.toString() + " : " + sourcePort;
		String destination = destinationIP.toString() + " : " + destinationPort; 
		int sequenceNumber = Packet.getSequenceNumber(packet.getData());
		int ackNumber = Packet.getACKNumber(packet.getData());
		String flag = Packet.getPurpose(packet.getData()[12]);
		
		logger.println("[ " + System.currentTimeMillis() + " ], " + source + " , " + destination + " , " + sequenceNumber + " , " + ackNumber + " , " + flag);
		logger.flush();
	}
}
