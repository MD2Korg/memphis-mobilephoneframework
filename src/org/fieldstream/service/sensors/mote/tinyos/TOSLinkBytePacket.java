//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided
//that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and
//      the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
//      and the following disclaimer in the documentation and/or other materials provided with the
//      distribution.
//    * Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its
//      contributors may be used to endorse or promote products derived from this software without specific
//      prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
//WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
//PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
//ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
//TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//POSSIBILITY OF SUCH DAMAGE.
//
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
