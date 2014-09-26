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

import org.fieldstream.Constants;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.ant.AntConnection.ChannelStates;
import org.fieldstream.utility.CommonUtil;


public class AntStateManager extends Thread {

//	private static AntStateManager INSTANCE = null;
	public static final boolean CONNECTED=true;
	public static final boolean NOT_CONNECTED=false;

	private static String TAG = "AntStateManager";
	public static boolean isReceived[]=new boolean [Constants.MOTE_NO];
	public static long moteLastTryToReconnectTime[] = new long[Constants.MOTE_NO];
	private AntConnection antConnection=null;
	static AntStateManager INSTANCE=null;
//	AntConnectionAlcohol antConnectionAlcohol=null;


	DatabaseLogger db;


	/*
	 * A reader variable to do the byte level reading.
	 * It reads from the byte queue to produce tinyos packets.
	 * There should be just one instance hence a static variable.
	 */

	private Packetizer reader = null;

	/*
	 * The address of the
	 */
	//private long lastReceivedPacketTime = -1;

	private volatile boolean keepAlive = true;
	public static AntStateManager isInstance()
	{
		return INSTANCE;
	}
	public static AntStateManager getInstance()
	{
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_getInstance()");

		if(INSTANCE == null)
		{
			INSTANCE = new AntStateManager();
			String msg = "AntStateManager Created";
			if (Log.DEBUG) Log.d(TAG,msg);

		}
		return INSTANCE;
	}

	public AntStateManager()
	{
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_Constructor()");
		for(int i=0; i<this.isReceived.length; i++) {
			this.isReceived[i] = NOT_CONNECTED;
		}

		db = DatabaseLogger.getInstance(this);
		if(antConnection==null){
			antConnection=new AntConnection();
			reader=Packetizer.getInstance();

			antConnection.start();
		}
		setName("fs_ANTStateManager_"+System.currentTimeMillis());
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (AntStateManager)_Constructor()",System.currentTimeMillis());}
		start();
	}

