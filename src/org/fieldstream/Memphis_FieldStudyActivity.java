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

//@author Mishfaq Ahmed
//@author Patrick Blitz
//@author Somnath Mitra
//@author Andrew Raij


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.gui.ema.IContent;
import org.fieldstream.gui.ema.InterviewData;
import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.incentives.EMAIncentiveManager;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera.Size;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.text.InputFilter.LengthFilter;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug.IntToString;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Memphis_FieldStudyActivity extends BaseActivity {
	// EMA stuff
	private Intent emaScheduler;
	//	private final static String REPORT_CODE = "8812";


	private Button selfReportButton;
	private Intent selfReportIntent;
	private Intent salivaInitIntent;
	private Button salivaInitButton;
	//test

	private TextView label;
	private TextView endofdaylabel;
	private TextView stressorlabel;
	private TextView incentives;
	private TextView salivalabel;
	private TextView wakesleep;
	Handler handler =new Handler();


	//alarm dialog
	static final String START_STRING = "Start";
	static final String DELAY_STRING = "Delay 10 Minutes";

	long alarmarray[]= new long [10];
	int ALARMNO=8;
	long now=0;


//	final static String  alarm[]=new String [10];
	int status ;


	//alarm button
	Button salivaCollectButton;
	Button salivaCancelButton;



	// state constants
	AlertDialog.Builder builder;
	AlertDialog.Builder builder1;
	AlertDialog alert_collect = null;
	AlertDialog alert_confirm =null;

	//long diff=10;
	String test="Nothing";

	//alarm parameters

	private static final int BEEP_DURATION=1000;
	private static final int SECOND_REMINDER=50*1000; // 50 second

	private static final int BEEP_COUNT=20; // 240*1=240 -> 4 minute beep
	int beepCount=0;
	int fail;
	static final int VOLUME = 100;

	@SuppressWarnings("unused")
	private int result;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		long diff=0;
		start=0;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.memphis_fieldstudy_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="Memphis FieldStudy";

		setTitleBar(NOT_CONNECTED);

		//time start

//		loadAlarmConfig();
		loadDeadPeriods();
		resetAlarmTime(offEnd,offStart);
		diff=findNearAlarm();

		handler.postDelayed(generateAlarm,diff);













		// formattedDate have current date/time
		//     Toast.makeText(this, formattedDate, Toast.LENGTH_SHORT).show();


		// Now we display formattedDate value in TextView


		//time end
		//setTitle(R.layout.titlebar);
		//setTitleColor(Color.RED);
		/*		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		if ( customTitleSupported ) {
	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mytitle);
	    }

	    final TextView myTitleText = (TextView) findViewById(R.id.myTitle);
	    if ( myTitleText != null ) {
	        myTitleText.setText("========= NEW TITLE ==========");
	        myTitleText.setBackgroundColor(Color.GREEN);
	    }
		 */

		//		subjectlabel=(TextView)findViewById(R.id.subjectlabel);
		stressorlabel=(TextView)findViewById(R.id.stressorlabel);
		endofdaylabel=(TextView)findViewById(R.id.eodlabel);



		label=(TextView)findViewById(R.id.Label);
		incentives=(TextView)findViewById(R.id.incentives);
		InterviewScheduler.INCENTIVE_SCHEME=InterviewScheduler.UNIFORM_AND_BONUS_INCENTIVE_SCHEME;

		if (InterviewScheduler.INCENTIVE_SCHEME == InterviewScheduler.NO_INCENTIVE_SCHEME)
			incentives.setVisibility(View.INVISIBLE);
		// read config in!
		readConfig();

		selfReportButton =(Button) findViewById(R.id.saliva_init_wake);

		salivaCollectButton=(Button) findViewById(R.id.salivaCollectButton);
		salivaCancelButton=(Button) findViewById(R.id.salivaCancelButton);

		salivaInitButton=(Button) findViewById(R.id.saliva_init_sleep);



		selfReportButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				beepCount=0;
				// TODO Auto-generated method stub
				//System.out.println("he wants to report some event!");
				//choice c = new choice();
				//c.startActivity(intent);
				//	startActivity((Intent)c);
				selfReportIntent = new Intent(getBaseContext(),Memphis_SelfReportEventActivity.class);
				startActivity(selfReportIntent);
				if(Log.DEBUG_MONOWAR) Log.m("Nusrat_saliva","OK\t: Saliva: on click works");

				//handler.postDelayed(setAlarm,BEEP_DURATION);
				//	alert.show();





			}






		});


		salivaInitButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				salivaInitIntent = new Intent(getBaseContext(),Minnesota_SalivaCollectionActivity.class);
				startActivity(salivaInitIntent);

			}

		});


		builder = new AlertDialog.Builder(this);
		builder.setMessage("Please Collect Saliva")
		.setCancelable(false)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				//		        	   handler.removeCallbacks(setAlarm);
				//  beepCount=BEEP_COUNT;
				status=1;
				alert_confirm=builder.create();
				alert_confirm.show();

				//for confirm dialog start

				builder.setMessage("Saliva Collection Complete")
				.setCancelable(false)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						status=2;
						//   finish();

						//  Toast.makeText(this, formattedDate, Toast.LENGTH_SHORT).show();
					}

				});
				//for confirm dialog end

				//  alert_confirm=builder.create();
				//   alert_confirm.show();


				//   finish();
			}

		});


		alert_collect = builder.create();

		updateIncentivesDisplay();

	}




	private Runnable setAlarm = new Runnable(){
		@SuppressLint("NewApi")
		public void run(){

			//			tone.startTone(ToneGenerator.TONE_PROP_BEEP2,1000);

			//			tone.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);




			if (status ==2){
				//set Next Alarm
				//handler.removeCallbacks(setAlarm);
					handler.postDelayed(generateAlarm,findNearAlarm());
					return;

			}
			if (beepCount < BEEP_COUNT){
				if(beepCount==0) alert_collect.show();
				beepCount++;
				if(status==0){

					ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_DTMF, VOLUME);
					tone.startTone(ToneGenerator.TONE_CDMA_LOW_S_X4,4000);
					tone.release();

				}
				handler.postDelayed(this, BEEP_DURATION);

			}
			else{
				fail++;
				if(status==0) {
					alert_collect.cancel();
					status=-1;
				}

				else if (status==1) {
					alert_confirm.cancel();
					status=-2;
				}
				if(fail<2){
					status =0;
					beepCount=0;
					handler.postDelayed(setAlarm,SECOND_REMINDER);

				}
				else{
					// set next alarm
				}
			}












		}

	};



	@Override
	protected void onDestroy() {
		stopService(emaScheduler);
		super.onDestroy();

	}
	long quietStart, quietEnd;
	long offStart, offEnd;
	//	long firstDayStart;
	void writeConfigToFile(long starttime) {
		try {
			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();
			File setupFile = new File(dir, Constants.FIELDINFO_CONFIG_FILENAME);

			BufferedWriter writer = new BufferedWriter(new FileWriter(setupFile));
			writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			writer.write("<fieldstream>\n");
			writer.write("\t<day>\n");
			writer.write("\t\t<starttime type=\"firstday\" start=\"" + starttime + "\" />\n" );
			writer.write("\t</day>\n");
			writer.write("</fieldstream>");

			writer.close();
		}
		catch(Exception e) {
			// TODO Auto-generated catch block
			Log.d("SETUP",e.getMessage());
			e.printStackTrace();

			Toast.makeText(getApplicationContext(), "Error Saving Network Setup", Toast.LENGTH_SHORT).show();
		}

	}

