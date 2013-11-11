import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
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
		while(!tcpComplete){
			try {
				packetSocket.receive(bufferPacket);
				if (Packet.getPurpose(bufferPacket.getData()[12]) == "FIN"){
					tcpComplete = true;
				}
				else{//Packet is not corrupt and is correct one
					fileParts.add(bufferPacket.getData());
					//flush buffer and packet
					this.buffer = new byte[576];
					this.bufferPacket = new DatagramPacket(buffer, buffer.length);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//if packet is corrupt
			  //send corrupt ACK
		    //if packet is fine
			  //send regular ACK
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
            for(int i = 20; i < getDataLength(buff) + 20; i++){
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
