import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Packet payload - Byte Array Structure:
 * Index = Contents
 * 0-1 = sourcePortNumber
 * 2-3 = destPortNumber
 * 4-7 = sequenceNumber
 * 8-11 = ACK #
 * 12 = purpose code 
 * 13-19 = TCP CheckSum
 * (if data) 20-575 = data
 * Purpose Code:
 * Byte Representation = Meaning
 * 00000000 = DATA
 * 00000001 = ACK
 * 00000010 = ACK - Was corrupt
 * 00000011 = FIN
 */

public class Packet {
	private int sourcePortNumber;
	private int destPortNumber;
	private int sequenceNumber;
	private int ackNumber;
	private String purposeCode;
	private byte[] data;
	private byte[] packetLoad;
	
	private byte[] checkSum;
	
	public Packet(int sourcePortNumber, int destPortNumber, int sequenceNumber, 
				  int ackNumber, String purposeCode, byte[] data){
		this.sourcePortNumber = sourcePortNumber;
		this.destPortNumber = destPortNumber;
		this.sequenceNumber = sequenceNumber;
		this.ackNumber = ackNumber;
		this.purposeCode = purposeCode;
		this.data = data;
		constructPacket();
		calculateCheckSum();
		finalizePacket();
	}
	
	public byte[] getPacketLoad(){
		return packetLoad;
	}
	
	public byte[] getCheckSum(){
		return checkSum;
	}
	
	private void constructPacket(){
		//Make packet to full just without checksum
		//if no data, then no data (packet does not have to be 576 bytes)
		int packetLength = 20 + data.length;
		packetLoad = new byte[packetLength];
		packetLoad[0] = (byte) (sourcePortNumber / 256);
		packetLoad[1] = (byte) (sourcePortNumber % 256);
		packetLoad[2] = (byte) (destPortNumber / 256);
		packetLoad[3] = (byte) (destPortNumber % 256);
		
		byte[] sequenceNumberBytes = ByteBuffer.allocate(4).putInt(sequenceNumber).array();
		byte[] ackNumberBytes = ByteBuffer.allocate(4).putInt(ackNumber).array();
		int seqIndexStart = 4;
		int ackIndexStart = 8;
		for(int i = 0; i < 4; i++){
			packetLoad[seqIndexStart + i] = sequenceNumberBytes[i];
			packetLoad[ackIndexStart + i] = ackNumberBytes[i];
		}
		packetLoad[12] = getPurposeByte(purposeCode);
		if(data != null){
			for(int i = 20; i < 20 + data.length; i++){
				packetLoad[i] = data[i - 20];
			}
		}
	}
	
	private byte getPurposeByte(String purpose){
		byte purposeByte = Byte.parseByte("00000000", 2);;
		if(purpose.equals("DATA")){
			purposeByte = Byte.parseByte("00000000", 2);
		}
		else if(purpose.equals("ACK")){
			purposeByte = Byte.parseByte("00000001", 2);
		}
		else if(purpose.equals("CORR")){
			purposeByte = Byte.parseByte("00000010", 2);
		}
		else if(purpose.equals("FIN")){
			purposeByte = Byte.parseByte("00000011", 2);
		} 
		return purposeByte;
	}
	
	private void calculateCheckSum(){
		//Create byte array of everything without checksum
		byte[] checkArray = new byte[13 + data.length];
		for(int i = 0; i < 13; i++){
			checkArray[i] = packetLoad[i];
		}
		for(int i = 13; i < 13 + data.length; i++){
			checkArray[i] = data[i - 13];
		}
		try {
			checkSum = MessageDigest.getInstance("MD5").digest(checkArray);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
	}
	
	//Only taking first 7 bytes of checksum
	private void finalizePacket(){
		//Add checkSum to packet
		for(int i = 13; i < 20; i++){
			packetLoad[i] = checkSum[i - 13];
		}
	}
}
