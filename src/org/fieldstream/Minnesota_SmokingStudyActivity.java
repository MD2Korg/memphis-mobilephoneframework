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
package org.fieldstream;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.fieldstream.functions.AlarmScheduler;
import org.fieldstream.functions.ReadWriteConfigFiles;
import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Minnesota_SmokingStudyActivity extends BaseActivity {
	// EMA stuff
	private Intent emaScheduler;
	public static int COUNT=0;
	public static Context context;
	//	private final static String REPORT_CODE = "8812";
	private Button selfReportButton;
	private Intent selfReportIntent;
	private Intent wakeSleepIntent;
	private Button wakeSleepButton;
	private Button silentButton;
	private Intent silentIntent;
	//alarm dialog
//	static final String START_STRING = "Start";
//	static final String DELAY_STRING = "Delay 10 Minutes";
	private AlarmScheduler alarmScheduler;
	static final int PICK_SILENT_PERIOD = 1;
	long now=0;
	String test="Nothing";

	@SuppressWarnings("unused")
	private int result;
/*	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}
	*/
/*
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

	    super.onWindowFocusChanged(hasFocus);
	}
*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		COUNT++;
		Log.mm("", "Smoking activity: "+COUNT);
		start=0;
		context=this;
		super.onCreate(savedInstanceState);
		DatabaseLogger.getInstance(this).logAnything("prog_stat", "start", System.currentTimeMillis());
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.minnesota_smokingstudy_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

		titleText="Minnesota FieldStudy";

		setTitleBar(NOT_CONNECTED);

		loadEMAScheduler();
		if(AlarmScheduler.INSTANCE!=null){
			AlarmScheduler.getInstance(this).stopAlarm();
			AlarmScheduler.INSTANCE=null;
		}
		alarmScheduler=AlarmScheduler.getInstance(Minnesota_SmokingStudyActivity.this);

		selfReportButton =(Button) findViewById(R.id.report_event_button);

		selfReportButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				//System.out.println("he wants to report some event!");
				//choice c = new choice();
				//c.startActivity(intent);
				//	startActivity((Intent)c);
				selfReportIntent = new Intent(getBaseContext(),SelfReportEventActivity.class);
				startActivity(selfReportIntent);
				if(Log.DEBUG_MONOWAR) Log.m("Nusrat_saliva","OK\t: Saliva: on click works");
				//handler.postDelayed(setAlarm,BEEP_DURATION);
				//	alert.show();
			}
		});

		wakeSleepButton=(Button) findViewById(R.id.wake_sleep_button);
		wakeSleepButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				wakeSleepIntent = new Intent(getBaseContext(),Minnesota_SalivaCollectionActivity.class);
				startActivityForResult(wakeSleepIntent,2);
			}
		});
		silentButton=(Button) findViewById(R.id.silent_button);
		silentButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				silentIntent = new Intent(getBaseContext(),Minnesota_SilentActivity.class);
				startActivityForResult(silentIntent,PICK_SILENT_PERIOD);
			}
		});

		setGUI();
		//		updateIncentivesDisplay();
		disableUnnecessarySensors();

//		WindowManager.LayoutParams layout = getWindow().getAttributes();
//		layout.screenBrightness = 0.15F;
//		getWindow().setAttributes(layout);
	}
	private void disableUnnecessarySensors() {
		String message = "";
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager.isWifiEnabled()) {
			message = message + "Wifi";
			wifiManager.setWifiEnabled(false);
		}
//		Settings.System.putInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON,1);
		/*// Not Available in Api Level 4
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
		}*/

		if(message.length()>0) {
			message = message + " turned off due to optimize power";
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}

	// Call Back method  to get the Message form other Activity    override the method
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);


		// check if the request code is same as what is passed  here it is 2
		if(requestCode==2 && resultCode==RESULT_OK){
			// fetch the message String
			String message=data.getStringExtra("MESSAGE");
			if(message==null) return;
			if(message.equals("sleepend") || message.equals("sleepstart")){
				Log.ema_alarm("", "wakeup sleep now: msg="+message);
				AlarmScheduler.getInstance(this).resetAlarm();
				stopService(emaScheduler);
				loadEMAScheduler();
				setGUI();
			}
		}
		else if(requestCode==PICK_SILENT_PERIOD){
			String message=data.getStringExtra("MESSAGE");
			Log.mm("", "pick="+message);
			if(Integer.valueOf(message)!=-1){
				Log.mm("", "turnoff ema");
				stopService(emaScheduler);
				loadEMAScheduler();
			}
		}

	}
	@Override
	protected void onDestroy() {
		Log.ema_alarm("", "ondestroy");
		stopService(emaScheduler);
		alarmScheduler.stopAlarm();
		AlarmScheduler.INSTANCE=null;
		DatabaseLogger.getInstance(this).logAnything("prog_stat", "end", System.currentTimeMillis());
		super.onDestroy();

	}
	//	long firstDayStart;
	private void loadEMAScheduler(){
		InterviewScheduler.INCENTIVE_SCHEME=InterviewScheduler.NO_INCENTIVE_SCHEME;
		if(emaScheduler!=null){
			stopService(emaScheduler);
		}
		emaScheduler = new Intent(getBaseContext(),InterviewScheduler.class);
		startService(emaScheduler);

	}
	/* This is called when the app is killed. */
	@Override
	public void onResume() {
		context=this;
		super.onResume();
		setGUI();
	}

	/* END ANDROID LIFE CYCLE */
	public void setGUI()
	{
		TextView wakeup=(TextView)findViewById(R.id.TV_wakeuptime);
		TextView sleep=(TextView)findViewById(R.id.TV_sleeptime);
		TextView saliva=(TextView)findViewById(R.id.TV_saliva);
		ReadWriteConfigFiles.getInstance(this).loadDeadPeriodsDB();
		String s="";
		String wakestr="",sleepstr="";
		for (int i=0;i<alarmScheduler.alarms.length;i++){
			if(i!=0 && i%2==0) s=s+"\n";
			if(i%2==1) s=s+", ";
			s=s+Constants.millisecondToTime(alarmScheduler.alarms[i].alarmTime);
//			if(alarmScheduler.alarms[i].alarmName.equals("wakeup")) sleepEnd=alarmScheduler.alarms[i].alarmTime;
//			if(alarmScheduler.alarms[i].alarmName.equals("sleep")) sleepStart=alarmScheduler.alarms[i].alarmTime;
		}
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);cal.set(Calendar.MINUTE, 0);cal.set(Calendar.SECOND, 0);cal.set(Calendar.MILLISECOND, 0);
		long today=cal.getTimeInMillis();
		long waketime=DatabaseLogger.getInstance(this).getdeadperiod("sleepend", today, today+Constants.DAYMILLIS);
		if(waketime==-1) waketime=ReadWriteConfigFiles.getInstance(this).getdefaulttime(0,8);
		long sleeptime=DatabaseLogger.getInstance(this).getdeadperiod("sleepstart", today, today+Constants.DAYMILLIS);
		if(sleeptime==-1) sleeptime=ReadWriteConfigFiles.getInstance(this).getdefaulttime(0,22);

		wakestr=Constants.millisecondToTime(waketime);
		sleepstr=Constants.millisecondToTime(sleeptime);

		saliva.setText(s);
		wakeup.setText(wakestr);
		sleep.setText(sleepstr);
	}
}
