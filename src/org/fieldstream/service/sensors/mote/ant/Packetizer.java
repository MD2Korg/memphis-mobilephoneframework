////Copyright (c) 2010, University of Memphis
////All rights reserved.
////
////Redistribution and use in source and binary forms, with or without modification, are permitted provided 
////that the following conditions are met:
////
////    * Redistributions of source code must retain the above copyright notice, this list of conditions and 
////      the following disclaimer.
////    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
////      and the following disclaimer in the documentation and/or other materials provided with the 
////      distribution.
////    * Neither the name of the University of Memphis nor the names of its contributors may be used to 
////      endorse or promote products derived from this software without specific prior written permission.
////
////THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
////WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
////PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
////ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
////TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
////HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
////NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
////POSSIBILITY OF SUCH DAMAGE.
////
//
//@author Somnath Mitra


package org.fieldstream.service.sensors.mote.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.fieldstream.Constants;
import org.fieldstream.oscilloscope.OscilloscopeActivity;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.ChannelToSensorMapping;
import org.fieldstream.service.sensors.mote.MoteDeviceManager;
import org.fieldstream.service.sensors.mote.sensors.SensorDataBufferSingleton;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

import android.os.Environment;
/*
 * A parser to parse incoming bytes from the bluetooth into valid tinyos1.x packets
 * of predefined oscilloscope type tinyos message but with a slightly different payload
 * @author mitra
 */
public class Packetizer extends Thread {

	private final BlockingQueue<TOSOscopeIntPacket> queue;
	StringBuilder TOSpacketBuff;
//	private ArrayList<TOSOscopeIntPacket> TOSpacketBuff;  //mahbub: this buffer is to save tos packets here to save some write operation
	private TOSOscopeIntPacket toip = null;

	private volatile boolean keepAlive;

	private static Packetizer INSTANCE = null;

	private String TAG;

//	private FileOutputStream fosTos;
//	private PrintStream pTos;
	private BufferedWriter out;


	String root = Environment.getExternalStorageDirectory().toString();
	String DIRNAME = root + "/" + Constants.LOG_DIR;

	//	String linkLogFileName = root + "/" + Constants.LOG_DIR + "/LINK_PACKET";


	Packetizer() 
	{ 
//		TOSpacketBuff=new ArrayList<TOSOscopeIntPacket>();
		TOSpacketBuff=new StringBuilder();
		queue = new LinkedBlockingQueue<TOSOscopeIntPacket>();	 
		keepAlive = true;
		setName("fs_Packetizer_"+System.currentTimeMillis());
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (Packetizer)_thread_Constructor", System.currentTimeMillis());}

	}

	public static Packetizer getInstance()
	{
		if(InferrenceService.running==false)return null; 		
		if(INSTANCE == null)
		{
			INSTANCE = new Packetizer();
			INSTANCE.setPriority(MIN_PRIORITY);

			INSTANCE.initTOSFile();
			INSTANCE.start();			
		}
		return INSTANCE;
	}

