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

//@author Syed Monowar Hossain
//@author Mishfaq Ahmed
//@author Brian French
//@author Andrew Raij

package org.fieldstream.gui.ema;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fieldstream.Constants;
import org.fieldstream.functions.AlarmScheduler;
import org.fieldstream.functions.ReadWriteConfigFiles;
import org.fieldstream.incentives.EMAIncentiveManager;
import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class InterviewScheduler extends Service {

	public static class Model {
		private String modelName;
		private int budget;
		private int expectedNumber;
		private int triggerDelay;
		private int modelID;
		String level;
		private int indexQuestionFile;
		public Model(int cmodelID,String cmodelName, int cbudget, int cexpectedNumber,int cdelay,String clevel,int indexfile) {
			modelID=cmodelID;
			this.modelName = cmodelName;
			this.budget = cbudget;
			this.expectedNumber = cexpectedNumber;
			triggerDelay=cdelay*60*1000;
			level=clevel;
			indexQuestionFile=indexfile;
		}
		public int getModelID() { return modelID; }		
		public String getModelName() { return modelName; }
		public int getBudget() { return budget; }
		public int getExpectedNumber() { return expectedNumber; }
		public int getTriggerDelay() { return triggerDelay; }
		public String getlevel() { return level; }

	}
	String TAG="IS";
	DatabaseLogger dataLogger=null;
	EMAIncentiveManager incentives = null;
	static InterviewScheduler INSTANCE = null;
	Intent interviewIntent;
	Intent inferenceServiceIntent;
	IInferrenceService inferenceService;
	IInterviewCancellationCallback interviewCallback;

	Handler scheduler;
	long lastInterviewTimeTry;
	long nextPeriodicTime;
	boolean interviewRunning;
	int launchType;
	long lastInterviewTime;
	String activeModelsString;
	boolean sleepActive,quietActive,graceActive;

	static public IContent getContent() {
		IContent content = InterviewContent.getInstance();
		return content;
	}

	public EMAIncentiveManager getIncentiveMangager() {
		return incentives;
	}	
	// scheduler constants
	static final long MINUTE_MILLIS = 60L * 1000L;
	static final long HOUR_MILLIS = 60L * 60L * 1000L;
	static final long timeRange=30L*MINUTE_MILLIS; // check last 30 minutes
	static final int dataValidity=18;	// 60% of 30 mins. if it is satisfied, ema will trigger
	static final long delayNotValidData=5L*MINUTE_MILLIS;
	long PERIODIC_INTERVAL;// = (55L * MINUTE_MILLIS);
	long GRACE_PERIOD;// = (33L * MINUTE_MILLIS);
	int MAX_DAILY_EMA;// = 20;
	public Model[] models;
	int MINDELAY_FIRSTEMA=20; //10 minutes minimum delay for 1st EMA	

	// rescheduling type constants
	static final int PERIODIC = 0;
	static final int CONTEXT_CHANGE = 2;
	static final int BAD_DATA=3;

	// sub activity constants
	static final int INTERVIEW_REQUEST = 1;

	// ema log constants
	static final String[] INTERVIEW_HEADER = { "TYPE", "DATE", "TIME", "AM/PM",
		"RAW TIMESTAMP", "DELAY", "DELAY REASON", "START OFFSET", "STATUS" };


	/* START ANDROID LIFE CYCLE */

	/** Called when the activity is first created. */
	@Override
	public void onCreate() { // (Bundle savedInstanceState) {
		loadEMAConfig();

		super.onCreate();// (savedInstanceState);
		INSTANCE = this;
	}
	@Override
	// TODO THIS FUNCTION IS DEPRECATED AFTER API 4. SHOULD BE CHANGED TO
	// ONSTARTCOMMAND IF API 5 OR ABOVE IS USED.
	public void onStart(Intent intent, int startId) {
		// the intent contains potential configuration information
		super.onStart(intent, startId);
		if (intent != null) {
			dataLogger=DatabaseLogger.getInstance(this);
			ReadWriteConfigFiles.getInstance(this).loadDeadPeriodsDB();
		} 
		initialize_handler();
		bindServices();
		resetInterviewScheduler();
	}
	void stopInterviewScheduler()
	{
		Log.mm("mm", "StopInterviewScheduler");
		if (interviewCallback != null && interviewRunning) {
			try {
				interviewCallback.cancelInterview();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		scheduler.removeCallbacks(launchScheduledInterview);
		scheduler.removeCallbacks(launchContextChangeInterview);
		scheduler.removeCallbacks(restartScheduler);
		scheduler.removeCallbacks(QUIETTIME);
		scheduler.removeCallbacks(SLEEPTIME);
		scheduler.removeCallbacks(resetNewDay);
		scheduler.removeCallbacks(launchSalivaInterview);

		interviewRunning = false;
		Constants.INTERVIEWRUNNING=false;
		sleepActive=false;quietActive=false;graceActive=false;

	}
	void startInterviewScheduler()
	{
		Log.ema_alarm("", "Start Interview Scheduler");
		long currtime=System.currentTimeMillis();
		if(dataLogger==null) dataLogger=DatabaseLogger.getInstance(this);
		lastInterviewTime = dataLogger.getLastEMA_completed_or_prompted();
		lastInterviewTimeTry =  lastInterviewTime;

		interviewRunning = false;
		Constants.INTERVIEWRUNNING=false;


		sleepActive=false;quietActive=false;graceActive=false;
		ReadWriteConfigFiles.getInstance(this).loadDeadPeriodsDB();
		initIncentiveManager();
		initEMABudget();
		initEMATriggerer(this.budgeter,models);
		if(lastInterviewTime+this.GRACE_PERIOD>currtime){
			Log.ema_alarm("", "Start Interview Scheduler: Enter grace period: last Interview Time="+Constants.millisecondToDateTime(lastInterviewTime)+" gracetime="+this.GRACE_PERIOD);
			this.enterGracePeriod(lastInterviewTime+this.GRACE_PERIOD);
			return;
		}
		else if (currtime < Constants.QUIETSTART && Constants.QUIETSTART<Constants.QUIETEND){
			Log.ema_alarm("IS", "Quiettime Start: "+Constants.millisecondToDateTime(Constants.QUIETSTART));
			scheduler.removeCallbacks(QUIETTIME);
			scheduler.postDelayed(QUIETTIME, Constants.QUIETSTART - currtime);
		}
		else if (currtime < Constants.QUIETEND && Constants.QUIETSTART<Constants.QUIETEND){
			Log.ema_alarm("IS", "Quiettime: now");		
			scheduler.removeCallbacks(QUIETTIME);
			scheduler.post(QUIETTIME);
			return;
		}
		Log.ema_alarm("IS", "Sleep start:" + Constants.millisecondToDateTime(Constants.SLEEPSTART)+ "Sleep end"+Constants.millisecondToDateTime(Constants.SLEEPEND));
		if(currtime>=Constants.SLEEPSTART && currtime<Constants.SLEEPEND){
			Log.mm("IS", "sleeptime: now");
			scheduler.removeCallbacks(SLEEPTIME);
			scheduler.post(SLEEPTIME);
			return;
		}
		else if(currtime<Constants.SLEEPSTART){
			Log.ema_alarm("IS", "Sleeptime: "+Constants.millisecondToDateTime(Constants.SLEEPSTART));			
			scheduler.removeCallbacks(SLEEPTIME);
			scheduler.postDelayed(SLEEPTIME, Constants.SLEEPSTART-currtime);
		}
		reschedulePeriodicEMA();
		Log.ema_alarm("IS", "resetNewDay: "+Constants.millisecondToDateTime(Constants.DAYEND));		
		scheduler.removeCallbacks(resetNewDay);
		scheduler.postDelayed(resetNewDay, Constants.DAYEND-currtime);
	}
	void resetInterviewScheduler() // reset: new day, onStart, onservice connected
	{
		Log.mm("", "reset interview scheduler");
		stopInterviewScheduler();
		startInterviewScheduler();
	}
	/* This is called when the app is killed. */
	@Override
	public void onDestroy() {

		if (incentives != null) {
			incentives.cleanup();
			incentives = null;
		}

		Log.i(TAG, "onDestroy");
		// unbind the inference service since we are being destroyed
		unbindServices();
		stopInterviewScheduler();
		super.onDestroy();
	}

	/* END ANDROID LIFE CYCLE */
	@SuppressWarnings("all")
	private void initialize_handler() {
		interviewIntent = new Intent(getBaseContext(), Interview.class);
		interviewIntent.putExtra("interviewType", INTERVIEW_REQUEST);
		interviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		inferenceServiceIntent = new Intent(getBaseContext(), InferrenceService.class);

		scheduler = new Handler();
	}
	boolean isvalidforema(int sensorid)
	{
		long endtimestamp=System.currentTimeMillis();
		long starttimestamp=endtimestamp-timeRange;
		String temp[],temp1[];
		int count=0,value;
		String entry;
		Cursor c;
		//erase
		if(true) return true;
		dataLogger=DatabaseLogger.getInstance(this);
		c=dataLogger.readdataquality(sensorid, starttimestamp, endtimestamp);

		if (c==null)
			return false;

		if (c.getCount() != 0) {

			int contextcolumn = c.getColumnIndex("entry");
			c.moveToFirst();
			entry = c.getString(contextcolumn);

			temp=entry.split(",",2);
			temp1=temp[0].split("=");
			value=Integer.valueOf(temp1[1]);
			if(value>=13) count++;
			Log.mm(TAG,"OK\t: (InterviewScheduler)_isvalidforema() entry="+entry+" temp="+temp[0]+" temp1="+temp1[1]+" value="+value);

			while (c.moveToNext()) {
				entry = c.getString(contextcolumn);
				temp=entry.split(",",2);
				temp1=temp[0].split("=",2);
				value=Integer.valueOf(temp1[1]);
				if(value>=13) count++;
				Log.mm(TAG,"OK\t: (InterviewScheduler)_isvalidforema() entry="+entry+" temp="+temp[0]+" temp1="+temp1[1]+" value="+value);

			}
		}
		c.close();
		Log.mm(TAG,"OK\t: (InterviewScheduler)_isvalidforema() count="+count+", datavalidity="+dataValidity);
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK\t: (InterviewScheduler)_isvalidforema() count="+count,System.currentTimeMillis());}	
		if(count >=dataValidity)
			return true;
		return false;
	}


	/*
	 * Adds a header to the EMA log
	 */

	/* BEGIN INTERPROCESS COMMUNICATION WITH INFERRENCE SERVICE */

	private void bindServices() {
		bindService(inferenceServiceIntent, inferenceConnection, 0);
	}

	private void unbindServices() {
		if (inferenceService != null) {
			try {
				inferenceService.unsubscribe(inferenceCallback);
				//stopService(inferenceServiceIntent);
				if (Log.DEBUG) Log.d("unbindInferenceService",
						"Unsubscribed the service callback");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		unbindService(inferenceConnection);
		// unbindService(monitorConnection);
		if (Log.DEBUG) Log.d("unbindServices", "Services unbound");

	}

	/*
	 * Connection to the inference service
	 */
	private ServiceConnection inferenceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			inferenceService = IInferrenceService.Stub.asInterface(service);
			if (Log.DEBUG) Log.d("inferenceConnection", "Connected to the inference service");

			try {
				inferenceService.subscribe(inferenceCallback);				

				if (Log.DEBUG) Log.d("inferenceConnection",
						"Subscribed to the inference service callback");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			//reschedule(BAD_DATA,false);
			//			scheduleFirstInterview();
			resetInterviewScheduler();
		}

		public void onServiceDisconnected(ComponentName name) {			

			inferenceService = null;
			if (Log.DEBUG) Log.d("inferenceConnection", "Disconnected from inference service");
		}
	};
	/*
	 * IInferrenceServiceCallback implementation
	 */
	private IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {
		public void receiveCallback(int modelID, int value, long start, long end) throws RemoteException {
			Log.ema_alarm(TAG,"OK\t: (InterviewScheduler)_InferenceCallback receiveCallBack()");
			processContext(modelID, value); // (((Integer)value).toString());
		}
	};
	/* END INTERPROCESS COMMUNICATION WITH INFERRENCE SERVICE */

	private void enterGracePeriod(long gracePeriodTime) {
		long curtime=System.currentTimeMillis();
		stopInterviewScheduler();
		graceActive=true;
		Log.ema_alarm("IS", "gracePeriodOver: "+Constants.millisecondToDateTime(gracePeriodTime));
		scheduler.removeCallbacks(restartScheduler);
		if(gracePeriodTime-curtime<=0)
			scheduler.post(restartScheduler);
		else
			scheduler.postDelayed(restartScheduler, gracePeriodTime-curtime);
	}


	/*
	 * All interview rescheduling happens here so that they are properly
	 * synchronized
	 */
	long removequietperiod(long currtime,long quietStartTime,long quietStopTime,long dayEndTime)
	{
		if(quietStopTime<currtime) return 0;
		if(quietStartTime >dayEndTime) return 0;
		if(currtime<=quietStartTime){
			if(dayEndTime>quietStopTime)
				return quietStopTime-quietStartTime;
			else
				return dayEndTime-quietStartTime;
		}
		else{
			if(dayEndTime>quietStopTime)
				return quietStopTime-currtime;
			else return dayEndTime-currtime;
		}
	}
	long removesleepperiod(long currtime,long sleepStartTime,long sleepEndTime,long dayEndTime)
	{
		long res=0;
		if(dayEndTime>sleepEndTime){
			if(currtime>sleepEndTime) res=0;
			else if(currtime<sleepStartTime) res=sleepEndTime-sleepStartTime;
			else if(currtime>sleepStartTime) res=sleepEndTime-currtime;
			if(sleepStartTime+Constants.DAYMILLIS<dayEndTime){
				if(currtime<sleepStartTime+Constants.DAYMILLIS)
					res+=dayEndTime-(sleepStartTime+Constants.DAYMILLIS);
				else if(currtime>sleepStartTime+Constants.DAYMILLIS)
					res+=dayEndTime-currtime;
			}
		}
		else{
			if(dayEndTime>sleepStartTime){
				if(currtime<sleepStartTime) res=dayEndTime-sleepStartTime;
				else res=dayEndTime-currtime;
			}
			else{
				res=0;
			}
		}

		return res;
	}
	public long removetimeforsaliva()
	{
		long millisec=8*30*60*1000; //8 saliva, 30 mins gap for each, 60 sec, 1000 millisec
		return millisec;
	}
	long calculateRemainingTime()
	{
		long currtime=System.currentTimeMillis();
		long remainingtime=Constants.DAYEND-currtime;

		remainingtime=remainingtime-removequietperiod(currtime,Constants.QUIETSTART,Constants.QUIETEND,Constants.DAYEND);
		remainingtime=remainingtime-removesleepperiod(currtime,Constants.SLEEPSTART,Constants.SLEEPEND,Constants.DAYEND);
		remainingtime=remainingtime-removetimeforsaliva();

		//		Log.mm(TAG,"calculateremainingtime: quiet="+removequietperiod(currtime)/60000+" min, sleep="+removesleepperiod(currtime)/60000+" min, remain="+remainingtime/60000+" min");
		return remainingtime;
	}
	/**
	 * @param type
	 * @param typeIsRunning
	 */
	private void reschedulePeriodicEMA() {
		long currtime=System.currentTimeMillis();
		long remainingtime=calculateRemainingTime();
		Random randomGenerator = new Random();
		long randomlong= randomGenerator.nextLong()%7;
		randomlong-=3;
		randomlong=randomlong*60*1000;

		long remainingbudget=budgeter.getremainingBudget();
		Log.ema_alarm("IS", "PeriodicEMA: remainingbudget="+remainingbudget+" remainingtime="+remainingtime/(1000*60)+" minutes");

		if(remainingbudget<=0) return ;
		//		if(remainingtime<=0) return;
		if(remainingtime<=0) remainingtime=0;
		PERIODIC_INTERVAL=remainingtime/remainingbudget;

		if(currtime+PERIODIC_INTERVAL+randomlong>currtime)
			nextPeriodicTime=currtime+PERIODIC_INTERVAL+randomlong;
		else
			nextPeriodicTime=currtime+PERIODIC_INTERVAL+Math.abs(randomlong);

		long x=AlarmScheduler.getInstance(getBaseContext()).findNearAlarm();
		if(Math.abs(nextPeriodicTime-(x+currtime))<15*60*1000){
			nextPeriodicTime=x+currtime+18*60*1000;
		}
		Log.ema_alarm("IS", "PeriodicEMA: "+Constants.millisecondToDateTime(nextPeriodicTime));
		scheduler.removeCallbacks(launchScheduledInterview);
		if(nextPeriodicTime-currtime<=0)
			scheduler.post(launchScheduledInterview);
		else
			scheduler.postDelayed(launchScheduledInterview, nextPeriodicTime-currtime);
	}

	// BEGIN CONTEXT CHANGE HANDLING CODE


	/*
	 * Process context information and schedules context triggered interviews
	 * accordingly
	 */
	private synchronized void processContext(int modelID, int value) {
		int who=-1;
		this.activeModelsString=Integer.toString(modelID);

		for (int i=0;i<models.length;i++){
			if(models[i].modelID==modelID){
				who=i;
				break;
			}
		}		
		if(modelID==Constants.MODEL_COLLECT_SALIVA){
			//			sleepActive=false;quietActive=false;graceActive=false;
			scheduler.removeCallbacks(launchSalivaInterview);
			scheduler.post(launchSalivaInterview);
		}
		else{
			int numEMA = DatabaseLogger.getInstance(this).getNumEMAsWithoutSaliva(Constants.DAYSTART, Constants.DAYEND);
			String tt="processContext() modelID="+modelID+", numEMA="+numEMA+"MAX_DAILY_EMA="+MAX_DAILY_EMA;
			Log.mm(TAG,tt);
			////////// For handling self report ///////////
			if(modelID >= Constants.MODEL_SELF_START && modelID<=Constants.MODEL_SELF_END)
			{
				if(sleepActive==true || quietActive==true || graceActive==true) return;

				if( numEMA<MAX_DAILY_EMA && triggerer.trigger(modelID,System.currentTimeMillis())){
					if(who!=-1){
						Log.ema_alarm("I","processcontext() delay="+models[who].triggerDelay+" modelname="+models[who].modelName);
						Constants.INDEX_CURRENT_EMA_QUESTION_FILENAME=models[who].indexQuestionFile;
						Log.ema_alarm("IS", "ContextChange: "+(models[who].triggerDelay/(1000*60))+" minutes");
						scheduler.removeCallbacks(launchScheduledInterview);
						scheduler.removeCallbacks(launchContextChangeInterview);
						scheduler.postDelayed(launchContextChangeInterview,models[who].triggerDelay);
					}
				}
				return;
			}
		}
	}
	// END CONTEXT CHANGE HANDLING CODE

	/* RUNNABLES TO HANDLE SCHEDULING TIMES */

	private Runnable launchScheduledInterview = new Runnable() {
		public void run() {
			long currtime=System.currentTimeMillis();
			activeModelsString="";
			Log.mm("mm", "launchSchedulerInterview - Start");
			Log.ema_alarm("", "Periodic: sleepActive:"+String.valueOf(sleepActive)+"quietActive:"+String.valueOf(quietActive)+"graceActive:"+String.valueOf(graceActive)+" diff:"+(currtime-dataLogger.getLastEMA_completed_or_prompted())+" graceperiod: "+GRACE_PERIOD);
			if(sleepActive==true || quietActive==true || graceActive==true || (currtime-dataLogger.getLastEMA_completed_or_prompted())<GRACE_PERIOD){
				reschedulePeriodicEMA();
				return;
			}
			Log.ema_alarm("", "Periodic: Here");
			int numEMAs = dataLogger.getNumEMAsWithoutSaliva(Constants.DAYSTART, Constants.DAYEND);
			if (!interviewRunning && numEMAs < MAX_DAILY_EMA && Constants.ALARMRUNNING==false) {
				if(isvalidforema(21)==false){
					lastInterviewTimeTry=currtime;
					reschedulePeriodicEMA();
				}
				else{
					interviewRunning = true;
					Constants.INTERVIEWRUNNING=true;
					lastInterviewTime = System.currentTimeMillis();
					lastInterviewTimeTry=lastInterviewTime;
					launchType = EMALogConstants.TYPE_PERIODIC;

					Constants.INDEX_CURRENT_EMA_QUESTION_FILENAME=Constants.INDEX_DEFAULT_EMA_QUESTION_FILENAME;

					// startActivityForResult(interviewIntent, INTERVIEW_REQUEST);
					startActivity(interviewIntent);
					enterGracePeriod(currtime+GRACE_PERIOD);
				}
			}
			else
				reschedulePeriodicEMA();
		}
	};
	private Runnable resetNewDay = new Runnable() {
		public void run() {
			if(Constants.ALARMRUNNING==true || interviewRunning)
				scheduler.postDelayed(resetNewDay, 60*1000);
			else
				resetInterviewScheduler();
		}
	};
	private Runnable launchSalivaInterview = new Runnable() {
		public void run() {
			long currtime=System.currentTimeMillis();
			if(interviewRunning || currtime-dataLogger.getLastEMA_completed_or_prompted()<5*60*1000){
				reschedulePeriodicEMA();				
				return;
			}
			if (!interviewRunning  && Constants.ALARMRUNNING==false) {
				interviewRunning = true;
				Constants.INTERVIEWRUNNING=true;
				lastInterviewTime = System.currentTimeMillis();
				lastInterviewTimeTry=lastInterviewTime;
				launchType = EMALogConstants.TYPE_CONTEXT_CHANGE;
				startActivity(interviewIntent);
				enterGracePeriod(currtime+GRACE_PERIOD);
			}
			//}
		}
	};

	private Runnable launchContextChangeInterview = new Runnable() {
		public void run() {
			long currtime=System.currentTimeMillis();

			if(sleepActive==true || quietActive==true || graceActive==true || (currtime-dataLogger.getLastEMA_completed_or_prompted())<GRACE_PERIOD) {
				reschedulePeriodicEMA();				
				return;
			}
			long diff=AlarmScheduler.getInstance(getBaseContext()).findNearAlarm();
			if (diff<10*60*1000) return;

			if (!interviewRunning  && Constants.ALARMRUNNING==false) {
				if(isvalidforema(21)==false){
					lastInterviewTimeTry=currtime;
					lastInterviewTime=lastInterviewTimeTry;
					reschedulePeriodicEMA();
				}
				else{
					interviewRunning = true;
					Constants.INTERVIEWRUNNING=true;
					lastInterviewTime = System.currentTimeMillis();
					lastInterviewTimeTry=lastInterviewTime;
					launchType = EMALogConstants.TYPE_CONTEXT_CHANGE;
					startActivity(interviewIntent);
					enterGracePeriod(currtime+GRACE_PERIOD);
				}
			}
		}
	};

	private Runnable QUIETTIME = new Runnable() {
		public void run() {
			stopInterviewScheduler();
			quietActive=true;
			long restartTime = Constants.QUIETEND - System.currentTimeMillis();
			Log.ema_alarm("IS", "(QuietEnd) RestartScheduler: "+Constants.millisecondToDateTime(Constants.QUIETEND));
			scheduler.removeCallbacks(restartScheduler);
			scheduler.postDelayed(restartScheduler, restartTime);
		}
	};

	private Runnable SLEEPTIME = new Runnable() {
		public void run() {
			long curtime=System.currentTimeMillis();
			stopInterviewScheduler();
			sleepActive=true;
			Log.ema_alarm("IS", "(SleepEnd) RestartScheduler: "+Constants.millisecondToDateTime(Constants.SLEEPEND));			
			scheduler.removeCallbacks(restartScheduler);
			if(Constants.SLEEPEND-curtime<=0)
				scheduler.post(restartScheduler);
			else
				scheduler.postDelayed(restartScheduler, Constants.SLEEPEND-curtime);
		}
	};
	private Runnable restartScheduler = new Runnable() {
		public void run() {
			startInterviewScheduler();
		}
	};
	/* BEGIN IMPLEMENT THE SCHEDULER SERVICE INTERFACE */
	private ISchedulerService.Stub schedulerService = new ISchedulerService.Stub() {

		public void interviewInterrupted(Bundle state) throws RemoteException {

			// keyguard.reenableKeyguard();
			Intent restartInterview = new Intent(getBaseContext(),
					Interview.class);
			restartInterview.putExtra("STATE", state);
			restartInterview.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(restartInterview);

		}

		public void interviewCompleted(Intent result) throws RemoteException {

			long promptTime, delayTime, startTime;
			String[] delayResponses;
			long[] responseTimes;
			long[] delayResponseTimes;
			int emaStatus;
			String[] interviewResponses;

			int requestCode = result.getIntExtra("interviewType", -1);
			//String interviewResult, logEntry;

			// extract the common log data between interview types
			if (requestCode == INTERVIEW_REQUEST) {
				promptTime = result.getLongExtra(EMALogConstants.PROMPT_TIME, -1);
				delayTime = result.getLongExtra(EMALogConstants.DELAY_TIME, -1);
				startTime = result.getLongExtra(EMALogConstants.START_TIME, -1);
				emaStatus = result.getIntExtra(EMALogConstants.EMA_STATUS, -1);
				delayResponseTimes = result.getLongArrayExtra(EMALogConstants.DELAY_RESPONSE_TIMES);
				responseTimes = result.getLongArrayExtra(EMALogConstants.RESPONSE_TIMES);
				interviewResponses = null;
				delayResponses = new String[delayResponseTimes.length];
				for (int i=0; i<delayResponseTimes.length; i++) {
					Serializable response = result.getSerializableExtra(EMALogConstants.DELAY_RESPONSES+i);
					if (response == null) {
						delayResponses[i] = "";
					}
					else {
						try {
							delayResponses[i] = (String)response;
						} catch(ClassCastException e) {
							delayResponses[i] = "" + (Integer)response;
						}						
					}
				}

				//}
				//StringBuilder sb;
				Log.i("onActivityResult",
						"Scheduler received result from Interview");
				// retrieve and format the result form the intent object
				switch (requestCode) {
				case INTERVIEW_REQUEST:
					// if (resultCode == Activity.RESULT_CANCELED) {
					// // something bad happened
					// Log.e("onActivityResult", "Interview failed!!");
					//					
					// } else {
					// received a result from the interview
					Log.i("onActivityResult", "Interview returned correctly");
					interviewResponses = new String[responseTimes.length];
					for (int i=0; i<responseTimes.length; i++) {
						Serializable response = result.getSerializableExtra(EMALogConstants.RESPONSES+i);
						if (response == null) {
							interviewResponses[i] = "";
						}
						else {
							try {
								interviewResponses[i] = (String)response;
							} catch(ClassCastException e) {
								interviewResponses[i] = "" + (Integer)response;
							}
						}						
					}
					break;
				default:
					//logEn	try = null;
				}
				try {
					if (requestCode == INTERVIEW_REQUEST && inferenceService != null) {
						inferenceService.writeEMALog(launchType, activeModelsString, emaStatus, promptTime, delayTime, delayResponses, delayResponseTimes, startTime, interviewResponses, responseTimes);						
					}

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// reset state variables
			String[] id = activeModelsString.split(" ");
			String tt="OK\t: (InterviewScheduler)_InterviewCompleted() id.length="+id.length+"id[0]="+id[0];
			Log.mm(TAG,tt);
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", tt,System.currentTimeMillis());}	


			if (id[0].equals("")) {
				budgeter.updateBudget(null);
			}
			else {
				budgeter.updateBudget(id[0]);
			}
			interviewRunning = false;
			Constants.INTERVIEWRUNNING=false;
			// keyguard.reenableKeyguard();
		}

		public void registerCallback(IInterviewCancellationCallback interview)
				throws RemoteException {

			interviewCallback = interview;

		}

		public void unregisterCallback(IInterviewCancellationCallback interview)
				throws RemoteException {

			if (interviewCallback.equals(interview))
				interviewCallback = null;
		}

		public void reenableInterviews() throws RemoteException {
			scheduler.removeCallbacks(restartScheduler);
			Log.mm("IS", "ReenableInterview: now");
			scheduler.removeCallbacks(launchScheduledInterview);
			scheduler.post(launchScheduledInterview);
		}
	};

	/* END IMPLEMENT THE SCHEDULER SERVICE INTERFACE */

	@Override
	public IBinder onBind(Intent arg0) {
		return schedulerService;
	}

	public static InterviewScheduler getInstance() {
		return INSTANCE;
	}

	private EMABudgeter budgeter = null;
	private EMATriggerer triggerer = null;

	void loadEMAConfig()
	{
		File root = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();
			File setupFile = new File(dir, Constants.EMA_CONFIG_FILENAME);
			if (!setupFile.exists()){
				//				createAlertDialog("\""+Constants.NETWORK_CONFIG_FILENAME+"\" is not found");
				return;
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(setupFile);

			NodeList minTimeEMAList = doc.getElementsByTagName("min_time_between_ema");
			Element minTimeEMAElement = (Element)minTimeEMAList.item(0); 
			NodeList nodeminTimeEMAList = minTimeEMAElement.getChildNodes();
			int minTimeEMA = Integer.parseInt(((Node)nodeminTimeEMAList.item(0)).getNodeValue().trim());
			GRACE_PERIOD=minTimeEMA*MINUTE_MILLIS;

			NodeList maxNumberEMAsList = doc.getElementsByTagName("max_number_emas");
			Element maxNumberEMAsElement = (Element)maxNumberEMAsList.item(0);
			NodeList nodemaxNumberEMAsList = maxNumberEMAsElement.getChildNodes();
			int maxNumberEMAs = Integer.parseInt(((Node)nodemaxNumberEMAsList.item(0)).getNodeValue().trim());
			MAX_DAILY_EMA=maxNumberEMAs;

			NodeList timeoutList = doc.getElementsByTagName("starttimeout");
			Element timeoutElement = (Element)timeoutList.item(0); 
			NodeList nodetimeoutList = timeoutElement.getChildNodes();
			long timeout = Long.parseLong(((Node)nodetimeoutList.item(0)).getNodeValue().trim());
			Constants.START_TIMEOUT=timeout*60*1000;
			timeoutList = doc.getElementsByTagName("interviewtimeout");
			timeoutElement = (Element)timeoutList.item(0); 
			nodetimeoutList = timeoutElement.getChildNodes();
			timeout = Long.parseLong(((Node)nodetimeoutList.item(0)).getNodeValue().trim());
			Constants.INTERVIEW_TIMEOUT=timeout*60*1000;

			//PERIODIC_INTERVAL=periodicEMAInterval*MINUTE_MILLIS;

			NodeList list = doc.getElementsByTagName("beeptime");
			Element element = (Element)list.item(0); 
			NodeList lists = element.getChildNodes();
			int beeptime = Integer.parseInt(((Node)lists.item(0)).getNodeValue().trim());
			Constants.BEEP_COUNT=beeptime*60;

			list = doc.getElementsByTagName("userdelay");
			element = (Element)list.item(0); 
			lists = element.getChildNodes();
			long maxdelay = Long.parseLong(((Node)lists.item(0)).getNodeValue().trim());
			Constants.USER_DELAY=maxdelay*60*1000;


			NodeList list_emaFiles = doc.getElementsByTagName("emafile");
			int totalFiles=0;
			if (list_emaFiles!=null)
				totalFiles =  list_emaFiles.getLength();
			Constants.EMA_QUESTION_FILENAME=new String[totalFiles];
			for(int iterator=0; iterator<totalFiles ; iterator++){
				Node eventNode = list_emaFiles.item(iterator);
				if(eventNode!=null && eventNode.getNodeType() == Node.ELEMENT_NODE){
					Element eventElement = (Element)eventNode;

					NodeList List = eventElement.getElementsByTagName("filename");
					Element Element = (Element)List.item(0);
					NodeList Lists = Element.getChildNodes();
					Constants.EMA_QUESTION_FILENAME[iterator] = ((Node)Lists.item(0)).getNodeValue().trim();
				}
			}

			list = doc.getElementsByTagName("defaultemafiles");
			element = (Element)list.item(0); 
			lists = element.getChildNodes();
			String filename = (((Node)lists.item(0)).getNodeValue().trim());
			for(int ii=0;ii<Constants.EMA_QUESTION_FILENAME.length;ii++){
				if(Constants.EMA_QUESTION_FILENAME[ii].equals(filename)){
					Constants.INDEX_DEFAULT_EMA_QUESTION_FILENAME=ii;
					break;
				}
			}


			NodeList listOfEvents = doc.getElementsByTagName("event");
			int totalEvents=0;
			if (listOfEvents!=null)
				totalEvents =  listOfEvents.getLength();
			models = new Model[totalEvents];

			for(int iterator=0; iterator<totalEvents ; iterator++){
				Node eventNode = listOfEvents.item(iterator);
				if(eventNode!=null && eventNode.getNodeType() == Node.ELEMENT_NODE){
					Element eventElement = (Element)eventNode;

					NodeList List = eventElement.getElementsByTagName("model");
					Element Element = (Element)List.item(0);
					NodeList Lists = Element.getChildNodes();
					String model = ((Node)Lists.item(0)).getNodeValue().trim();

					List = eventElement.getElementsByTagName("budget");
					Element = (Element)List.item(0); 
					Lists = Element.getChildNodes();
					int budget = Integer.parseInt(((Node)Lists.item(0)).getNodeValue().trim());

					List = eventElement.getElementsByTagName("expected_number");
					Element = (Element)List.item(0); 
					Lists = Element.getChildNodes();
					int expectedNumber = Integer.parseInt(((Node)Lists.item(0)).getNodeValue().trim());

					List = eventElement.getElementsByTagName("delay");
					Element = (Element)List.item(0); 
					Lists = Element.getChildNodes();
					int delay = Integer.parseInt(((Node)Lists.item(0)).getNodeValue().trim());

					List = eventElement.getElementsByTagName("level");
					Element = (Element)List.item(0); 
					Lists = Element.getChildNodes();
					String level = ((Node)Lists.item(0)).getNodeValue().trim();

					int indexfile=Constants.INDEX_DEFAULT_EMA_QUESTION_FILENAME;
					List = eventElement.getElementsByTagName("filename");
					if(List.getLength()!=0){
						Element = (Element)List.item(0); 
						Lists = Element.getChildNodes();
						filename = ((Node)Lists.item(0)).getNodeValue().trim();
						for(int ii=0;ii<Constants.EMA_QUESTION_FILENAME.length;ii++){
							if(Constants.EMA_QUESTION_FILENAME[ii].equals(filename)){
								indexfile=ii;
								break;
							}
						}
					}
					int modelID=Constants.getModelToIndex(model);
					models[iterator]= new Model(modelID,model, budget, expectedNumber,delay,level,indexfile);

					//                     questions[iterator]= new Question(iterator, qstring, rid, cqid, crc, ismcq);
					//					Log.mm(TAG,"EMA:"+iterator+" modelID="+modelID+" model="+model+"Budget="+budget+" expectedNumber="+expectedNumber+" delay="+delay+" level="+level);

					/*here the arrays to keep the components for the questions will be filled*/
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("LabStudyActivity",e.getMessage());
			e.printStackTrace();
		}
	}

	private void initEMABudget() {
		String tt="initEMABudget(IS)";
		int remainingbudget=0;
		budgeter = new EMABudgeter();
		loadEMAConfig();
		budgeter.setTotalBudget(MAX_DAILY_EMA);
		tt+=" MAXEMA="+String.valueOf(MAX_DAILY_EMA);
		remainingbudget=MAX_DAILY_EMA-dataLogger.getNumEMAsWithoutSaliva(Constants.DAYSTART,Constants.DAYEND);
		tt+=" remainingbudget="+remainingbudget;
		budgeter.setRemainingBudget(remainingbudget);
		budgeter.setMinTimeBeforeNext(GRACE_PERIOD);
		for(int i=0;i<models.length;i++){
			budgeter.setTotalItemBudget(Integer.toString(models[i].modelID),models[i].budget);
			budgeter.setRemainingItemBudget(Integer.toString(models[i].modelID), models[i].budget-dataLogger.getNumContextEMAs(models[i].modelID,Constants.DAYSTART,Constants.DAYEND));
			tt+=" model="+String.valueOf(models[i].modelID)+" budget="+String.valueOf(models[i].budget)+" remaining="+String.valueOf(models[i].budget-dataLogger.getNumContextEMAs(models[i].modelID,Constants.DAYSTART,Constants.DAYEND));
		}
		Log.mm(TAG,tt);
	}
	private void initEMATriggerer(EMABudgeter budgeter, Model[] models){
		triggerer = new EMATriggerer(budgeter,models,Constants.DAYSTART,Constants.DAYEND);
	}

	public static final int NO_INCENTIVE_SCHEME = 3;
	public static final int UNIFORM_INCENTIVE_SCHEME = 0;
	public static final int VARIABLE_INCENTIVE_SCHEME = 1;
	public static final int HIDDEN_INCENTIVE_SCHEME = 2;
	public static final int UNIFORM_AND_BONUS_INCENTIVE_SCHEME = 4;	

	public static int INCENTIVE_SCHEME = NO_INCENTIVE_SCHEME;//UNIFORM_AND_BONUS_INCENTIVE_SCHEME;

	private void initIncentiveManager() {
		if (INCENTIVE_SCHEME == NO_INCENTIVE_SCHEME) {
			incentives = null;
			return;
		}

		incentives = new EMAIncentiveManager();
		incentives.setNumQuestions(getContent().getNumberQuestions(true));

		if (INCENTIVE_SCHEME != UNIFORM_AND_BONUS_INCENTIVE_SCHEME)
			incentives.setPerQuestion(true);
		else
			incentives.setPerQuestion(false);

		// on average, this distribution leads to EMAs worth ~$1.10, a little high compared to the uniform max of $1.04
		// produces an expected value of ~$1.04
		//		float[] distribution = {
		//				0.281818182f,      62/220
		//				0.213636364f,      47/220
		//				0.172727273f,      38/220
		//				0.122727273f,      27/220
		//				0.068181818f,      15/220
		//				0.040909091f,       9/220
		//				0.031818182f,       7/220
		//				0.027272727f,       6/220
		//				0.018181818f,       4/220
		//				0.013636364f,       3/220
		//				0.009090909f};      2/220

		// on average, this distribution leads to EMAs 
		// worth $1.05, much closer to the uniform max of $1.04
		// also happens to produce an expected value of $1.00 (exactly)
		float[] distribution = {
				0.304545455f,    // 67/220
				0.236363636f,    // 52/220
				0.163636364f,    // 36/220
				0.109090909f,    // 24/220
				0.054545455f,    // 12/220
				0.036363636f,    //  8/220
				0.031818182f,    //  7/220
				0.022727273f,    //  5/220
				0.018181818f,    //  4/220
				0.013636364f,    //  3/220
				0.009090909f     //  2/220
		};		

		BigDecimal[] amounts ={
				new BigDecimal("0.50").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("0.75").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.00").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.25").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.50").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("1.75").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.00").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.25").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.50").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("2.75").setScale(2, BigDecimal.ROUND_HALF_EVEN),
				new BigDecimal("3.00").setScale(2, BigDecimal.ROUND_HALF_EVEN)};


		if (INCENTIVE_SCHEME == UNIFORM_AND_BONUS_INCENTIVE_SCHEME) {		
			incentives.setUniform(new BigDecimal(1));
			incentives.setIncentiveVisible(true);		
			incentives.setBonusTime(300000);
			incentives.setBonusAmount(new BigDecimal(1).divide(new BigDecimal(2)));
		}
		else if (INCENTIVE_SCHEME == UNIFORM_INCENTIVE_SCHEME) {
			incentives.setUniform(new BigDecimal(1));
			incentives.setIncentiveVisible(true);
		}
		else if (INCENTIVE_SCHEME == VARIABLE_INCENTIVE_SCHEME) {			
			incentives.setVariable(distribution, amounts);
			incentives.setIncentiveVisible(true);			
		}
		else if (INCENTIVE_SCHEME == HIDDEN_INCENTIVE_SCHEME) {
			incentives.setVariable(distribution, amounts);
			incentives.setIncentiveVisible(false);
		}
		incentives.loadIncentivesEarned();
	}
}
