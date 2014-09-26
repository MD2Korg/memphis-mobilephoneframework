package org.fieldstream.service.sensors.mote;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/*
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
*/
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

public class MoteDeviceManager {
	
	private String TAG = "MoteDeviceManager";
	
	private static MoteDeviceManager INSTANCE;
	public static long startSampleTime;
	//private final BlockingQueue<TOSOscopeIntPacket> queue;
	
	public ArrayList<MoteReceiverInterface> motePacketSubscribers;
	
	// private volatile boolean keepAlive;
	
	public MoteDeviceManager() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (MDM)_Constructor");
		motePacketSubscribers = new ArrayList<MoteReceiverInterface>();
		startSampleTime=System.currentTimeMillis();
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (MDM)_Constructor", System.currentTimeMillis());}
	}
	
	public static MoteDeviceManager getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new MoteDeviceManager();
			String TAG = "MoteDeviceManager.getInstance";
			
			if (Log.DEBUG) Log.d(TAG, "created New");
		}
		return INSTANCE;
	}
	
	public void subscribe(MoteReceiverInterface subscriber)
	{
		Log.d("MoteDeviceManager.subsribe", subscriber.toString());
		motePacketSubscribers.add(subscriber);
	}
	
	public void unsubscribe(MoteReceiverInterface subscriber)
	{
		motePacketSubscribers.remove(subscriber);
	}
	
	/*
	 * This distributes packets to motes
	 */
	public void onReceive(TOSOscopeIntPacket toip)
	{ 	int count=0;
		try{
		Log.d("MoteDeviceManager", "onReceive() called");
		
		for( MoteReceiverInterface item: motePacketSubscribers )
		{
			Log.d("MoteDeviceManager", "sending to receiver");
			item.onReceiveMotePacket(toip);
			count++;
		}
		}
		catch(Exception e){
			if(Log.DEBUG_HILLOL) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				Log.h(TAG, sw.toString());
			}
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","Exception\t: (MDM)_OnReceive() (count="+count+" "+e.getLocalizedMessage()+")");
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "Exception\t: (MDM)_OnReceive() (count="+count+" "+e.getLocalizedMessage()+")",System.currentTimeMillis());}
		}
	}
	
/*	public void onMissingSamplesRequest(int SensorID, int numberOfPackets)
	{
		try{
			if(Log.DEBUG)
			{
				Log.d(TAG,"null packet request for "+" Sensor = "+SensorID+" no. of packets = "+numberOfPackets);
			}
			for ( MoteReceiverInterface item: motePacketSubscribers)
			{
				item.onMissingSamplesRequest(SensorID, numberOfPackets);
			}
		}catch(Exception e)
		{
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","Exception\t: (MDM)_OnMissingSample ("+e.getLocalizedMessage()+")");
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "Exception\t: (MDM)_OnMissingSample() ("+e.getLocalizedMessage()+")",System.currentTimeMillis());}
		}
	}	
	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (MDM)_finalize() ");			
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (MDM)_finalize()", System.currentTimeMillis());}
	}
*/
}
