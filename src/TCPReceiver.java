import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class TCPReceiver {
	
	private String fileName;
	private int listenPort;
	private InetAddress remoteIP; //server IP
	private int remotePort; //server port to receive 
	private String logFileName;
	private DatagramSocket packetSocket;
	private final int HEADERSIZE = 20;
	private final int CHECKSUMSIZE = 5;
	
	private DatagramPacket bufferPacket;
	private byte[] buffer;
	private byte[] completeFileBuffer;
	
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
	private boolean corruptCheck(byte[] packetData) throws NoSuchAlgorithmException{
		boolean packetCorrupt;
		byte[] calculatedCheckSum = new byte[CHECKSUMSIZE];
		byte[] actualCheckSum = new byte[CHECKSUMSIZE];
		byte[] entireCalcCheckSum;
		byte[] queryArray;
		
		//Generate byte array of all headers other than checksum and data
		queryArray = new byte[(HEADERSIZE -  CHECKSUMSIZE) + getDataLength(packetData)];
		for(int i = 0; i < (HEADERSIZE -  CHECKSUMSIZE); i++){
			queryArray[i] = packetData[i];
		}
		for(int i = (HEADERSIZE -  CHECKSUMSIZE); i < queryArray.length; i++){
			queryArray[i] = packetData[i + (CHECKSUMSIZE)];
		}
		
		//Calculate check sum of what's been received
		entireCalcCheckSum = Packet.generateCheckSum(queryArray);
		
		//Collect actual check sum and calculated check sum (first 5 bytes)
		for(int i = (HEADERSIZE -  CHECKSUMSIZE); i < HEADERSIZE; i++){
			actualCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)] = packetData[i];
			calculatedCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)] = entireCalcCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)];
		}
		packetCorrupt = !Packet.compareCheckSums(calculatedCheckSum, actualCheckSum);
		return packetCorrupt;
	}

	public void receive(){
		boolean tcpComplete = false;
		ArrayList<byte[]> fileParts = new ArrayList<byte[]>();
		try {	
			while(!tcpComplete){	
				packetSocket.receive(bufferPacket);
				if (Packet.getPurpose(bufferPacket.getData()[12]) == "FIN"){
					tcpComplete = true;
				}
				else if (corruptCheck(bufferPacket.getData())){//is corrupt data packet
					//send corrupt ACK
					
				}
				else{//Packet is not corrupt and is correct one
					fileParts.add(bufferPacket.getData());
					
					//send regular ACK
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
		//Print stats
		packetSocket.close();
	}
	private void compileFile(ArrayList<byte[]> filePortions){
		int fileLength = 0;
		for(byte[] buff : filePortions){
			fileLength += (getDataLength(buff));
		}
		completeFileBuffer = new byte[fileLength];
		int fileFilled = 0;
		for(byte[] buff : filePortions){
            for(int i = HEADERSIZE; i < getDataLength(buff) + HEADERSIZE; i++){
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
	private int getDataLength(byte[] packetLoad){
	    int val = 0x00000000;
	    val |= ((0x000000FF & packetLoad[13]) << 8) | (0x000000FF & packetLoad[14]);
	    return val;
	}
}
