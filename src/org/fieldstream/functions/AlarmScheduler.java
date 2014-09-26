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

package org.fieldstream.functions;

import org.fieldstream.Constants;
import org.fieldstream.Minnesota_SmokingStudyActivity;
import org.fieldstream.functions.ReadWriteConfigFiles;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.ToneGenerator;
import android.os.Handler;
import android.text.format.Time;

public class AlarmScheduler{

	public static class Alarm {
		public String alarmName;
		public long alarmTime;
		public int isEMA;
		public String emaFilename;
	};
	public static AlarmScheduler INSTANCE = null;
	public Alarm[] alarms;
	int selectedAlarm;
//	boolean processing_confirm=false;
//	boolean processing_saliva=false;

	static long previous_confirmtime=-1;
	static long previous_salivatime=-1;

	public static Context context=null;
	int status ;
	public boolean alarm_on=false;
	AlertDialog.Builder builder_confirm;
	AlertDialog alert_confirm =null;
	AlertDialog.Builder builder_collect;
	AlertDialog alert_collect =null;
	public static int STATUS_CONFIRMED=2;
	public static int STATUS_COLLECT=1;
	public static int STATUS_START=0;


	private static final int BEEP_DURATION=1000;
	//	private static final int SECOND_REMINDER=300*1000; // 5 Minutes

	//	public static final int BEEP_COUNT=60; // 60 = 1 minute beep
	int beepCount=0;
	int fail;
	static final int VOLUME = 100;
	Handler handler =new Handler();
	DatabaseLogger db;

	public static AlarmScheduler getInstance(Context holder) {
		if (INSTANCE == null) {
			context=holder;
			INSTANCE = new AlarmScheduler();
		}
		return INSTANCE;
	}
	AlarmScheduler(){
		db = DatabaseLogger.getInstance(this);
		previous_salivatime=-1;
		previous_confirmtime=-1;
		resetAlarm();
		builder_confirm = new AlertDialog.Builder(Minnesota_SmokingStudyActivity.context);
		builder_confirm.setMessage("Saliva Collection Completed")
		.setCancelable(false)
		.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				long curtime=System.currentTimeMillis();
				if(previous_confirmtime!=-1 && curtime-previous_confirmtime<Constants.CLICK_DURATION) return;
					db.logAnything("alarm_info", "Click: Confirm", System.currentTimeMillis());
					status=STATUS_CONFIRMED;
					previous_confirmtime=curtime;
			}
		});
		builder_collect = new AlertDialog.Builder(Minnesota_SmokingStudyActivity.context);

		builder_collect.setMessage("Please Collect Saliva");
		builder_collect.setCancelable(false);
		builder_collect.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				long curtime=System.currentTimeMillis();
				if(previous_salivatime!=-1 && curtime-previous_salivatime<Constants.CLICK_DURATION) return;
				db.logAnything("alarm_info", "Click: Ok", System.currentTimeMillis());
				status=STATUS_COLLECT;
				alert_confirm=builder_confirm.create();
				alert_confirm.show();
				previous_salivatime=curtime;
			}
		});
	}
	public void stopAlarm()
	{
		handler.removeCallbacks(generateAlarm);
		handler.removeCallbacks(runAlarm);
		alarm_on=false;
		Constants.ALARMRUNNING=false;
	}
	public void resetAlarm()
	{
		stopAlarm();
		setNextAlarm();
	}
	public void setNextAlarm()
	{
		long diff=findNearAlarm();
		Log.ema_alarm("","Next Alarm:"+(diff/(1000*60))+" minute");
		handler.postDelayed(generateAlarm,diff);

	}
	private Runnable runAlarm = new Runnable(){
		@SuppressLint("NewApi")
		public void run(){
			Log.ema_alarm("", "runalarm status="+String.valueOf(status));
			if (status ==STATUS_CONFIRMED){
				Constants.ALARMRUNNING=false;
				if(alarms[selectedAlarm].isEMA==1){
					for(int ii=0;ii<Constants.EMA_QUESTION_FILENAME.length;ii++){
						if(Constants.EMA_QUESTION_FILENAME[ii].equals(alarms[selectedAlarm].emaFilename)){
							Log.mm("aa", alarms[selectedAlarm].emaFilename+" "+ii);
							Constants.INDEX_CURRENT_EMA_QUESTION_FILENAME=ii;//INDEX_DEFAULT_EMA_QUESTION_FILENAME=ii;
							break;
						}
					}
					ContextBus.getInstance().pushNewContext(Constants.MODEL_COLLECT_SALIVA, 1, System.currentTimeMillis(), System.currentTimeMillis());
				}
				return;
			}
			else if (beepCount < Constants.ALARMDURATION){
				if(beepCount==0) {
					Log.mm("", "create alert collect");
					alert_collect=builder_collect.create();
					alert_collect.show();
				}
				beepCount++;
				if(status==STATUS_START){
					ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_DTMF, VOLUME);
					tone.startTone(ToneGenerator.TONE_PROP_PROMPT,4000);
					tone.release();
				}
				handler.postDelayed(this, BEEP_DURATION);
			}
			else{
				fail++;
				if(status==STATUS_START) {
					alert_collect.cancel();
					status=-1;

				}
				else if (status==STATUS_COLLECT) {
					alert_confirm.cancel();
					status=-2;
				}
				if(fail<Constants.REPEATALARM){
					status =STATUS_START;
					beepCount=0;
					handler.postDelayed(runAlarm,Constants.DIFF_BETWEEN_ALARM*1000);
				}
				else{
					Constants.ALARMRUNNING=false;
				}
			}
		}
	};

	public long findNearAlarm(){
		long curtime=System.currentTimeMillis();
		int who=-1;
		for(int day=0;;day++){
			alarms=ReadWriteConfigFiles.getInstance(this).loadAlarms(day);
			for(int i=0;i<alarms.length;i++)
			{
				if(alarms[i].alarmTime<curtime) continue;
				if(who==-1) who=i;
				else if(alarms[i].alarmTime<alarms[who].alarmTime)
					who=i;
			}
			selectedAlarm=who;
			if(who!=-1) break;
		}
		return alarms[who].alarmTime-curtime;
	}

	public String milisecondToTimePrint(long time) {
		Time t = new Time();
		t.set(time);
		return t.format("%I:%M %p");
		//	return format(t.hour) + ":" + pad(t.minute) + (t.hour >=12 ? "pm":"am");
	}
	private Runnable generateAlarm = new Runnable(){
		@SuppressLint("NewApi")
		public void run(){
			if(Constants.INTERVIEWRUNNING==true){
				handler.postDelayed(generateAlarm,1000*60*10);
			}
			else{
				status = STATUS_START;
				beepCount=0;
				fail=0;
				alarm_on=true;
				Constants.ALARMRUNNING=true;
				handler.removeCallbacks(runAlarm);
				handler.postDelayed(runAlarm,BEEP_DURATION);
				setNextAlarm();
			}
		}
	};
}
