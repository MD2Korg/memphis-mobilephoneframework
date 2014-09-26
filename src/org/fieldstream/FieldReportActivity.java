//Copyright (c) 2010, University of Memphis
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

// @author Syed Monowar Hossain


package org.fieldstream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.sensors.QualityBuffer.DataQualityColorEnum;
import org.fieldstream.service.sensors.mote.sensors.SensorDataQualitySingleton.SensorTypeQualityEnum;

import org.fieldstream.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
@SuppressLint("NewApi")
public class FieldReportActivity extends Activity{

	private final String TAG = "FieldReportActivity";
	private Button backToMain;
	private final int MAXDAY=5;
	DatabaseLogger db;
	private static Map<Long, ReportValueObject> dayReportMap = new HashMap<Long, ReportValueObject>(); // Preserve this map when report is closed. So static.
	private static long lastModifiedReportMap = (long) -1;
	
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);  
	    super.onAttachedToWindow();
	}	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db=DatabaseLogger.getInstance(this);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  

		this.setContentView(R.layout.fieldreport);

		//final Activity reference = this;
		this.backToMain = (Button)findViewById(R.id.fieldreport_BackButton);


		backToMain.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				finish();
			}}
				);

		DateFormat timeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		//fieldStartTimeTextView.setText(timeFormatter.format(fieldstarttimeCal.getTime()));

		findReport();

	}
	String getFormattedDate(Calendar cal) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(cal.getTime());
	}

	void findReport()
	{
		try {
			DatabaseLogger db=DatabaseLogger.getInstance(this);
	
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy(EEE)");
	
			String date;
			long todaysStartTime = 0;
			for(int day=0; day<MAXDAY; day++){
				//create starttime
				Calendar calStart = Calendar.getInstance();
				calStart.set(Calendar.HOUR_OF_DAY, 0);
				calStart.set(Calendar.MINUTE, 0);
				calStart.set(Calendar.SECOND, 0);
				calStart.set(Calendar.MILLISECOND, 0);
				calStart.set(Calendar.DAY_OF_YEAR, calStart.get(Calendar.DAY_OF_YEAR)-day);
				Long startTime = calStart.getTimeInMillis();
				if(day==0) {
					todaysStartTime = startTime.longValue();
				}
	
				//generate endtime
				Calendar calEnd = Calendar.getInstance();
				calEnd.set(Calendar.HOUR_OF_DAY, 23);
				calEnd.set(Calendar.MINUTE, 59);
				calEnd.set(Calendar.SECOND, 59);
				calEnd.set(Calendar.MILLISECOND, 0);
				calEnd.set(Calendar.DAY_OF_YEAR, calEnd.get(Calendar.DAY_OF_YEAR)-day);
				Long endTime = calEnd.getTimeInMillis();
				// read # of saliva confirmation
				
				date=dateFormat.format(startTime);
				
				ReportValueObject reportValueObject = dayReportMap.get(startTime);
				if(reportValueObject==null || day==0 || lastModifiedReportMap!=todaysStartTime) {
					reportValueObject = new ReportValueObject();
					//reportValueObject.wearingSecond = db.getQualityDuration(SensorTypeQualityEnum.RIP, startTime, endTime, DataQualityColorEnum.GREEN)/1000;
					reportValueObject.wearingRip = db.getQualityDuration(SensorTypeQualityEnum.RIP, startTime, endTime, DataQualityColorEnum.GREEN)/1000;
					reportValueObject.wearingEcg = db.getQualityDuration(SensorTypeQualityEnum.ECG, startTime, endTime, DataQualityColorEnum.GREEN)/1000;
					reportValueObject.wearingWristLeft = db.getQualityDuration(SensorTypeQualityEnum.WristLeft, startTime, endTime, DataQualityColorEnum.GREEN)/1000;
					reportValueObject.wearingWristRight = db.getQualityDuration(SensorTypeQualityEnum.WristRight, startTime, endTime, DataQualityColorEnum.GREEN)/1000;
					reportValueObject.totalSecond = db.getTotalDuration(SensorTypeQualityEnum.RIP, startTime, endTime)/1000;
					//int emaPrompt = db.getNumEMAsAll(startTime, endTime);
					//int emaAnswered = db.getNumEMAsWithin7Mins(startTime, endTime);
					db.getEmaStatistics(startTime, endTime, 7*60*1000, reportValueObject);
					reportValueObject.salivaAckCount = db.getSalivaConfirmation(startTime, endTime);
					dayReportMap.put(startTime, reportValueObject);
				} else {
					if(Log.DEBUG_HILLOL) {
						Log.h(TAG, "Report Retrieved from HashMap. Date: "+startTime + " : " + getFormattedDate(calStart));
					}
				}
				//Log.ema_alarm("", "time="+Constants.millisecondToDateTime(startTime)+" Saliva No="+salivaAck);
				
				addRow(date, reportValueObject);
			}
			lastModifiedReportMap = todaysStartTime; // If this changes than invalidate the map.
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	private String getDurationString(long durationSec) {
		//return String.format("%d:%02d:%02d", durationSec/3600, (durationSec%3600)/60, (durationSec%60));
		return String.format("%.2fh", ((durationSec*1.0)/3600));
	}
	
	void addRow(String date, ReportValueObject reportValueObject)
	{
		//String qualityDurationStr = this.getDurationString(reportValueObject.wearingSecond) + 
		//			"\n(" + this.getDurationString(reportValueObject.totalSecond) +")";
		String qualityDurationStr = this.getDurationString(reportValueObject.wearingRip) 
				+ ", " + this.getDurationString(reportValueObject.wearingEcg)
				+ "\n" + this.getDurationString(reportValueObject.wearingWristLeft)
				+ ", " + this.getDurationString(reportValueObject.wearingWristRight)
				+ "\n(" + this.getDurationString(reportValueObject.totalSecond) +")";
		
		TableLayout tl = (TableLayout)findViewById(R.id.tablelayout_fieldreport);
		/* Create a new row to be added. */
		TableRow tr = new TableRow(this);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		int leftMargin=0;
        int topMargin=10;
        int rightMargin=0;
        int bottomMargin=0;
		lp.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
		
		tr.setLayoutParams(lp);

		TextView tv1 = new TextView(this);
		tv1.setLayoutParams(lp);
		tv1.setText(date);
		tv1.setGravity(Gravity.CENTER);

		TextView tv2 = new TextView(this);
		tv2.setLayoutParams(lp);
		tv2.setText(qualityDurationStr);
		tv2.setGravity(Gravity.CENTER);

		TextView tv3 = new TextView(this);
		tv3.setLayoutParams(lp);
		String emaText = String.valueOf(reportValueObject.emaPromptCount)+"/"+
						String.valueOf(reportValueObject.emaAnsweredCount)+"/"+
						String.valueOf(reportValueObject.emaAnswered7MinCount);
		tv3.setText(emaText);
		//tv3.setText(String.valueOf(emaAnswered)+"("+String.valueOf(emaPrompt)+")");
		tv3.setGravity(Gravity.CENTER);
		
		TextView tv4 = new TextView(this);
		tv4.setLayoutParams(lp);
		tv4.setText(String.valueOf(reportValueObject.salivaAckCount));
		tv4.setGravity(Gravity.CENTER);

		tr.addView(tv1);
		tr.addView(tv2);
		tr.addView(tv3);         
		tr.addView(tv4);
		
		tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	}
	
	public class ReportValueObject {
		//public long wearingSecond;
		public long wearingRip;
		public long wearingEcg;
		public long wearingWristLeft;
		public long wearingWristRight;
		public long totalSecond;
		public int emaPromptCount;
		public int emaAnsweredCount;
		public int emaAnswered7MinCount;
		public int salivaAckCount;
	}
}
