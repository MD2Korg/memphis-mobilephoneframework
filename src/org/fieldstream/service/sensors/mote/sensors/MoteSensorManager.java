﻿//Copyright (c) 2010, University of Memphis
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

// @author Somnath Mitra


package org.fieldstream.service.sensors.mote.sensors;

import java.util.ArrayList;

import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.ChannelToSensorMapping;
import org.fieldstream.service.sensors.mote.TimeStamping;

/*
 * The mote sensor manager does the following things:
 * <br> Starts/Stops a bluetooth connection that reads data from
 * 		a AutoSense BlueTooth Bridge node into a Byte Level Queue
 * <br> Starts a reader that reads bytes from the Byte Level queue
 * 		and tries to parse those into tinyos oscope (Stress Project specific) packets
 * <br> Makes an instance of itself.
 * <br>
 * There  should only be one instance of such a manager.
 * @author mitra
 */
public class MoteSensorManager {

	/*
	 * There will only be one Sensor Manager
	 * that keeps talking to one bridge, hence
	 * there should only one INSTANCE of such a manager.
	 */
	private static MoteSensorManager INSTANCE;

	final String TAG = "MoteSensorManager";

	//	private  NotificationManager nm;
	//	private boolean led=true;

	public ArrayList<MoteUpdateSubscriber> moteUpdateSubsribers;

	//	private Notification notif;

	public MoteSensorManager() {

		moteUpdateSubsribers = new ArrayList<MoteUpdateSubscriber>();
		//		notif = new Notification();
		//		notif.flags = Notification.FLAG_SHOW_LIGHTS;
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (MSM)_Constructor");
	}
	public static MoteSensorManager getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new MoteSensorManager();
			String TAG = "MoteSensorManager.getInstance";
			if (Log.DEBUG) Log.d(TAG, "created New");
		}
		return INSTANCE;
	}

	public void registerListener(MoteUpdateSubscriber subscriber)
	{
		Log.ema_alarm("MoteSensorManager", "MUS: " + subscriber.toString());
		moteUpdateSubsribers.add(subscriber);
	}

	public void unregisterListener(MoteUpdateSubscriber subscriber)
	{
		moteUpdateSubsribers.remove(subscriber);
		Log.d("MoteSensorManager", "removed MUS: " + subscriber.toString());
	}


	public void updateSensor(int[] data, int SensorID, long[] timestamps)
	{
		if (SensorID == -1) {
			Log.d("updateSensor", "Packet from sensor " + SensorID + " ignored");
			return;
		}
		//Log.h(TAG, "SensorID = "+SensorID+", data=" + data.length + ", timestamps="+timestamps.length);

		//SensorDataBufferSingleton.getInstance().pushData(SensorID, data, timestamps);
		//        if (led) {
		//       	if (SensorID == Constants.SENSOR_ECK)   // ECG mote
		//       		notif.ledARGB = 0xFFFF0000;
		//           else if (SensorID == Constants.SENSOR_ALCOHOL || SensorID == Constants.SENSOR_GSRWRIST || SensorID == Constants.SENSOR_TEMPWRIST)					// RIP mote
		//           	notif.ledARGB = 0xFF0000FF;

		//       	notif.ledOnMS = 1;
		//       } else {
		//       	notif.ledARGB = 0x00000000;
		//       	notif.ledOnMS = 0;
		//       }
		//       led=!led;
		//       notif.ledOffMS = 0;
		//        if (nm == null) {
		//        	nm=( NotificationManager ) InferrenceService.INSTANCE.getSystemService( InferrenceService.NOTIFICATION_SERVICE );
		//        }
		//        nm.notify(1, notif);

		if(Log.DEBUG)
		{
			String TAG = "MoteSensorManager";
			String sendingTo = "sending To " + SensorID;
			Log.d(TAG, sendingTo);
		}
		try{
			for(MoteUpdateSubscriber item : moteUpdateSubsribers)
			{
				item.onReceiveData(SensorID, data , timestamps);
			}
		}catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (MSM)_UpdateSensor "+e.toString()+" "+e.getLocalizedMessage()+e.getMessage()+")");
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG","EXCEPTION\t: (MSM)_UpdateSensor "+e.toString()+" "+e.getLocalizedMessage()+" "+e.getMessage()+")" , System.currentTimeMillis());}
		}
	}
	///////////////// New "UpdateSensor" Start//////////////////

	public void updateSensor(int[] data, int moteID, int ChannelID , long timestamp)
	{

		/*        if (led) {
        	if (moteID % 10 == 1)   // ECG mote
        		notif.ledARGB = 0xFFFF0000;
        	else					// RIP mote
        		notif.ledARGB = 0xFF0000FF;

        	notif.ledOnMS = 1;
        } else {
        	notif.ledARGB = 0x00000000;
        	notif.ledOnMS = 0;
        }
        led=!led;
        notif.ledOffMS = 0;
        if (nm == null) {
        	nm=( NotificationManager ) InferrenceService.INSTANCE.getSystemService( InferrenceService.NOTIFICATION_SERVICE );
        }
        nm.notify(1, notif);
		 */
		for(MoteUpdateSubscriber item : moteUpdateSubsribers)
		{
			int SensorID = ChannelToSensorMapping.mapMoteChannelToPhoneSensor(moteID, ChannelID);
			//				String TAG = "updateSensor";
			//				String sendingTo = "sending To " + item.toString();
			//				Log.d(TAG, sendingTo);

			long[] timeStamps = null;
			timeStamps = TimeStamping.timestampCalculator(timestamp, SensorID);

			if (SensorID != -1) {
				item.onReceiveData(SensorID, data , timeStamps);
			}
			else {
				Log.d("updateSensor", "Packet from mote " + moteID + " ignored");
			}
		}
		return;
	}
	///////////////// New "UpdateSensor" end//////////////////
	public int isSensorActive(int ChannelID)
	{
		int position = -1;

		for(MoteUpdateSubscriber item: moteUpdateSubsribers)
		{
			position++;
			if(item != null)
			{
				if(ChannelID != position)
				{
					break;
				}
			}
		}
		return position;
	}
}
