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
 * 13-14 = data length (in bytes) 
 * 15-19 = TCP CheckSum
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
	
	private final int HEADERSIZE = 20;
	private final static int CHECKSUMSIZE = 5;
	
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
		int packetLength = 0;
		if(data != null){
			packetLength = HEADERSIZE + data.length;
		}
		else{
		    packetLength = HEADERSIZE;
		}
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
		packetLoad[13] = (byte) (data.length / 256);
		packetLoad[14] = (byte) (data.length % 256);
		if(data != null){
			for(int i = HEADERSIZE; i < HEADERSIZE + data.length; i++){
				packetLoad[i] = data[i - HEADERSIZE];
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
		byte[] checkArray = new byte[(HEADERSIZE - CHECKSUMSIZE) + data.length];
		for(int i = 0; i < (HEADERSIZE - CHECKSUMSIZE); i++){
			checkArray[i] = packetLoad[i];
		}
		for(int i = HEADERSIZE - CHECKSUMSIZE; i < (HEADERSIZE - CHECKSUMSIZE) + data.length; i++){
			checkArray[i] = data[i - (HEADERSIZE - CHECKSUMSIZE)];
		}
		try {
			checkSum = generateCheckSum(checkArray);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
	}
	
	public static byte[] generateCheckSum(byte[] byteArray) throws NoSuchAlgorithmException{
		return MessageDigest.getInstance("MD5").digest(byteArray);
	}
	
	public static boolean compareCheckSums(byte[] checkSum1, byte[] checkSum2){
		boolean checkSumsMatch = true;
		for(int i = 0; i < CHECKSUMSIZE; i++){
			if(checkSum1[i] != checkSum2[i]){
				checkSumsMatch = false;
			}
		}
		return checkSumsMatch;
	}
	
	//Only taking first 5 bytes of checksum
	private void finalizePacket(){
		//Add checkSum to packet
		for(int i = HEADERSIZE - CHECKSUMSIZE; i < HEADERSIZE; i++){
			packetLoad[i] = checkSum[i - (HEADERSIZE - CHECKSUMSIZE)];
		}
	}
}
