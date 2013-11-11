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
 * 00000010 = CORR (Received packet was corrupt)
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
	private int dataLength;
	
	private final static int HEADERSIZE = 20;
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
		if(data != null){
			this.dataLength = data.length;
		}
		else{
			this.dataLength = 0;
		}
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
			packetLength = HEADERSIZE + dataLength;
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
		packetLoad[13] = (byte) (dataLength / 256);
		packetLoad[14] = (byte) (dataLength % 256);
		if(data != null){
			for(int i = HEADERSIZE; i < HEADERSIZE + dataLength; i++){
				packetLoad[i] = data[i - HEADERSIZE];
			}
		}
	}
	
	private byte getPurposeByte(String purpose){
		byte purposeByte = Byte.parseByte("00000000", 2);
		if(purpose.equals("DATA")){
			purposeByte = Byte.parseByte("00000000", 2);
		}
		else if(purpose.equals("ACK")){
			purposeByte = Byte.parseByte("00000001", 2);
		}
		else if(purpose.equals("COR")){
			purposeByte = Byte.parseByte("00000010", 2);
		}
		else if(purpose.equals("FIN")){
			purposeByte = Byte.parseByte("00000011", 2);
		} 
		return purposeByte;
	}
	
	public static String getPurpose(byte purposeByte){
		String purpose = "";
		if (purposeByte == Byte.parseByte("00000000", 2)){
			purpose = "DATA";
		}
		else if(purposeByte == Byte.parseByte("00000001", 2)){
			purpose = "ACK";
		}
		else if(purposeByte == Byte.parseByte("00000010", 2)){
			purpose = "CORR";
		}
		else if(purposeByte == Byte.parseByte("00000011", 2)){
			purpose = "FIN";
		}
		return purpose;
	}
	
	private void calculateCheckSum(){
		//Create byte array of everything without checksum
		byte[] checkArray = new byte[(HEADERSIZE - CHECKSUMSIZE) + dataLength];
		for(int i = 0; i < (HEADERSIZE - CHECKSUMSIZE); i++){
			checkArray[i] = packetLoad[i];
		}
		for(int i = HEADERSIZE - CHECKSUMSIZE; i < (HEADERSIZE - CHECKSUMSIZE) + dataLength; i++){
			checkArray[i] = data[i - (HEADERSIZE - CHECKSUMSIZE)];
		}
		try {
			checkSum = generateCheckSum(checkArray);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
	}
	
	public static boolean isCorrupt(byte[] packetData) throws NoSuchAlgorithmException{
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
		entireCalcCheckSum = generateCheckSum(queryArray);
		
		//Collect actual check sum and calculated check sum (first 5 bytes)
		for(int i = (HEADERSIZE -  CHECKSUMSIZE); i < HEADERSIZE; i++){
			actualCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)] = packetData[i];
			calculatedCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)] = entireCalcCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)];
		}
		packetCorrupt = !compareCheckSums(calculatedCheckSum, actualCheckSum);
		return packetCorrupt;
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
	
	public static int getDataLength(byte[] packetLoad){
	    int val = 0x00000000;
	    val |= ((0x000000FF & packetLoad[13]) << 8) | (0x000000FF & packetLoad[14]);
	    return val;
	}
	
	//Only taking first 5 bytes of checksum
	private void finalizePacket(){
		//Add checkSum to packet
		for(int i = HEADERSIZE - CHECKSUMSIZE; i < HEADERSIZE; i++){
			packetLoad[i] = checkSum[i - (HEADERSIZE - CHECKSUMSIZE)];
		}
	}
}
