﻿//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
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

package org.fieldstream.service.sensor.virtual;

//@author Amin Ahsan Ali
//@author Patrick Blitz
//@author Mahbub Rahman


import org.fieldstream.Constants;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.SensorBus;
import org.fieldstream.service.sensor.SensorBusSubscriber;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.fieldstream.service.sensors.mote.sensors.MoteSensorManager;
import org.fieldstream.service.sensors.mote.sensors.MoteUpdateSubscriber;

/**
 * This Class has two modes. One connects to the mote subsystem to receive updates for the ECG one connects to the replay server.
 * @author mahbub
 *
 */
public class ExhalationFirstDiffVirtualSensor extends AbstractSensor implements MoteUpdateSubscriber,SensorBusSubscriber {

//	private static final int NUMBER_OF_PEAKS_TO_CONSIDER = 6;			//because one peak may be discarded. 5 peaks are supposed to be there
//	private static final int EXHALATION_WINDOWSIZE =5; //4*NUMBER_OF_PEAKS_TO_CONSIDER+2;
//	private static final Boolean EXHALATION_SCHEDULER = false;
	private static final int FRAMERATE = 60;
	/**
	 * duration in seconds.
	 */
	private static final int WINDOW_DURATION=60;
	private static final int EXHALATION_WINDOWSIZE = WINDOW_DURATION*FRAMERATE;
	private static final Boolean EXHALATION_SCHEDULER = false;

	private static final Boolean REPLAY_SENSOR = false;
	/**
	 * decide if the Replay sensor or the mote ECG sensor should be used!
	 */
	//private static final Boolean REPLAY_SENSOR = true;
	private ExhalationFirstDiffRunner runner = new ExhalationFirstDiffRunner();

