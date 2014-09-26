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
package org.fieldstream.service.sensor;

import java.util.ArrayList;
import java.util.Calendar;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.virtual.RipQualityCalculation;

import android.os.Handler;


/**
 * Singleton class that relays all sensor update to the subscribers
 * @author Patrick Blitz
 * @author Andrew Raij
 * @author Mahbub Rahman
 */

public class SensorBus {

	//following two static variables are for debugging and cut out after debug
	//	public static String sensorBusDataDump="";
	//	public static String sensorBusTimestampDump="";

	private ArrayList<SensorBusSubscriber> subscribers;
	private static SensorBus INSTANCE;
	RipQualityCalculation calculateQualityRIP;
	int []dataQ;
	long[]timestampsQ;
	int sensorIDQ;
//	Handler dataQHandler;

	public static SensorBus getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new SensorBus();
		}
		return INSTANCE;
	}

	protected void finalize() {
		Log.d("SensorBus", "Garbage Collected");
		//		try {
		//			file.close();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}

	private SensorBus() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (SensorBus)_Constructor()");
		calculateQualityRIP=new RipQualityCalculation();
//		dataQHandler = new Handler();

		subscribers = new ArrayList<SensorBusSubscriber>();
	}

	/**
	 *
	 * @param subscriber
	 */

	public void subscribe(SensorBusSubscriber subscriber) {
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);

		}

	}

	/**
	 * unsubscribe from this bus
	 * @param subscriber
	 */

	public void unsubscribe(SensorBusSubscriber subscriber) {
		if (subscribers.contains(subscriber)) {
			subscribers.remove(subscriber);
		}
	}
	/**
	 * propagates a new buffer of values to the subscribers.
	 * In the case of sliding windows, only some of this data is actually new
	 * @param sensorID
	 * @param data
	 * @param timestamps
	 * @param startNewData The starting index of the new data
	 * @param endNewData The ending index of the new data
	 */
//	private Runnable DataQualityThread = new Runnable() {
//		public void run() {
//			// disable all interview runnables
//			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","DQ: inside running ");
//
//			Calendar cal = Calendar.getInstance();
//			cal.set(Calendar.HOUR_OF_DAY, 0);
//			cal.set(Calendar.MINUTE, 0);
//			cal.set(Calendar.SECOND, 0);
//			cal.set(Calendar.MILLISECOND, 0);
//			long startTime = cal.getTimeInMillis();
//			int valid_minutes;
//			int dataQualityGood = 0, dataQualityLoose=0, dataQualityNoise=0, dataQualityOff=0;
//			int quality;
//			String text="";
//			DatabaseLogger db;
//			db = DatabaseLogger.getInstance(this);
//			int []buffer=new int[64];
//			for(int i=0;i+64<=dataQ.length;i+=64){
//				for(int j=0;j<64;j++){
//					buffer[j]=dataQ[i+j];
//				}
//				quality=calculateQualityRIP.currentQuality(buffer);
//
//				if(quality==RipQualityCalculation.DATA_QUALITY_GOOD) dataQualityGood++;
//				if(quality==RipQualityCalculation.DATA_QUALITY_BAND_LOOSE) dataQualityLoose++;
//				if(quality==RipQualityCalculation.DATA_QUALITY_NOISE) dataQualityNoise++;
//				if(quality==RipQualityCalculation.DATA_QUALITY_BAND_OFF) dataQualityOff++;
//			}
//			text="Good="+Integer.toString(dataQualityGood)
//					+", Noise="+Integer.toString(dataQualityNoise)
//					+", Loose="+Integer.toString(dataQualityLoose)
//					+", Off="+Integer.toString(dataQualityOff);
//
//			if(dataQualityGood>=13) //66% of 20 window
//				valid_minutes=1;
//			else
//				valid_minutes=0;
//			int totalMinuteInc=0;
//			if((dataQualityGood+dataQualityLoose+dataQualityNoise)>=13)
//				totalMinuteInc++;
//			db.logAnything("dataquality"+Integer.toString(sensorIDQ),text , System.currentTimeMillis());
//			db.logValidData(sensorIDQ, startTime, totalMinuteInc, valid_minutes);
//			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","DQ: RIP: "+text);
//		}
//	};
	public void receiveBuffer(int sensorID, int[] data, long[] timestamps, int startNewData, int endNewData) {
		if (Log.DEBUG) {
			Long t = data.length > 0 ? timestamps[0] : 0;
			Log.d("SensorBus", "Got a " + data.length + " sample buffer of " + Constants.getSensorDescription(sensorID) + " with timestamp " + t);
		}

		/*
		 * 	Data Qualilty Measurements
		 *
		 * */
		// Hillol: No need to do this : as SensorDataBufferSingleton and SensorDataQualitySingleton is doing the same thing in 3 sec window
//		if(sensorID==Constants.SENSOR_RIP){
//			dataQ=new int[data.length];
//			timestampsQ=new long [timestamps.length];
//			for(int i=0;i<data.length;i++){
//				dataQ[i]=data[i];
//				timestampsQ[i]=timestamps[i];
//			}
//			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","DQ: going to start");
//			sensorIDQ=sensorID;
//			dataQHandler.post(DataQualityThread);
//
//		}

		if (subscribers!= null) {
			for (int i=0;  i<subscribers.size(); i++) {
				//				Log.d("SensorBus", "sending buffer to " + subscribers.get(i).toString());
				subscribers.get(i).receiveBuffer(sensorID, data, timestamps, startNewData, endNewData);
			}
		}

	}
}