/*	void loadAlarmConfig() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_Alarm","OK\t: Alarm Start");

		File root = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();

			File setupFile = new File(dir, Constants.ALARM_CONFIG_FILENAME);
			if (!setupFile.exists()){
				createAlertDialog("\""+Constants.ALARM_CONFIG_FILENAME+"\" is not found");

				return;
			}
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_Alarm","OK\t: Alarm: file found");

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(setupFile);

			Element xmlroot = dom.getDocumentElement();
			NodeList nodeList=xmlroot.getElementsByTagName("alarm");
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_Alarm","OK\t: element no="+nodeList.getLength());

			if (nodeList.getLength() > 0) {
				Element element = (Element)nodeList.item(0);
				nodeList = element.getElementsByTagName("time");
				for (int i=0; i < nodeList.getLength(); i++) {
					element = (Element)nodeList.item(i);
					String time = element.getFirstChild().getNodeValue();
					alarm[i]=time;
					if(Log.DEBUG_MONOWAR) Log.m("Monowar_Alarm","OK\t: Alarm="+alarm[i]);

				}
			}

		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("LabStudyActivity",e.getMessage());
			e.printStackTrace();
		}
	}
	*/
/*	void loadFirstDayInfo() {
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.FIELDINFO_CONFIG_FILENAME);
		if (!setupFile.exists()){
			writeConfigToFile(System.currentTimeMillis());
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document dom = null;
		try {
			dom = builder.parse(setupFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element xmlroot = dom.getDocumentElement();

		NodeList nodeList = xmlroot.getElementsByTagName("starttime");
		for (int i=0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NamedNodeMap map = node.getAttributes();
			if (map != null) {
				Node type = map.getNamedItem("type");
				Node start = map.getNamedItem("start");

				if (type != null && start != null) {
					if (type.getNodeValue().equalsIgnoreCase("firstday")) {
						//        				firstDayStart = Long.parseLong(start.getNodeValue());
						Constants.FIRSTDAYSTART=Long.parseLong(start.getNodeValue());
					}
				}
			}
		}
	}
*/
	void loadDeadPeriods() {
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.DEAD_PERIOD_CONFIG_FILENAME);
		if (!setupFile.exists())
			return;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document dom = null;
		try {
			dom = builder.parse(setupFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element xmlroot = dom.getDocumentElement();

		NodeList nodeList = xmlroot.getElementsByTagName("period");
		for (int i=0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NamedNodeMap map = node.getAttributes();
			if (map != null) {
				Node type = map.getNamedItem("type");
				Node start = map.getNamedItem("start");
				Node end = map.getNamedItem("end");

				if (type != null && start != null && end != null) {
					if (type.getNodeValue().equalsIgnoreCase("quiet")) {
						quietStart = Long.parseLong(start.getNodeValue());
						quietEnd = Long.parseLong(end.getNodeValue());
					}
					else if (type.getNodeValue().contentEquals("off")) {
						offStart = Long.parseLong(start.getNodeValue());
						offEnd = Long.parseLong(end.getNodeValue());
					}
				}
			}
		}
	}

	private void readConfig() {
		loadDeadPeriods();
//		loadFirstDayInfo();

		emaScheduler = new Intent(getBaseContext(),InterviewScheduler.class);
		emaScheduler.putExtra(Constants.quietStart, quietStart);
		emaScheduler.putExtra(Constants.quietEnd, quietEnd);
		emaScheduler.putExtra(Constants.sleepStart, offStart);
		emaScheduler.putExtra(Constants.sleepEnd, offEnd);
		//		emaScheduler.putExtra(Constants.firstDayStart, firstDayStart);
		switch(InterviewScheduler.INCENTIVE_SCHEME) {
		case InterviewScheduler.UNIFORM_INCENTIVE_SCHEME:
			label.setText("u\n");
			break;
		case InterviewScheduler.VARIABLE_INCENTIVE_SCHEME:
			label.setText("v\n");
			break;
		case InterviewScheduler.HIDDEN_INCENTIVE_SCHEME:
			label.setText("h\n");
			break;
		case InterviewScheduler.NO_INCENTIVE_SCHEME:
		case InterviewScheduler.UNIFORM_AND_BONUS_INCENTIVE_SCHEME:
			label.setText("\n");
			break;
		default:
			label.setText("error");

		}
//for(int i=0;i<8;i++)
		stressorlabel.setText("Saliva Alarm Time "+ milisecondToTimePrint(alarmarray[0])+","+ milisecondToTimePrint(alarmarray[1])+","+ milisecondToTimePrint(alarmarray[2])+","+ milisecondToTimePrint(alarmarray[3])+","+ milisecondToTimePrint(alarmarray[4])+","+ milisecondToTimePrint(alarmarray[5])+","+ milisecondToTimePrint(alarmarray[6])+","+ milisecondToTimePrint(alarmarray[7])+"\n");


	//	wakesleep.setText("Wakeup Time "+ makeTimeString(alarmarray[0])+ "Sleep Time "+ makeTimeString(alarmarray[7]));

		endofdaylabel.setText("Wakeup Time "+milisecondToTimePrint(alarmarray[0])+", Sleep Time" + milisecondToTimePrint(alarmarray[7]) + "\n");

		startService(emaScheduler);

		// callback to disable stress inference when the time is right!
		Log.i("onActivityResult", "started the scheduler service");
		CharSequence text = "Configuration successfully loaded";
	}

	/* This is called when the app is killed. */
	@Override
	public void onResume() {
		super.onResume();
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (FieldStudyActivity)_onResume().........."+inferenceService);

		updateIncentivesDisplay();
	}

	private void updateIncentivesDisplay() {
		if (InterviewScheduler.INCENTIVE_SCHEME == InterviewScheduler.NO_INCENTIVE_SCHEME) {
			incentives.setText("");
			return;
		}

		double total = 0.0;
		try {
			if(inferenceService==null){
				DatabaseLogger db=DatabaseLogger.getInstance(this);
				total=db.getTotalIncentivesEarned();
			}
			else
				total = inferenceService.getTotalIncentivesEarned();
		} catch (RemoteException e) {
			// TODO Auto-g-enerated catch block
			e.printStackTrace();
		}

		String text=NumberFormat.getCurrencyInstance().format(total);

		incentives.setText("So far, you've earned:\n\n" + text);
	}

	/* END ANDROID LIFE CYCLE */


	private static String makeTimeString(long time) {
		Time t = new Time();
		t.set(time);
		return t.format("%D %I:%M %p");
		//	return format(t.hour) + ":" + pad(t.minute) + (t.hour >=12 ? "pm":"am");

	}
	private static String milisecondToTime(long time) {
		Time t = new Time();
		t.set(time);
		return t.format("%H:%M:%S");
		//	return format(t.hour) + ":" + pad(t.minute) + (t.hour >=12 ? "pm":"am");

	}

	private static String milisecondToTimePrint(long time) {
		Time t = new Time();
		t.set(time);
		return t.format("%I:%M %p");
		//	return format(t.hour) + ":" + pad(t.minute) + (t.hour >=12 ? "pm":"am");

	}
	private long findNearAlarm(){
		long diff=0;
		java.util.Date rightnow;
		int i;
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

		String formattedDate = df.format(c.getTime());
		java.text.DateFormat dateformat = new java.text.SimpleDateFormat("hh:mm:ss");
		try {
			rightnow = dateformat.parse(formattedDate);
			now=rightnow.getTime();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(i=0; i<ALARMNO;i++){
			if(now<alarmarray[i])
				break;
		}
		if(i==ALARMNO){
			i=0;
		}
		diff=alarmarray[i]-now;
		if(diff <0){
			diff=diff+ (24*60*60*1000);
		}
		return diff;

	}
	public void resetAlarmTime(long w,long s){
		String wakeuptime=null;
		String sleeptime=null;
		long diff=0;
		wakeuptime=milisecondToTime(w);
		if(Log.DEBUG_MONOWAR) Log.m("Nusrat_wake","OK\t: Wake:"+wakeuptime);
		sleeptime=milisecondToTime(s);

		//  System.out.println("Current time => "+c.getTime());
		java.util.Date wake;
		java.util.Date sleep;
		java.util.Date noon;
		java.util.Date afternoon;
		java.util.Date night;
		java.text.DateFormat dateformat = new java.text.SimpleDateFormat("hh:mm:ss");


		try {
			wake=dateformat.parse(wakeuptime);

			wake.toString();
			sleep=dateformat.parse(sleeptime);


			alarmarray[0]=  wake.getTime();

			noon=dateformat.parse("16:35:00");
			afternoon=dateformat.parse("16:36:00");
			night=dateformat.parse("16:40:00");


			alarmarray[4]=noon.getTime();
			alarmarray[5]=afternoon.getTime();
			alarmarray[6]=night.getTime();
			alarmarray[7]=sleep.getTime();
			diff=findNearAlarm();

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		alarmarray[1]=alarmarray[0]+ 1*60*1000;
		alarmarray[2]=alarmarray[1]+ 2*60*1000;
		alarmarray[3]=alarmarray[2]+ 1*60*1000;


		for(int i=4; i<8; i++){
			if (alarmarray[i]>alarmarray[7]){
				alarmarray[i]=alarmarray[7];

			}
		}


		for(int i=0; i<8;i++)
			if(Log.DEBUG_MONOWAR) Log.m("Nusrat_Alarmarray","nusrat "+i+"="+alarmarray[i]);

	}
	public long getSleepTime(){
		return alarmarray[7];
	}

public long getWakeTime(){
	return alarmarray[0];
}
	private Runnable generateAlarm = new Runnable(){
		@SuppressLint("NewApi")
		public void run(){
			status =0;
			beepCount=0;
			fail=0;
			handler.postDelayed(setAlarm,BEEP_DURATION);



		}

	};
}
