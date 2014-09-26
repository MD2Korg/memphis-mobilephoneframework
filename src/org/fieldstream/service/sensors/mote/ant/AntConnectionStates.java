//Copyright (c) 2010, University of Memphis
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
//    * Neither the name of the University of Memphis nor the names of its contributors may be used to 
//      endorse or promote products derived from this software without specific prior written permission.
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

//@author Somnath Mitra
//@author Andrew Raij


package org.fieldstream.service.sensors.mote.ant;


/*
 * This class abstracts bluetooth states and modes.
 * <br> Modes represent local states, such as CONNECT / DISCONNECT
 * <br> phases of the power cycle state.
 * <p> The Bluetooth connection can be in different states such as
 * <br> DUTY CYCLE - The phone and the radio turn on their bt radios
 *      after a fixed interval to transmit data
 * <br> NO DUTY CYCLE (CONTINOUS) 
 * <br> OUT OF RANGE - The bluetooth bridge node is out of range
 * <br> DEAD TIME - The participant has explicitly asked to switch off 
 *      data collection
 */
public class AntConnectionStates {
	
	public static final int ANT_STATE_NEWCONNECTION = 1;
	public static final int ANT_WAIT_FOR_REPLY = 2;
//	public static final int ANT_STATE_DISCONNECTED = 3;

	public static final long ANT_CONNECTION_TIMEOUT_MILLIS = 30000;
	public static final long ANT_CHECK_TIME_DEFAULT = 1000;
		
//	private static int mAntCurrentState;
//	private static long mAntLastDataReceivedTime;
//	private static boolean mReceived;
	
/*	public static long getLast_Data_Received_Time() {
		return mAntLastDataReceivedTime;
	}
	public static void setLast_Data_Received_Time(long currentTime) {
		mAntLastDataReceivedTime=currentTime;
	}

	public static int getCurrent_Ant_State() {
		return mAntCurrentState;
	}

	public static void setCurrent_Ant_State(int antCurrentState) {
		mAntCurrentState = antCurrentState;
	}
	public static boolean isReceived() {
		return mReceived;
	}
	public static void setIsReceived(boolean received) {
		mReceived=received;
	}
*/
}