	private Object lock = new Object();
	/**
	 * internal runner to execute the HRCalculation in a separate thread
	 * @author blitz
	 *
	 */
	class ExhalationFirstDiffRunner implements Runnable {
		//private ExhalationFirstDiffCalculation exhalationFirstDiffCalculation;
		private FirstDifferenceExhalationNew firstDifference;
		public ExhalationFirstDiffRunner() {
			firstDifference = new FirstDifferenceExhalationNew();
		}
		public boolean active=true;
		public int[] buffer;
		public long[] timestamps;
		public int startNewData;
		public int endData;
		public void run() {
			while (active) {
				synchronized (lock) {
					if (buffer!=null) {
						Log.d("FirstDiffBefore","BeginTS= "+timestamps[0]+"EndTS= "+timestamps[timestamps.length-1]);
						int[] calculate = firstDifference.calculate(buffer, timestamps);
						if (calculate.length != 0) {
							long[] timestampsNew = new long[calculate.length];
							if(calculate.length<=1)
							{
								timestampsNew[0]=timestamps[0];
							}
							else if (calculate.length<timestamps.length) {
								float fakeIndex = 0;
								float factor = timestamps.length/((float)calculate.length - 1);
								timestampsNew[0]=timestamps[0];
								for (int i=1; i<calculate.length;i++) {
									fakeIndex = i * factor;
									timestampsNew[i]=timestamps[(int)Math.floor(fakeIndex)-1];
								}
							} else {
								for (int i=0; i<calculate.length;i++) {
									timestampsNew[i]=timestamps[i];
								}
							}

							int start = 0;
							int end = calculate.length;
							Log.d("FirstDiffAfter","BeginTS= "+timestampsNew[0]+"EndTS= "+timestampsNew[timestampsNew.length-1]);
							//							String sensor = "";
							//							for (int i=0; i < calculate.length; i++) {
							//								sensor += calculate[i] + ",";
							//							}
							//							Log.d("Exhalation", "Exhalation Durations = " + sensor);
//							String sensor = "";

//							for (int i=0; i < calculate.length; i++) {
//								sensor += calculate[i] + ",";
//							}
//							Log.d("ExhalationFirstDiff", "ExhalationFirstDiff Durations = " + sensor);

//							String timeStamp="";
//							for(int i=0;i<timestampsNew.length;i++)
//							{
//								timeStamp+=timestampsNew[i]+",";
//							}
//							Log.d("ExhalationFirstDiff","ExhalationFirstDiff timestamp= "+timeStamp);

							//sendBufferReal(calculate, timestamps, start, end);
							sendBufferReal(calculate, timestampsNew, start, end);
							//Log.d("RealPeakValleyCalculatingRunner", "Send buffer after calculation");
						}
					}
					try {
						if (active)
							lock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	};
	private Thread ExhalationFirstDiffThread;
	private ExhalationFirstDiffVirtualSensor INSTANCE;

	public ExhalationFirstDiffVirtualSensor(int SensorID) {
		super(SensorID);
		INSTANCE = this;
		initalize(EXHALATION_SCHEDULER,EXHALATION_WINDOWSIZE, EXHALATION_WINDOWSIZE);		//can I use sliding window
	}

	@Override
	public void activate() {
		Log.d("ExhalationFirstDiff","Acticated");
		if (REPLAY_SENSOR) {
			SensorBus.getInstance().subscribe(this);
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_REPLAY_RESP);
		} else {
			MoteSensorManager.getInstance().registerListener(this);
			InferrenceService.INSTANCE.fm.activateSensor(Constants.SENSOR_RIP);
		}

		// as this depends on the ECG sensor to be active, i need to load it to make sure it's there!

		//SensorBus.getInstance().subscribe(this);
		active = true;
		runner.active = true;
		ExhalationFirstDiffThread = new Thread(runner);
        ExhalationFirstDiffThread.setName("virtual_"+System.currentTimeMillis());

		ExhalationFirstDiffThread.start();
	}

	@Override
	public void deactivate() {
		Log.d("ExhalationFirstDiff","Deacticated");
		//SensorBus.getInstance().unsubscribe(this);
		if (REPLAY_SENSOR) {
			SensorBus.getInstance().unsubscribe(this);
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_REPLAY_RESP);
		} else {
			MoteSensorManager.getInstance().unregisterListener(this);
			InferrenceService.INSTANCE.fm.deactivateSensor(Constants.SENSOR_RIP);
		}

		active = false;
		synchronized(lock) {
			runner.active=false;
			ExhalationFirstDiffThread=null;
			lock.notify();
		}

	}


	@Override
	protected void sendBuffer(int[] toSendSamples, long[] toSendTimestamps,
			int startNewData, int endNewData) {
		// TODO Auto-generated method stub
		//		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);

		if (active) {
			synchronized (lock) {
				runner.buffer=toSendSamples;
				runner.timestamps=toSendTimestamps;
				runner.startNewData=startNewData;
				runner.endData=endNewData;
				lock.notify();
			}
		}
	}
	/**
	 * called from the Runner thread to actually send the new buffer to the AbstractSensor
	 * @param toSendSamples
	 * @param toSendTimestamps
	 * @param startNewData
	 * @param endNewData
	 */
	protected void sendBufferReal(int[] toSendSamples, long[] toSendTimestamps,
			int startNewData, int endNewData) {
		// TODO Auto-generated method stub
		super.sendBuffer(toSendSamples, toSendTimestamps, startNewData, endNewData);
	}

	//	public void onReceiveData(int SensorID, int[] data) {
	//		if(SensorID == Constants.SENSOR_VIRTUAL_REALPEAKVALLEY)
	//		{
	//			long[] timeStamps = new long[data.length];
	//			addValue(data, timeStamps);
	//		}
	//
	//	}
//	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
//			int startNewData, int endNewData) {
//		if (sensorID==Constants.SENSOR_VIRTUAL_EXHALATION) {
//			String ripData="";
//			for(int i=0;i<data.length;i++)
//			{
//				ripData+=data[i]+",";
//			}
//			String checktimestamp="";
//			for(int i=0;i<timestamps.length;i++)
//			{
//				checktimestamp+=timestamps[i]+",";
//			}
//			Log.d("ExhalationFirstDiff", "raw RIP data= "+ripData);
//			Log.d("ExhalationFirstDiff","raw RIP data timestamp= "+checktimestamp);
//			addValue(data, timestamps);
//		}
//	}
	public void onReceiveData(int SensorID, int[] data, long[] timeStamps) {

		Log.ema_alarm("","MUS: EXhale: SensorID = " + SensorID);
		if(SensorID == Constants.SENSOR_RIP)
		{
			//long[] timeStamps = new long[data.length];
			//Arrays.fill(timeStamps, 0, data.length, timestamp);
			addValue(data, timeStamps);

//			if (Log.DEBUG) {
//				//Log.d("RealPeakValleyVirtualSensor", "raw value = " + data[0]);
//				//comment out-mahbub
//				//Log.d("RealPeakValleyVirtualSensor", "length of data array = " + data[0]);
//				String ripData="";
//				for(int i=0;i<data.length;i++)
//				{
//					ripData+=data[i]+",";
//				}
//				String checktimestamp="";
//				for(int i=0;i<timeStamps.length;i++)
//				{
//					checktimestamp+=timestamps[i]+",";
//				}
//				Log.d("ExhalationFirstDiff", "raw RIP data= "+ripData);
//				Log.d("ExhalationFirstDiff","raw RIP data timestamp= "+checktimestamp);
//			}
		}
	}
	public void receiveBuffer(int sensorID, int[] data, long[] timestamps,
			int startNewData, int endNewData) {
		if (sensorID==Constants.SENSOR_REPLAY_RESP) {
			addValue(data, timestamps);

		}
	}
}
