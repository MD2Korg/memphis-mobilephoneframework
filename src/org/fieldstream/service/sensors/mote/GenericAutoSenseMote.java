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
package org.fieldstream.service.sensors.mote;

import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.api.AbstractMote;
import org.fieldstream.service.sensors.mote.ant.AntStateManager;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;


public class GenericAutoSenseMote extends AbstractMote implements MoteReceiverInterface {
	/*
	 * the standard log tag
	 */
	public static final int SYNC_TIME=5000;
	public String TAG = "GenericAutoSenseMote";
	/*
	 * The number of physical sensors on this mote
	 */
//	private AntStateManagerRIPECG antStateManagerRIPECG = null;
//	private AntStateManagerAlcohol antStateManagerAlcohol = null;
//	private AntStateManager antStateManager = null;

	public int NumberOfMoteSensors;
	public int[][] sensorMap;
	private HashMap<Integer, Integer> sensorIndex;
	private int sensorCount;
	private int [] sampleCount;
	private int []seqNumber;
//	public static GenericAutoSenseMote INSTANCE=null;
	public GenericAutoSenseMote(int moteType)
	{
		super(moteType);
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM)_Constructor (moteType="+moteType);
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (GASM)_Constructor (moteType="+moteType, System.currentTimeMillis());}
		if(Log.DEBUG)
		{
			Log.d(TAG,"GenericAutoSenseMote of type = "+moteType);
		}
		initialize();
	}
	public void initialize() {

		try {
	//		buffer = new TOSOscopeIntPacket[bufferLength];
			Log.h(TAG, "GenericAutoSenseMote_initialize MoteType="+MoteType);
			switch(MoteType)
			{
			case Constants.MOTE_TYPE_AUTOSENSE_1_ECG:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_ECG;
				break;

			case Constants.MOTE_TYPE_AUTOSENSE_1_RIP:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_RIP;
				break;

			case Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ALCOHOL;
				break;

			case Constants.MOTE_TYPE_AUTOSENSE_2_NINE_AXIS:
				NumberOfMoteSensors = Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_NINE_AXIS;
				break;

			}

			sampleCount = new int [NumberOfMoteSensors];
			seqNumber = new int [NumberOfMoteSensors];
			// init the sensormap
			sensorMap = ChannelToSensorMapping.getChannelToSensorMap(MoteType);


			// create the hashmap to remember which sensor is in which index
			sensorIndex = new HashMap<Integer, Integer>();
			sensorCount = 0;

			for(int i=0;i < NumberOfMoteSensors; i++)
			{

				sensorIndex.put(sensorMap[i][0], sensorCount++ );
				sampleCount[i]=0;
				seqNumber[i]=-1;
				if(Log.DEBUG)
				{
					Log.d(TAG,"sensorMap[i][0] = "+sensorMap[i][0]);
					Log.d(TAG,"sensorCount = "+(sensorCount - 1));
					Log.d(TAG,"sensorIndex = "+sensorIndex.get(sensorMap[i][0]));
				}

			}

			if(Log.DEBUG)
			{
				Log.d(TAG+".initialize()",Constants.getMoteDescription(MoteType)+"initialized" );
			}
		} // end try
		catch(Exception e)
		{
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","Exception\t: (GASM)_Initialize() ("+e.getLocalizedMessage()+")");
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "Exception\t: (GASM)_Initialize() ("+e.getLocalizedMessage()+")",System.currentTimeMillis());}
		}

	}


	boolean isMyPacket(int chanID)
	{
		boolean myPacket = false;

			// use the channel number to decide
			int moteType = ChannelToSensorMapping.getMoteTypeAutosense2(chanID);
			if(Log.DEBUG)
			{
				Log.d(TAG," Type = "+MoteType+" moteType = "+moteType+"Chan ID = "+chanID);
			}
			if(moteType == MoteType)
				myPacket = true;
		return myPacket;
	}
	public void onReceiveMotePacket(TOSOscopeIntPacket toip) {
		Log.d(TAG, "onReceiveMotePacket Called");
		synchronized(INSTANCE) {
			// mote id
//			int moteID = toip.getMoteID();

			//channel id
			int chanID = toip.getChan();

			if(isMyPacket( chanID)) {
				if(Log.DEBUG) {
					Log.d(TAG,"MyPacket is true");
				}
				// this packet actually belongs to this mote
				// so start processing
				int index = sensorIndex.get(chanID);
				// get the sensor id
				int sensorID = sensorMap[index][1];
				// the data
				int[] data = toip.getData();
				long currentTime=toip.getReceivedTimeStamp();
//				if(isMissingOnSequenceNumber(sensorID,diffSampleNo , currentTime, sampleCount[index]+data.length)){
//					onMissingSamplesRequest(sensorID,diffSampleNo);
//				}
//					onMissingSampleRequest(sensorID);
				long currentEstimatedTime=calculateEstimatedCurrentTime(sensorID, sampleCount[index]+data.length);

//				while(currentTime-currentEstimatedTime>SYNC_TIME){
//					if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM)_onReceiveMotePacket() add missing.................");

//					int numberOfSamples=TimeStamping.calculateNumberOfSamples(sensorID, SYNC_TIME);
//					if (Log.DEBUG) Log.d("Monowar_GASM","Missing-------------Sensor="+sensorID+"Mote="+moteID+"ChanID="+chanID+" Time Difference="+(currentTime-currentEstimatedTime)/1000+"(S) MissingSampleNo="+numberOfSamples);
//					onMissingSamplesRequest(sensorID,numberOfSamples);
//				}
				if(currentTime-currentEstimatedTime<0){
					//if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM)_onReceiveMotePacket() time backward currenttime="+currentTime+" CurEstTime="+currentEstimatedTime+" datalength="+data.length+" Diff"+(-currentTime+currentEstimatedTime)+".................");
//					if (Log.DEBUG) Log.d("Monowar_GASM","Time Backword###################Sensor="+sensorID+"Mote="+moteID+"ChanID="+chanID+" Time Difference="+(-currentTime+currentEstimatedTime)/1000+"(S)");
				}
				samplesPrepareAndSend(chanID,sensorID, data);
//				seqNumber[index]=lastSeqNo;
			} // if my packet
		}// end synchronized
	} // method end
	public boolean isMissingOnSequenceNumber(int sensorID, int diffSampleNo, long currentTime, int totalSampleNo)
	{
		if(diffSampleNo<=0) return false;
		long currentEstimatedTime=calculateEstimatedCurrentTime(sensorID,totalSampleNo+diffSampleNo);
		if(currentTime>=currentEstimatedTime)
			return true;
		else return false;
	}
	public void samplesPrepareAndSend(int chanID, int sensorID, int []data)
	{
		long []timestamps=new long[data.length];
		int index = sensorIndex.get(chanID);
		long startSampleTime=MoteDeviceManager.startSampleTime;

		timestamps=TimeStamping.timestampCalculator(startSampleTime,sampleCount[index], sensorID, data.length);

		sampleCount[index]+=data.length;
//		if (Log.MONOWAR) Log.d("Monowar_GASM","Sensor="+sensorID+" DataLength="+data.length+" SampleCount="+sampleCount[index]+" StartTime="+timestamps[0]+" EndTime="+timestamps[data.length-1]);
//		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM_here.....MSM.....");
		MoteSensorManager.getInstance().updateSensor(data, sensorID, timestamps);
	}

	public void onMissingSamplesRequest(int sensorID, int numberOfSamples) {
//			if (Log.DEBUG) Log.d("Monowar_GASM","Missing--------- sensorID="+sensorID+" Samples="+numberOfSamples );
				int missingToken = PacketLoss.getMissingToken(sensorID);
				int []data;
				int chanID = ChannelToSensorMapping.mapPhoneSensorToMoteChannel(sensorID);
				if(isMyPacket(chanID)){
//					if (Log.DEBUG) Log.d("Monowar_GASM","Missing ------insert----- Start-----. sensorID="+sensorID+" Samples="+numberOfSamples );

					data=generateMissingSamples(numberOfSamples,missingToken);
//					if (Log.DEBUG) Log.d("Monowar_GASM","Missing ------insert-----middle-----. sensorID="+sensorID+" Samples="+numberOfSamples );

					samplesPrepareAndSend(chanID, sensorID, data);
					if (Log.DEBUG) Log.d("Monowar_GASM","Missing----------End-------. sensorID="+sensorID+" Samples="+numberOfSamples );
				}
	}

	public int[] generateMissingSamples(int samplecount, int missingToken)
	{
		int data[]=new int[samplecount];
		for(int i=0;i<samplecount;i++)
			data[i]=missingToken;
		return data;
	}

	public long calculateEstimatedCurrentTime(int sensorid,int samplecount)
	{
		long curEstTime=-1;
		long startSampleTime=MoteDeviceManager.startSampleTime;
		int windowSize=TimeStamping.getWindowSize(sensorid);
		curEstTime=startSampleTime+(long)((long)60*(long)1000*(long)samplecount)/windowSize;
		return curEstTime;

	}


	public void activate() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM)_activate() moteType"+MoteType);
		MoteDeviceManager.getInstance().subscribe(this);
		AntStateManager.getInstance();