	public  void kill()
	{
		try{
			keepAlive = false;
			interrupt();  // necessary?
			INSTANCE = null;
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (Packetizer)_thread_kill()");			
			//		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (Packetizer)_thread_kill()", System.currentTimeMillis());}
		}
		catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","Exception\t: (Packetizer)_thread_kill()"+e.getLocalizedMessage());
		}
	}

	public synchronized void run() 
	{
		TAG = "readerRun";
		try 
		{
			while(keepAlive) 
			{ 
				consume(); 

			}
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (Packetizer)_thread_run() terminated");			
			//			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (Packetizer)_thread_run() terminated", System.currentTimeMillis());}

			TAG = "Reader";
			if (Log.DEBUG) Log.d(TAG,"Reader was killed");
		} 
		catch (Exception e) 
		{
			TAG = "Reader";
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (Packetizer)_thread_run() ("+e.getLocalizedMessage()+")");			
			//			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "EXCEPTION\t: (Packetizer)_thread_run() ("+e.getLocalizedMessage()+")", System.currentTimeMillis());}
		}
	}


	public void consume()
	{
		TOSOscopeIntPacket b = new TOSOscopeIntPacket();
		try
		{		
			b = queue.take();
		}
		catch(Exception e)
		{
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (Packetizer)_thread_consume() ("+e.getLocalizedMessage()+")");			
			//			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "EXCEPTION\t: (Packetizer)_thread_consume() ("+e.getLocalizedMessage()+")", System.currentTimeMillis());}
		} 
		try
		{
			// print to the tos packet file
			logToTOSFile(b);
			
			
			SensorDataBufferSingleton.getInstance().pushData(b.getChan(), b.getData());
			
			switch(b.getChan()) {
			
			case ChannelToSensorMapping.ECG:
			case ChannelToSensorMapping.ACCELX:
			case ChannelToSensorMapping.ACCELY:
			case ChannelToSensorMapping.ACCELZ:
			case ChannelToSensorMapping.GSR:
//			case ChannelToSensorMapping.BODY_TEMP:
//			case ChannelToSensorMapping.AMBIENT_TEMP:
			case ChannelToSensorMapping.RIP:
//			case ChannelToSensorMapping.ALCOHOL:
//			case ChannelToSensorMapping.GSRWRIST:
//			case ChannelToSensorMapping.TEMPWRIST:

			case ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X:
			case ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Y:
			case ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Z:
			case ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_X:
			case ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_Y:
			case ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_Z:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Y:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Z:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_X:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_Y:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_Z:
				if(OscilloscopeActivity.renderingSignals) {
					MoteDeviceManager.getInstance().onReceive(b);
					break;
				}
				
			case ChannelToSensorMapping.ALCOHOL_ACCL_RIGHT_X:
			case ChannelToSensorMapping.ALCOHOL_ACCL_RIGHT_Y:
			case ChannelToSensorMapping.ALCOHOL_ACCL_RIGHT_Z:
			case ChannelToSensorMapping.ALCOHOL_ACCL_LEFT_X:
			case ChannelToSensorMapping.ALCOHOL_ACCL_LEFT_Y:
			case ChannelToSensorMapping.ALCOHOL_ACCL_LEFT_Z:
				
			case ChannelToSensorMapping.NINE_AXIS_RIGHT_NULL_PACKET:
			case ChannelToSensorMapping.NINE_AXIS_LEFT_NULL_PACKET:
				// stop sending data to upper layer
				break;
				
				default:
					
			}
			/*
			if(b.getChan()!=8) {
				if(b.getChan()>=13 && b.getChan()<=18) {
					// TODO Hillol: For Alcohol Accelerometer Sensor need to implement
				} 
				else if( (b.getChan()>=19 && b.getChan()<=24)||(b.getChan()>=26 && b.getChan()<=31) ) {
					// TODO Hillol: Nine axis mote disabled to do further processing for displaying plot sensor data realtime.
					MoteDeviceManager.getInstance().onReceive(b);
				} else {
					// Monowar: stop sending data to upper layer
					MoteDeviceManager.getInstance().onReceive(b);
				}
			}*/
		}
		catch(Exception e)
		{
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (Packetizer)_thread_consume2() ("+e.getLocalizedMessage()+")");			
			//			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "EXCEPTION\t: (Packetizer)_thread_consume2() ("+e.getLocalizedMessage()+")", System.currentTimeMillis());}
		}
	}

	private void initTOSFile()
	{
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (Packetizer)_thread_initTOSFile()");			

		if (Constants.NETWORK_LOGGING) {
			try
			{
				File dir = new File(DIRNAME);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String FILENAME="TOS_PACKET"+ Long.toString(System.currentTimeMillis());
				File acclog=new File(DIRNAME,FILENAME);
				FileWriter writer = new FileWriter(acclog, true);
				out = new BufferedWriter(writer, 8192);

//				fosTos = new FileOutputStream(tosLogFileName + Long.toString(System.currentTimeMillis()));
//				pTos = new PrintStream(fosTos);
			}
			catch(Exception e)
			{
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (Packetizer)_thread_initTOSFile() ("+e.getLocalizedMessage()+")");			
				//				if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "EXCEPTION\t: (Packetizer)_thread_initTOSFile() ("+e.getLocalizedMessage()+")", System.currentTimeMillis());}
			}
		}
	}

	private void closeTOSFile()
	{
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (Packetizer)_thread_closeTOSFile()");			

		if (Constants.NETWORK_LOGGING) {
			try
			{
				out.flush();
				out.close();
//				pTos.flush();
//				pTos.close();
//				fosTos.close();

			}
			catch(Exception e)
			{
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (Packetizer)_thread_closeTOSFile() ("+e.getLocalizedMessage()+")");			
				if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "EXCEPTION\t: (Packetizer)_thread_closeTOSFile() ("+e.getLocalizedMessage()+")", System.currentTimeMillis());}
			}
		}
	}

	private void logToTOSFile(TOSOscopeIntPacket toip)
	{
		if (Constants.NETWORK_LOGGING) {
//			String s = printTOIP(toip);
			//print the chan
			TOSpacketBuff.append(toip.getChan()+",");

			//print the data
			int[] data = toip.getData();
			for(int i=0; i < data.length; i++)
			{
				TOSpacketBuff.append(data[i] + ",");
			}

			//print the time stamp
			TOSpacketBuff.append(toip.getReceivedTimeStamp()+",");
			TOSpacketBuff.append("\n");
//			TOSpacketBuff.append(s);
			if(TOSpacketBuff.length()>=8192){
				if(out != null){
					try {
						out.write(TOSpacketBuff.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				TOSpacketBuff.setLength(0);
//				pTos.flush();
			}
			
/*			
			if(TOSpacketBuff.size()<100)
			{
//				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","TOS buffer < 500. size = "+TOSpacketBuff.size());
				TOSpacketBuff.add(toip);
			}
			else{
				for(int i=0;i<TOSpacketBuff.size();i++)
				{
	//				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","TOS file writing");
					//String s = printTOIP(toip);
					String s = printTOIP(TOSpacketBuff.get(i));

					if(pTos != null)
						pTos.print(s);
//					if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","TOS element removing from the buffer. size = "+TOSpacketBuff.size());
					TOSpacketBuff.remove(i);
				}
			}
			if(pTos!=null)
				pTos.flush();
*/
		}
	}


/*	private String printTOIP(TOSOscopeIntPacket toip) 
	{
		StringBuilder buffer = new StringBuilder();
		//		StringBuffer buffer = new StringBuffer();

		//print the moteid
		//		buffer.append(toip.getMoteID()+",");

		//print the last sample number
		//		buffer.append(toip.getLastSample()+",");

		//print the chan
		buffer.append(toip.getChan()+",");

		//print the data
		int[] data = toip.getData();
		for(int i=0; i < data.length; i++)
		{
			buffer.append(data[i] + ",");
		}

		//print the time stamp
		buffer.append(toip.getReceivedTimeStamp()+",");
		buffer.append("\n");
		String s = buffer.toString();
		return s;
	}
*/
	public void addPacket(int[] samples, int sampleNo,int channel, long timestamp) {
		TOSOscopeIntPacket inPacket=new TOSOscopeIntPacket();
		inPacket.setChan(channel);
		for(int i=0; i < sampleNo; i++)
		{
			inPacket.setNextDataItem(samples[i]);
		}
		inPacket.setReceivedTimeStamp(timestamp);
		try{
			queue.put(inPacket);
		}catch (InterruptedException e) {

			e.printStackTrace();
		}
	}

	public void finalize() {
		closeTOSFile();
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (Packetizer)_thread_finalize()");			
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "EXCEPTION\t: (Packetizer)_thread_finalize()",System.currentTimeMillis());}
	}
}

