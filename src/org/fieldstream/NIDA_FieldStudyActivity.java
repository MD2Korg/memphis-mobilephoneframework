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
import java.text.NumberFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;

import android.text.format.Time;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NIDA_FieldStudyActivity extends BaseActivity {
	// EMA stuff
	private Intent emaScheduler;
//	private final static String REPORT_CODE = "8812";


	private Button selfReportButton;
	private Intent selfReportIntent;
	//test

	private TextView label;
//	private TextView subjectlabel;
	private TextView endofdaylabel;
	private TextView stressorlabel;
	private TextView incentives;
//	private String titleText="FieldStudy(NIDA)";
//	public static final int CONNECTED=1;
//	public static final int NOT_CONNECTED=0;

	@SuppressWarnings("unused")
	private int result;
/*	void changeTitleBar(int connection)
	{
		if(connection==CONNECTED){
			TextView title=(TextView) findViewById(R.id.myTitle);
			title.setText(titleText);
			ImageView image=(ImageView) findViewById(R.id.titleImage);
			image.setImageResource(R.drawable.circle_green);
		}
		else if (connection==NOT_CONNECTED){
			TextView title=(TextView) findViewById(R.id.myTitle);
			title.setText(titleText);
			ImageView image=(ImageView) findViewById(R.id.titleImage);
			image.setImageResource(R.drawable.circle_red);
		}
	}
*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		start=0;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.nida_fieldstudy_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="Smoking Pilot";

		setTitleBar(NOT_CONNECTED);

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
		InterviewScheduler.INCENTIVE_SCHEME=InterviewScheduler.NO_INCENTIVE_SCHEME;
		if (InterviewScheduler.INCENTIVE_SCHEME == InterviewScheduler.NO_INCENTIVE_SCHEME)
			incentives.setVisibility(View.INVISIBLE);
		// read config in!
		readConfig();
		//for self reporting event
	/*	try {
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (FieldStudyActivity)_onCreate()"+offStart+" "+offEnd+" "+inferenceService);
			//DatabaseLogger.getInstance(this);
//			inferenceService.logDeadPeriod(offStart,offEnd);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/		selfReportButton =(Button) findViewById(R.id.saliva_init_wake);
		selfReportButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				//System.out.println("he wants to report some event!");
				//choice c = new choice();
				//c.startActivity(intent);
			//	startActivity((Intent)c);
				selfReportIntent = new Intent(getBaseContext(),NIDA_SelfReportEventActivity.class);
				startActivity(selfReportIntent);
			}

		});

		updateIncentivesDisplay();

	}
	@Override
	protected void onDestroy() {
		stopService(emaScheduler);
		super.onDestroy();

	}
	long quietStart, quietEnd;
	long offStart, offEnd;
	long firstDayStart;

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
	/*
	void loadFirstDayInfo() {
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.FIELDINFO_CONFIG_FILENAME);
		if (!setupFile.exists())
			writeConfigToFile(System.currentTimeMillis());

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
        				firstDayStart = Long.parseLong(start.getNodeValue());
        				Constants.FIRSTDAYSTART=firstDayStart;
        			}
        		}
        	}
        }
	}
	*/
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

		stressorlabel.setText("Interview break from "+ makeTimeString(quietStart) + " until "+ makeTimeString(quietEnd) + "\n");
		endofdaylabel.setText("Data collection ends at "+makeTimeString(offStart)+", begins again at " + makeTimeString(offEnd) + "\n");

		startService(emaScheduler);

		// callback to disable stress inference when the time is right!
		Log.i("onActivityResult", "started the scheduler service");
		CharSequence text = "Configuration successfully loaded";
		//label.setText("Running the study");
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getApplicationContext(), text, duration);
//		toast.show();
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
/*	@Override
	protected void checkKeypadCodes() {
		if (keypadCode.length() == REPORT_CODE.length()) {
			if (keypadCode.equalsIgnoreCase(REPORT_CODE)) {
				keypad.hide();
				keypadVisible = false;
				startActivity(fieldReportIntent);
			}else {
				super.checkKeypadCodes();
			}
		}
	}
*/
}
