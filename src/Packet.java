import java.nio.ByteBuffer;


/**
 * Packet payload - Byte Array Structure:
 * Index = Contents
 * 0-1 = sourcePortNumber
 * 2-3 = destPortNumber
 * 4-7 = sequenceNumber
 * 8-11 = ACK #
 * 12 = purpose code 
 * 13-19 = TCP CheckSum
 * 
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
	
	private long checkSum;
	
	public Packet(int sourcePortNumber, int destPortNumber, int sequenceNumber, 
				  int ackNumber, long checkSum, String purposeCode, byte[] data){
		this.sourcePortNumber = sourcePortNumber;
		this.destPortNumber = destPortNumber;
		this.sequenceNumber = sequenceNumber;
		this.ackNumber = ackNumber;
		this.checkSum = checkSum;
		this.purposeCode = purposeCode;
		this.data = data;
		constructPacket();
		calculateCheckSum();
		finalizePacket();
	}
	
	public byte[] getPacketLoad(){
		return packetLoad;
	}
	
	public long getCheckSum(){
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
		packetLoad[12] = Byte.parseByte("00000000", 2);
	
	}
	
	private void calculateCheckSum(){
		//Create byte array of everything without checksum
		//MD5 stuff on this new array
		//Set checkSum = stuff
	}
	
	private void finalizePacket(){
		//Add checkSum to packet
	}
}