	/*
	 * This function is to stop receiving data from the Ant bridge.
	 * But this function is problematic. This should not be a problem for
	 * now, as stopping is not used that often.
	 */
	public void stopDown(){
		try
		{
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_StopDown()_1");
			antConnection.shutDown();
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_StopDown()_2");
		}catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (AntStateManager)_StopDown()_1 ("+e.getLocalizedMessage()+")");

		}
		try{

			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_StopDown()_reader"+reader.toString());
			if(reader!=null){
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_StopDown()_inside");
				reader.kill();
			}
		}catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (AntStateManager)_StopDown()_2 ("+e.getLocalizedMessage()+")");
		}
		reader=null;
		antConnection=null;
		INSTANCE=null;
	}

	public void doStateChange()
	{
		long curTime=System.currentTimeMillis();
//		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_doStateChange_"+antConnection.getRIPECGState()+" "+antConnection.getALCOHOLState());
//		if (Log.DEBUG) Log.d("Monowar","Current State: "+curState);

		//***********************************************
		//Constants.moteActive[Constants.MOTE_RIPECG_IND] = false;
		Constants.moteActive[Constants.MOTE_ALCOHOL_IND] = false; // Hillol: As alcohol is discontinued we are disabling this mote.
		//Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND] = false;
		//Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND] = false;
		//************************************************
		if(Constants.moteActive[Constants.MOTE_RIPECG_IND]){
			//if(antConnection.getRIPECGState()!= ChannelStates.TRACKING_DATA) {
			//	Log.h(TAG, "Chest State: "+ antConnection.getRIPECGState().toString());
			//}
			switch(antConnection.getRIPECGState())
			{
				case OFFLINE:
				case CLOSED:
				case PENDING_OPEN:
					antConnection.openChannel(AntConnection.MOTE_RIPECG_CHANNEL, false);
					break;

			}
		}
		if(Constants.moteActive[Constants.MOTE_ALCOHOL_IND]){
			//if(antConnection.getALCOHOLState()!= ChannelStates.TRACKING_DATA) {
			//	Log.h(TAG, "Alcohol State: "+ antConnection.getALCOHOLState().toString());
			//}
			switch(antConnection.getALCOHOLState())
			{
				case OFFLINE:
				case CLOSED:
				case PENDING_OPEN:
					antConnection.openChannel(AntConnection.MOTE_ALCOHOL_CHANNEL, false);
					break;
			}
		}

		if(Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]){
			//if(antConnection.getALCOHOLAcclRightState()!= ChannelStates.TRACKING_DATA) {
			//	Log.h(TAG, "Right State: "+ antConnection.getALCOHOLAcclRightState().toString());
			//}
			if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND)+30*1000) {
				if(curTime - AntStateManager.moteLastTryToReconnectTime[AntConnection.MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL] > 10*1000) {
					// More than 30 second missing means offline
					Log.h(TAG, "No data from Right Mote for the last 30 second. Assume offline.");
					switch (antConnection.getALCOHOLAcclRightState()) {
						case TRACKING_DATA:
						case SEARCHING:
							antConnection.setALCOHOLAcclRightState(ChannelStates.OFFLINE);
							break;
						default:
							break;
					}
				}
			}
			switch(antConnection.getALCOHOLAcclRightState()) {
				case OFFLINE:
				case CLOSED:
				case PENDING_OPEN:
					AntStateManager.moteLastTryToReconnectTime[AntConnection.MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL] = curTime;
					antConnection.openChannel(AntConnection.MOTE_ALCOHOL_ACCL_RIGHT_CHANNEL, false);
					break;
				case SEARCHING:
				case TRACKING_DATA:
				case TRACKING_STATUS:
					break;
				default:
					break;
			}
		}

		if(Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]){
			//if(antConnection.getALCOHOLAcclLeftState()!= ChannelStates.TRACKING_DATA) {
			//	Log.h(TAG, "Left State: "+ antConnection.getALCOHOLAcclLeftState().toString());
			//}
			if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_ALCOHOL_ACCL_LEFT_IND)+30*1000) {
				if(curTime - AntStateManager.moteLastTryToReconnectTime[AntConnection.MOTE_ALCOHOL_ACCL_LEFT_CHANNEL] > 10*1000) {
					// More than 30 second missing means offline
					Log.h(TAG, "No data from Left Mote for the last 30 second. Assume offline.");
					switch (antConnection.getALCOHOLAcclLeftState()) {
						case TRACKING_DATA:
						case SEARCHING:
							antConnection.setALCOHOLAcclLeftState(ChannelStates.OFFLINE);
							break;
						default:
							break;
					}
				}
			}
			switch(antConnection.getALCOHOLAcclLeftState()) {
				case OFFLINE:
				case CLOSED:
				case PENDING_OPEN:
					AntStateManager.moteLastTryToReconnectTime[AntConnection.MOTE_ALCOHOL_ACCL_LEFT_CHANNEL] = curTime;
					antConnection.openChannel(AntConnection.MOTE_ALCOHOL_ACCL_LEFT_CHANNEL, false);
					break;
				case SEARCHING:
				case TRACKING_DATA:
				case TRACKING_STATUS:
					break;
				default:
					break;
			}

		}




		if(Constants.moteActive[Constants.MOTE_NINE_AXIS_RIGHT_IND]){

			if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_NINE_AXIS_RIGHT_IND)+30*1000) {
				if(curTime - AntStateManager.moteLastTryToReconnectTime[Constants.MOTE_NINE_AXIS_RIGHT_IND] > 10*1000) {
					// More than 30 second missing means offline
					Log.h(TAG, "No data from Right Nine Axis Mote for the last 30 second. Assume offline.");
					switch (antConnection.getNineAxisRightState()) {
						case TRACKING_DATA:
						case SEARCHING:
							antConnection.setNineAxisRightState(ChannelStates.OFFLINE);
							break;
						default:
							break;
					}
				}
			}
			switch(antConnection.getNineAxisRightState()) {
				case OFFLINE:
				case CLOSED:
				case PENDING_OPEN:
					AntStateManager.moteLastTryToReconnectTime[Constants.MOTE_NINE_AXIS_RIGHT_IND] = curTime;
					antConnection.openChannel(AntConnection.MOTE_NINE_AXIS_RIGHT_CHANNEL, false);
					break;
				case SEARCHING:
				case TRACKING_DATA:
				case TRACKING_STATUS:
					break;
				default:
					break;
			}

		}
		if(Constants.moteActive[Constants.MOTE_NINE_AXIS_LEFT_IND]){

			if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_NINE_AXIS_LEFT_IND)+30*1000) {
				if(curTime - AntStateManager.moteLastTryToReconnectTime[Constants.MOTE_NINE_AXIS_LEFT_IND] > 10*1000) {
					// More than 30 second missing means offline
					Log.h(TAG, "No data from Left Nine Axis Mote for the last 30 second. Assume offline.");
					switch (antConnection.getNineAxisLeftState()) {
						case TRACKING_DATA:
						case SEARCHING:
							antConnection.setNineAxisLeftState(ChannelStates.OFFLINE);
							break;
						default:
							break;
					}
				}
			}
			switch(antConnection.getNineAxisLeftState()) {
				case OFFLINE:
				case CLOSED:
				case PENDING_OPEN:
					AntStateManager.moteLastTryToReconnectTime[Constants.MOTE_NINE_AXIS_LEFT_IND] = curTime;
					antConnection.openChannel(AntConnection.MOTE_NINE_AXIS_LEFT_CHANNEL, false);
					break;
				case SEARCHING:
				case TRACKING_DATA:
				case TRACKING_STATUS:
					break;
				default:
					break;
			}

		}


		if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_RIPECG_IND)+1000){
			isReceived[Constants.MOTE_RIPECG_IND]=NOT_CONNECTED;
		}
		else isReceived[Constants.MOTE_RIPECG_IND]=CONNECTED;
		if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_ALCOHOL_IND)+1000){
			isReceived[Constants.MOTE_ALCOHOL_IND]=NOT_CONNECTED;
		}
		else isReceived[Constants.MOTE_ALCOHOL_IND]=CONNECTED;

		if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND)+1000) {
			isReceived[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]=NOT_CONNECTED;
		}
		else {
			isReceived[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]=CONNECTED;
		}
		if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_ALCOHOL_ACCL_LEFT_IND)+1000) {
			isReceived[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]=NOT_CONNECTED;
		}
		else {
			isReceived[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]=CONNECTED;
		}

		if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_NINE_AXIS_RIGHT_IND)+1000) {
			isReceived[Constants.MOTE_NINE_AXIS_RIGHT_IND]=NOT_CONNECTED;
		}
		else {
			isReceived[Constants.MOTE_NINE_AXIS_RIGHT_IND]=CONNECTED;
		}
		if(curTime> AntConnection.getLast_Data_Received_Time(Constants.MOTE_NINE_AXIS_LEFT_IND)+1000) {
			isReceived[Constants.MOTE_NINE_AXIS_LEFT_IND]=NOT_CONNECTED;
		}
		else {
			isReceived[Constants.MOTE_NINE_AXIS_LEFT_IND]=CONNECTED;
		}
	}
	public void run()
	{
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_run() Start");
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (AntStateManager)_run() Start",System.currentTimeMillis());}

		long curTime=System.currentTimeMillis();
		for(int i=0; i<Constants.MOTE_NO; i++) {
			AntStateManager.moteLastTryToReconnectTime[i] = 0;//curTime;
		}
		while(keepAlive)
		{
			try
			{
				doStateChange();
				sleep(AntConnectionStates.ANT_CHECK_TIME_DEFAULT);
			} // end try
			catch(Exception e)
			{
				String TAG = "AntStateManager";
				String msg = "Exception while sleeping";
				if (Log.DEBUG) Log.d(TAG,msg);
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (AntStateManager)_run() Exception_while_sleeping("+e.getLocalizedMessage()+")");
				Log.h(TAG, CommonUtil.getExceptionStackTrace(e));
			} // end catch
		}
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (AntStateManager)_run() Terminate");
	}

	public void kill()
	{
		try{
			keepAlive  = false;
			interrupt();
		}
		catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","Exception\t: (AntStateManager)_kill()"+e.getLocalizedMessage());

		}
	}
}
