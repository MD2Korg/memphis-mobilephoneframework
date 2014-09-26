package org.fieldstream.service.sensors.mote.tinyos;

public class TOSLinkBytePacket {
	
	// all ranges inclusive
	// 114 bytes total
	byte junk[]; 		// 0-5
	
	
	public static final int JUNK_BYTE_SIZE = 6;
	
	int currentPosition;
	int junkPosition;
	
	long downTime;
	
	
	boolean linkDown;
	
	/*
	 * A variable to hold the current status of the packet and its delivery 
	 * to the framework
	 */
	int packetStatus;
	
	
	public byte[] getJunk() 
	{
		return junk;
	}
	public void setJunk(byte[] junk) 
	{
		this.junk = junk;
	}
	
	public void setJunkByteAtPosition(int position, byte dataByte)
	{
		if(position < TOSLinkBytePacket.JUNK_BYTE_SIZE)
			this.junk[position] = dataByte;
	}
	
	public byte getJunkByteAtPosition(int position)
	{
		byte data = -1;
		if(position < TOSLinkBytePacket.JUNK_BYTE_SIZE)
			data = this.junk[position];
		return data;
	}
	
	public void setNextJunkByte(byte dataByte)
	{
		if(junkPosition < TOSLinkBytePacket.JUNK_BYTE_SIZE)
			junk[++junkPosition] = dataByte;
	}
	
	
	
	public TOSLinkBytePacket()
	{
		junk = new byte[TOSLinkBytePacket.JUNK_BYTE_SIZE];
		
		currentPosition = 0;
		junkPosition = 0;
	
	}
	
	public long getDownTime() {
		return downTime;
	}
	
	public void setDownTime(long downTime) {
		this.downTime = downTime;
	}
	public boolean isLinkDown() {
		return linkDown;
	}
	public void setLinkDown(boolean linkDown) {
		this.linkDown = linkDown;
	}
	
	
}
