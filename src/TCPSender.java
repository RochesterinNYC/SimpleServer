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

/**
 * <b>TCPSender Class</b>
 * <p>
 * Represents the TCP Sender that is sending and transmitting to a receiver.
 * Simulates it through UDP.
 * @author James Wen - jrw2175
*/
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
	private long currentTripTime;

    /**
    * TCPSender constructor
    * <p>
    * Creates a TCPSender that will be sending data packets from a specific port
    * to a receiver at a remote IP and port.
    * resulting packet is constructed accordingly.
    * @param fileName - the name of the file to be sent/transmitted
    * @param remoteIP - the IP Address of the receiver
    * @param remotePort - the port of the receiver
    * @param ackPort - the port that this TCPSender will be sending packets from
    * @param windowSize - the size of the packet transmission window
    * @param logFileName - the name of the file that transmissions and receiving will be logged in
    */
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
	
    /**
     * processFile
     * <p>
     * Processes the file to be sent by opening a read stream for the file and 
     * calculating the number of packets that will be needed to send the file.
     * @return toLogOut - whether this client should log out
     */
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
	
    /**
     * send
     * <p>
     * Sends the file to the receiver through packets.
     */
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
				adjustTimeOut();
				firstTimePacket = true;
				
				while(!packetReceived){ //While packet in current window has not been acknowledged
					//send data packet
					packetBuffer = packetSet[packetsAcknowledged].getPacketLoad();
					packet = new DatagramPacket(packetBuffer, packetBuffer.length, remoteIP, remotePort);
					currentTripTime = System.currentTimeMillis();
					packetSocket.send(packet);
					logPacket(packet, true, false);
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
						markCurrentTripTime();
						
						//if ACK is for correct sequence number && received within timeout && packet wasn't corrupted && response packet wasn't corrupted itself
						if(Packet.getACKNumber(responsePacket.getData()) == currentSequenceNumber && Packet.getPurpose(responsePacket.getData()[12]) == "ACK" && !Packet.isCorrupt(responsePacket.getData())){
							packetReceived = true;
							packetsAcknowledged++;
							//if packet segment only transmitted once, calculate RTT
							if(firstTimePacket){
								roundTripTime = currentTripTime;
							}
							logPacket(responsePacket, false, firstTimePacket);							
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
			logPacket(packet, true, false);
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

    /**
     * prepPackets
     * <p>
     * Creates the set of packets that will be sent to receiver from the file.
     * @return packetSet - the set of packets to be sent
     */
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
	
	/**
     * adjustTimeOut
     * <p>
     * Adjusts the time out (only actually changes it if the packet was received
     * and acknowledged on first attempt.
     */
	private void adjustTimeOut(){
		this.timeOut = (long) (0.25 * timeOut + 0.75 * roundTripTime);
		try {
			packetSocket.setSoTimeout((int) timeOut);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
     * markCurrentTripTime
     * <p>
     * Records the round trip time of the current packet.
     */
	private void markCurrentTripTime(){
		currentTripTime = System.currentTimeMillis() - currentTripTime;
	}
	
    /**
     * logPacket
     * <p>
     * Logs the sending or receiving of a packet.
     * Logs: timestamp, source, destination, Sequence #, ACK #, flags, and RTT 
     * if packet is acknowledged on first time
     * @param packet - the packet to be logged
     * @param sendingOut - whether the packet was one that was sent out or received
     * @param firstTimeSent - whether this is the first time a sent packet is being sent
     */
	private void logPacket(DatagramPacket packet, boolean sendingOut, boolean firstTimeSent){
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
		String timeOut = "";
		
		if (firstTimeSent){
			timeOut = ", " + roundTripTime;
		}
		
		logger.println("[ " + System.currentTimeMillis() + " ], " + source + " , " + destination + " , " + sequenceNumber + " , " + ackNumber + " , " + flag + timeOut);
		logger.flush();
	}

}
