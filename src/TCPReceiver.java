import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class TCPReceiver {
	
	private String fileName;
	private int listenPort;
	private InetAddress remoteIP; //server IP
	private int remotePort; //server port to receive 
	private String logFileName;
	private DatagramSocket packetSocket;
	
	private DatagramPacket bufferPacket;
	private byte[] buffer;
	private byte[] completeFileBuffer;
	
	private PrintWriter logger;
	
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
		ArrayList<byte[]> fileParts = new ArrayList<byte[]>();
		byte responseBuffer[];
		DatagramPacket responsePacket;
		Packet responsePacketData;
		int currentSequenceNumber = 1;
		
		try {
			if(logFileName.equals("stdout")){
				this.logger = new PrintWriter(System.out);
			}
			else{
				this.logger = new PrintWriter(new BufferedWriter(new FileWriter(this.logFileName, true)));
			}
			while(!tcpComplete){	
				packetSocket.receive(bufferPacket);
				logPacket(bufferPacket, false);
				if (Packet.getPurpose(bufferPacket.getData()[12]) == "FIN"){
					tcpComplete = true;
				}
				//Structure needs refactoring
				else if (Packet.getSequenceNumber(bufferPacket.getData()) != currentSequenceNumber){//wrong packet sequence number
					//send ACK 
					responsePacketData = new Packet(listenPort, remotePort, 0, currentSequenceNumber, "ACK", null);
					responseBuffer = responsePacketData.getPacketLoad();
					responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, remoteIP, remotePort);
					packetSocket.send(responsePacket);
					logPacket(responsePacket, true);
				}
				else if (Packet.isCorrupt(bufferPacket.getData())){//is corrupt data packet
					//send CORR response
					responsePacketData = new Packet(listenPort, remotePort, 0, 0, "CORR", null);
					responseBuffer = responsePacketData.getPacketLoad();
					responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, remoteIP, remotePort);
					packetSocket.send(responsePacket);
					logPacket(responsePacket, true);
				}
				else{//Packet is not corrupt and is correct one
					fileParts.add(bufferPacket.getData());
					currentSequenceNumber = Packet.getACKNumber(bufferPacket.getData());
					//send ACK response 
					responsePacketData = new Packet(listenPort, remotePort, 0, currentSequenceNumber, "ACK", null);
					responseBuffer = responsePacketData.getPacketLoad();
					responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, remoteIP, remotePort);
					packetSocket.send(responsePacket);
					logPacket(responsePacket, true);
				}				
				//flush buffer and packet
				this.buffer = new byte[576];
				this.bufferPacket = new DatagramPacket(buffer, buffer.length);
			}   
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
		compileFile(fileParts);
		if(!logFileName.equals("stdout")){
			logger.close();
		}
		packetSocket.close();
		System.out.println("    Delivery completed successfully");
	}
	private void compileFile(ArrayList<byte[]> filePortions){
		int fileLength = 0;
		for(byte[] buff : filePortions){
			fileLength += (Packet.getDataLength(buff));
		}
		completeFileBuffer = new byte[fileLength];
		int fileFilled = 0;
		for(byte[] buff : filePortions){
            for(int i = 20; i < Packet.getDataLength(buff) + 20; i++){
				completeFileBuffer[fileFilled] = buff[i];
				fileFilled++;
			}
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fileName);
			fos.write(completeFileBuffer);
			fos.close();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
