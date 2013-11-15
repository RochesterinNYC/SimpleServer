import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <b>Packet Class</b>
 * <p>
 * Represents the packet that gets sent through udp (in tcp simulation).
 * Note that only first 5 bytes of calculated checksums are used and placed in
 * packet.
 * @author James Wen - jrw2175
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
	
    /**
    * Packet constructor
    * <p>
    * Creates a packet that has headers and data (optional). If data is null, 
    * resulting packet is constructed accordingly.
    * @param sourcePortNumber - the port number packet is being sent from
    * @param destPortNumber - the port number packet is going to
    * @param sequenceNumber - the sequence number of the packet (first data byte)
    * @param ackNumber - the acknowledgement number of the packet
    * @param purposeCode - what the packet is meant for (acknowledging, data, etc.)
    * @param data - the data that the packet will carry (can be null)
    */
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
		//Actually construct the packet as a byte array
		constructPacket();
		calculateCheckSum();
		finalizePacket();
	}
	
	/**
     * constructPacket
     * <p>
     * Convert the packet into a byte array (without checksum)
     */
	private void constructPacket(){
		int packetLength = 0;
		//Packet length can be just 20 (no data), or anywhere between 20 and 576 
		//bytes depending on data length
		if(data != null){
			packetLength = HEADERSIZE + dataLength;
		}
		else{
		    packetLength = HEADERSIZE;
		}
		packetLoad = new byte[packetLength];
		
		//Set port numbers
		packetLoad[0] = (byte) (sourcePortNumber / 256);
		packetLoad[1] = (byte) (sourcePortNumber % 256);
		packetLoad[2] = (byte) (destPortNumber / 256);
		packetLoad[3] = (byte) (destPortNumber % 256);
		
		//Set sequence and ack numbers
		byte[] sequenceNumberBytes = ByteBuffer.allocate(4).putInt(sequenceNumber).array();
		byte[] ackNumberBytes = ByteBuffer.allocate(4).putInt(ackNumber).array();
		int seqIndexStart = 4;
		int ackIndexStart = 8;
		for(int i = 0; i < 4; i++){
			packetLoad[seqIndexStart + i] = sequenceNumberBytes[i];
			packetLoad[ackIndexStart + i] = ackNumberBytes[i];
		}
		
		//Set purpose code
		packetLoad[12] = getPurposeByte(purposeCode);
		packetLoad[13] = (byte) (dataLength / 256);
		packetLoad[14] = (byte) (dataLength % 256);
		
		//Set data (if there is any)
		if(data != null){
			for(int i = HEADERSIZE; i < HEADERSIZE + dataLength; i++){
				packetLoad[i] = data[i - HEADERSIZE];
			}
		}
	}
	
	/**
     * calculateCheckSum
     * <p>
     * Calculates the packet's checksum using MD5 and all the header bytes other 
     * than the checksum itself and the data bytes
     */
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
			System.out.println("Error has occurred with calculating checksum.");
		}		
	}
	
	/**
     * generateCheckSum
     * <p>
     * Calculates a byte array's checksum using MD5
     * @param byteArray - the byte array to calculate checksum for
     * @return check sum of the byte array (MD5)
     */
	public static byte[] generateCheckSum(byte[] byteArray) throws NoSuchAlgorithmException{
		return MessageDigest.getInstance("MD5").digest(byteArray);
	}
	
	/**
     * finalizePacket
     * <p>
     * Adds the calculated check sum to the packet
     */
	//Only taking first 5 bytes of checksum
	private void finalizePacket(){
		//Add checkSum to packet
		for(int i = HEADERSIZE - CHECKSUMSIZE; i < HEADERSIZE; i++){
			packetLoad[i] = checkSum[i - (HEADERSIZE - CHECKSUMSIZE)];
		}
	}
	
	/**
     * getPurposeByte
     * <p>
     * Returns a purposeCode as a byte.
     * @param purpose - purposeCode to be converted into byte
     * @return purposeByte - purposeCode as byte
     */
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
	
	/**
     * getPurpose
     * <p>
     * Returns the purposeByte as the correlated purpose (code).
     * @param purposeByte - a purposeCode as a byte
     * @return purpose - the correlated purpose code
     */
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
	
	/**
     * isCorrupt
     * <p>
     * Checks whether a packet represented as a byte array is corrupt using its
     * checksum
     * @param packetData - the packet (as byte array) to check for corruption
     * @return packetCorrupt - whether the packet is corrupt
     */
	public static boolean isCorrupt(byte[] packetData) throws NoSuchAlgorithmException{
		boolean packetCorrupt;
		byte[] calculatedCheckSum = new byte[CHECKSUMSIZE];
		byte[] actualCheckSum = new byte[CHECKSUMSIZE];
		byte[] entireCalcCheckSum;
		byte[] queryArray;
		
		//Generate byte array of all headers other than checksum + data
		queryArray = new byte[(HEADERSIZE -  CHECKSUMSIZE) + getDataLength(packetData)];
		for(int i = 0; i < (HEADERSIZE -  CHECKSUMSIZE); i++){
			queryArray[i] = packetData[i];
		}
		for(int i = (HEADERSIZE -  CHECKSUMSIZE); i < queryArray.length; i++){
			queryArray[i] = packetData[i + (CHECKSUMSIZE)];
		}
		
		//Calculate whole check sum of what's been received
		entireCalcCheckSum = generateCheckSum(queryArray);
		
		//Collect stated check sum and calculated check sum (first 5 bytes)
		for(int i = (HEADERSIZE -  CHECKSUMSIZE); i < HEADERSIZE; i++){
			actualCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)] = packetData[i];
			calculatedCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)] = entireCalcCheckSum[i - (HEADERSIZE -  CHECKSUMSIZE)];
		}
		//Compare checksums
		packetCorrupt = !compareCheckSums(calculatedCheckSum, actualCheckSum);
		return packetCorrupt;
	}
	

	/**
     * compareCheckSums
     * <p>
     * Compares two checksums
     * @param checkSum1 - the first checksum 
     * @param checkSum2 - the second checksum
     * @return whether the checksums are equal
     */
	public static boolean compareCheckSums(byte[] checkSum1, byte[] checkSum2){
		boolean checkSumsMatch = true;
		for(int i = 0; i < CHECKSUMSIZE; i++){
			if(checkSum1[i] != checkSum2[i]){
				checkSumsMatch = false;
			}
		}
		return checkSumsMatch;
	}
	
	/**
     * getDataLength
     * <p>
     * Returns the length of the data in a packet
     * @param packetLoad - packet as a byte array
     * @return length - the length of the data
     */
	public static int getDataLength(byte[] packetLoad){
	    int length = 0x00000000;
	    length |= ((0x000000FF & packetLoad[13]) << 8) | (0x000000FF & packetLoad[14]);
	    return length;
	}
	
	/**
     * getSequenceNumber
     * <p>
     * Returns the sequence number of a packet
     * @param packetLoad - packet as a byte array
     * @return sequence number of a packet
     */
	public static int getSequenceNumber(byte[] packetLoad){
		return getACKOrSeq(packetLoad, 4);
	}
	
	/**
     * getACKNumber
     * <p>
     * Returns the ack number of a packet
     * @param packetLoad - packet as a byte array
     * @return ack number of a packet
     */
	public static int getACKNumber(byte[] packetLoad){
		return getACKOrSeq(packetLoad, 8);
	}
	
	/**
     * getACKOrSeq
     * <p>
     * Returns the ack or sequence number of a packet. Decides which to return
     * based on startIndex passed in. If startIndex passed in is 4, then looking
     * for sequence number, if it is 8, then looking for ack number.
     * @param packetLoad - packet as a byte array
     * @param startIndex - index of the bytes to read (ack index or seq index)
     * @return ack or sequence number of a packet
     */
	private static int getACKOrSeq(byte[] packetLoad, int startIndex){
		byte[] intBytes = new byte[4];
		for(int i = startIndex; i < startIndex + 4; i++){
			intBytes[i - startIndex] = packetLoad[i]; 
		}
		return ByteBuffer.wrap(intBytes).getInt();
	}
	

    /**
     * getPacketLoad
     * <p>
     * Returns the packet (with headers and optional data) as a byte array
     * @return packetLoad - packet as a byte array
     */	
	public byte[] getPacketLoad(){
		return packetLoad;
	}
	
	/**
     * getCheckSum
     * <p>
     * Returns the packet's checksum as a byte array
     * @return checkSum - checksum as a byte array
     */
	public byte[] getCheckSum(){
		return checkSum;
	}
}