//		if(MoteType==Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP && Constants.moteAddress[0]!="")
//			antStateManagerRIPECG = new AntStateManagerRIPECG(Constants.moteAddress[0], Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP);
//		else if(MoteType==Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL && Constants.moteAddress[1]!="")
//			antStateManagerAlcohol = new AntStateManagerAlcohol(Constants.moteAddress[1], Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL);

	}

	@Override
	public void deactivate() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM)_deactivate() moteType"+MoteType);
		MoteDeviceManager.getInstance().unsubscribe(this);
		try{
		if(AntStateManager.isInstance()!=null){
			AntStateManager antStateManager=AntStateManager.getInstance();
			antStateManager.stopDown();
			antStateManager.kill();
			antStateManager=null;
		}
		}catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","Exception\t: (GASM)_deactivate() moteType"+MoteType);

		}
/*		if(antStateManagerRIPECG!=null){
			antStateManagerRIPECG.stopDown();
			antStateManagerRIPECG.kill();
			antStateManagerRIPECG=null;
		}
		if(antStateManagerAlcohol!=null){
			antStateManagerAlcohol.stopDown();
			antStateManagerAlcohol.kill();
			antStateManagerAlcohol=null;
		}
*/
	}

	@Override
	public void sendCommand(int command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendCommand(String command) {
		// TODO Auto-generated method stub

	}


	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (GASM)_finalize() ");
//		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (GASM)_finalize()", System.currentTimeMillis());}
	}

}
