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
package org.fieldstream.service;

// some things to do
// 1. add the startForeground code to encourage android to keep this and the ema services running
// 2. remove Message Handler in Bluetooth Connection and move code to receiveThread's run, then remove

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.AbstractLogger;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.logger.TextFileLogger;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.ContextSubscriber;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Debug;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
/**
 *
 * @author Patrick Blitz
 * @author Somnath Mitra
 * @author Andrew Raij
 * main class of the stress inferencing project, backend part
 * implements the interface given in the .aidl file, provides a given set of functions to the front-end (gui) programs
 * relates to the major components of the middle layer of our software
 * <br />
 * To call it, use the code below
 * <pre>
 *   class InferrenceServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
			IBinder boundService ) {
          service = IInferrenceService.Stub.asInterface((IBinder)boundService);
		  if (Log.DEBUG) Log.d( LOG_TAG,"onServiceConnected" );
        }

        public void onServiceDisconnected(ComponentName className) {
          service = null;
		  if (Log.DEBUG) Log.d( LOG_TAG,"onServiceDisconnected" );
        }
    };
 *
 * </pre>
 *  and (in the same class)
 *
 <pre>conn = new InferrenceServiceConnection();
  	    Intent i = new Intent();
  	    i.setClassName( "edu.cmu.ices.stress.phone.service", "InferrenceService" );
  	    bindService( i, conn, Context.BIND_AUTO_CREATE);
 </pre>
 *
 */

public class InferrenceService extends Service implements ContextSubscriber{
		private static final String LOG_TAG = "INFERRENCESERVICE";
		public static Context mContext=null;

		public ActivationManager fm;
		private boolean active=false;
		public static InferrenceService INSTANCE;
		protected static ArrayList<IInferrenceServiceCallback> subscribers;
		public static boolean running=true;
//		private AntStateManager antStateManager = null;

		private final IInferrenceService.Stub mBinder = new IInferrenceService.Stub(){
			public int getCurrentLabel(int model) throws RemoteException {
				// TODO Auto-generated method stub

				return 0;
			}

			public void subscribe(IInferrenceServiceCallback listener)
					throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_subscribe()");

				if (subscribers==null) {
					subscribers= new ArrayList<IInferrenceServiceCallback>();

				}
				subscribers.add(listener);
				activate();

			}

			public void unsubscribe(IInferrenceServiceCallback listener)
					throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_unsubscribe()");

				subscribers.remove(listener);
			}

			public void activateMote(int mote) throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_activateMote()");
				fm.activateMote(mote);
			}


			public void deactivateMote(int mote) throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_deactivateMote()");

				fm.deactivateMote(mote);

			}

			public void activateModel(int model) throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_activateModel()");

				fm.activate(model);

			}

			public void deactivateModel(int model) throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_deactivateModel()");

				fm.deactivate(model);
			}

			public int getActiveModels() throws RemoteException {
				// TODO Auto-generated method stub
				return 0;
			}

			public void writeLabel(String string) throws RemoteException {
				if(dataLogger!=null) {
					dataLogger.logUIData(string);
				}
			}

			public void writeEMALog(int triggerType, String activeContexts, int status, long prompt,
					long delayDuration, String[] delayResponses, long[] delayResponseTimes, long start,
					String[] responses, long[] responseTimes)
					throws RemoteException {


				// log EMA data
				dataLogger.logEMA(triggerType, activeContexts, status, prompt, delayDuration, delayResponses, delayResponseTimes,
						start, responses, responseTimes);

				int r1=0, r2=0, r4 = 0, r5 = 0, r6 = 0;
				try {
					r1 = Integer.parseInt(responses[1]);
					r2 = Integer.parseInt(responses[2]);
					r4 = Integer.parseInt(responses[4]);
					r5 = Integer.parseInt(responses[5]);
					r6 = Integer.parseInt(responses[6]);

					if (r1 == -1 || r2 == -1 || r4 == -1 || r5 == -1 || r6 == -1) {

					}
					else {
						float newLabel=calculateEMALabel(r1,r2,r4,r5,r6);
						Log.d("EMALabel", ""+newLabel);
						fm.publishNewGroundTruth(Constants.MODEL_ACCUMULATION,newLabel);
						// inform the incentives manager (some incentives are based on EMA actions)
						// incentivesManager.processEMA(status, prompt, start);
					}
				}
				catch(Exception e) {

				}


			}

			public void setFeatureComputation(boolean state)
					throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_setFeatureComputation()");

				if (Log.DEBUG) Log.d(LOG_TAG,"set Feature Computation to "+state);
				FeatureCalculation.getInstance().active=state;

			}

/*		    public int getNumEMAsToday() {
		    	Cursor c = getTodaysEMAs(starttime,endtime);
		    	if (c==null)
		    		return 0;

		    	int x = c.getCount();
		    	c.close();
		    	return x;
		    }

		    public long getLastEMATimestamp() {
		    	Cursor c = getTodaysEMAs(starttime,endtime);
		    	if (c==null)
		    		return 0;

		    	long maxTime = 0;
		    	if (c.getCount() != 0) {
		    		int timeColumn = c.getColumnIndex("prompt_timestamp");
			    	c.moveToFirst();
		    		maxTime = c.getLong(timeColumn);

		    		while (c.moveToNext()) {
			    		long currentTime = c.getLong(timeColumn);
			    		if (currentTime > maxTime) {
			    			maxTime = currentTime;
			    		}
			    	}
		    	}

		    	c.close();

		    	return maxTime;
		    }
	*/

			public double getTotalIncentivesEarned() throws RemoteException {

				return dataLogger.getTotalIncentivesEarned();
			}

			public void logResume(long timestamp) throws RemoteException {
				if (dataLogger != null)
					dataLogger.logResume(timestamp);
			}

			public void activateSensor(int sensor) throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_activateSensor()");

				fm.activateSensor(sensor);
			}

			public void deactivateSensor(int sensor) throws RemoteException {
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_deactivateSensor()");

				fm.deactivateSensor(sensor);
			}



		};

		public AbstractLogger dataLogger;
//		public StressInferenceIncentivesManager incentivesManager;

		public InferrenceService() {
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_Constructor() context="+mContext);
		}

		protected void finalize() {
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_finalize()");

			Log.d("InferrenceService", "Garbage Collected");
		}

		protected float calculateEMALabel(int r1_reversed, int r2_reversed, int r4, int r5, int r6) {
			int r1=7-log2(r1_reversed);
			int r2=7-log2(r2_reversed);
			r4 = log2(r4);
			r5 = log2(r5);
			r6 = log2(r6);

			Log.d("R1", ""+r1);
			Log.d("R2", ""+r2);
			Log.d("R4", ""+r4);
			Log.d("R5", ""+r5);
			Log.d("R6", ""+r6);

			float out = (r1 + r2 + r4 + r5 + r6) / 5.0f;
			Log.d("OUT", ""+out);

			return out;
		}


		protected int log2(int x) {
			int r = 0; // r will be lg(v)

			while (x > 0)
			{
     	      x = x >> 1;
			  r++;
			}

			return r;
		}



		/**
		 * called to create this service, standard anroid function
		 */
	@Override
	public IBinder onBind(Intent arg0) {
            return mBinder;
	}




//	HTTPRequester http = null;
	public void activate() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_activate()");

		if (active)
			return;

		Log.d("InferenceService", "activating");
		mContext=this.getApplicationContext();


		// Create the mote sensor manager
		//MoteSensorManager.getInstance();

		// Create the Ant state manager
//		antStateManager = (AntStateManager) AntStateManager.getInstance(Constants.moteAddress[0],getApplicationContext());

//		Log.d("InferenceService", "ant address: " + Constants.antAddressRIPECG);
		/* Start up creates ANT Connection*/
//		antStateManager.startUp(getApplicationContext());

		//Monowar: added this line : should start btStateManager thread once
//		AntConnection.getInstance(Constants.moteAddress, getApplicationContext());
		//antConnection = new AntConnection(getApplicationContext());

//		if(btStateManager.startUp()==true){
//			BluetoothConnectionStates.setLAST_RECEIVED_TIME(System.currentTimeMillis());//if receivethread has some delay
//			BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_CONNECTED);
//		}
//		else{
//			BluetoothConnectionStates.setCURRENT_BT_STATE(BluetoothConnectionStates.BT_STATE_PERIODIC_SCANNING);
//			btStateManager.timeToNextScan=System.currentTimeMillis();
//		}

	//	btStateManager.start();

		//Create the mote device manager
//		MoteDeviceManager.getInstance();
		//MoteDeviceManager.getInstance().start();

        if (fm == null) {
        	fm = ActivationManager.getInstance();
        	fm.init();
        }

  		File root = Environment.getExternalStorageDirectory();
//  		if (Constants.LOGTODB) {
  			dataLogger = DatabaseLogger.getInstance(this);
 // 		} else {
//  			dataLogger = new TextFileLogger(root + "/" + Constants.LOG_DIR, true);
 // 		}

//  		incentivesManager = StressInferenceIncentivesManager.getInstance();
 // 		incentivesManager.setDay(StressInferenceIncentivesManager.FIRST_DAY);

    	if (Log.DEBUG) Log.d( LOG_TAG, "onCreate" );

    	// added for testing power reqs of using a backend
//    	http = HTTPRequester.getInstance();
//    	http.addData("garbage", "010320523");
//    	http.setUrl(HTTPRequester.DEFAULT_URL);
//    	http.postAddedData();


        ContextBus.getInstance().subscribe(this);
        active = true;
	}

	public void deactivate() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_deactivate()");
		if (active) {
//			antStateManager.stopDown();
//			antStateManager.kill();
//			antStateManager=null;
//			if (antConnection != null)
//				antConnection.disableRadio();

//			btStateManager.stopDown();
//			btStateManager.kill();
//			btStateManager = null;

			//MoteDeviceManager.getInstance().kill();

//			if (http != null)
//				http.shutdown();
			if(fm!=null)
				fm.destroy();
			fm=null;
			dataLogger = null;
//			incentivesManager = null;
			active=false;
		}
	}

    public void onCreate() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_onCreate()");
		running=true;
    	super.onCreate();
        if (INSTANCE== null) {
        	INSTANCE = this;
        }
		if (Constants.WRITETRACE)
			Debug.startMethodTracing("stressInferenceService");



		if (!active)
			activate();
      }

    public void onDestroy() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_onDestroy");
		running=false;
    	if (Log.DEBUG) Log.d( LOG_TAG,"onDestroy");
    	  if (active) {
    		  deactivate();
    	  }
    	  INSTANCE = null;
  		if (Constants.WRITETRACE)
			Debug.stopMethodTracing();
    	  super.onDestroy();
  		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (InferrenceService)_onDestroy_end");

      }


/*    public Cursor getTodaysEMAs(long startTime,long endTime) {

    	DatabaseLogger logger = DatabaseLogger.getInstance(this);
    	return logger.readEMA(startTime, endTime);
    }
 */

	public void receiveContext(int modelID, int label, long startTime, long endTime) {


		if (subscribers!=null) {
		for (IInferrenceServiceCallback subscriber: subscribers) {
			try {
				subscriber.receiveCallback(modelID, label, startTime, endTime);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}
}