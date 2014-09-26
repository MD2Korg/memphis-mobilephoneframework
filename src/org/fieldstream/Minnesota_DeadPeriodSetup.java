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


// @author Patrick Blitz
// @author Andrew Raij


package org.fieldstream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.fieldstream.functions.ReadWriteConfigFiles;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

public class Minnesota_DeadPeriodSetup extends Activity {
	private Button  sleepingStartDateButton, sleepingStartTimeButton, sleepingEndDateButton, sleepingEndTimeButton;
	private Button dayStartTimeButton, dayStartDateButton;
	private Button saveButton, backButton;

	private Calendar sleepingStartTime, sleepingEndTime,dayStartTime;

	boolean settingsChanged = false;

	// DIALOG IDs
	static final int SLEEPING_START_TIME_DIALOG_ID = 2;
	static final int SLEEPING_END_TIME_DIALOG_ID = 3;
	static final int SLEEPING_START_DATE_DIALOG_ID = 6;
	static final int SLEEPING_END_DATE_DIALOG_ID = 7;
	static final int DAYSTART_DATE_DIALOG_ID = 8;
	static final int DAYSTART_TIME_DIALOG_ID = 9;

	static final long HOUR_MILLIS = 60L * 60L * 1000L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.minnesota_dead_period_setup_layout);
		loadDeadPeriods();

		// capture our View elements
		sleepingStartDateButton = (Button) findViewById(R.id.SleepingStartDateButton);	    
		sleepingStartTimeButton = (Button) findViewById(R.id.SleepingStartTimeButton);
		sleepingEndDateButton = (Button) findViewById(R.id.SleepingEndDateButton);	    
		sleepingEndTimeButton = (Button) findViewById(R.id.SleepingEndTimeButton);
		dayStartDateButton = (Button) findViewById(R.id.DayStartDateButton);	    
		dayStartTimeButton = (Button) findViewById(R.id.DayStartTimeButton);

		saveButton = (Button) findViewById(R.id.SaveDeadPeriod);	    
		backButton = (Button) findViewById(R.id.BackDeadPeriod);
		
		// add a click listener to the button
		sleepingStartTimeButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(SLEEPING_START_TIME_DIALOG_ID);
			}
		});
		sleepingEndTimeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(SLEEPING_END_TIME_DIALOG_ID);
			}
		});
		// add a click listener to the button
		sleepingStartDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(SLEEPING_START_DATE_DIALOG_ID);
			}
		});
		sleepingEndDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(SLEEPING_END_DATE_DIALOG_ID);
			}
		});
		dayStartDateButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DAYSTART_DATE_DIALOG_ID);
			}
		});
		dayStartTimeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DAYSTART_TIME_DIALOG_ID);
			}
		});
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//				showDialog(DAYSTART_TIME_DIALOG_ID);
				finish();
			}
		});
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//				showDialog(DAYSTART_TIME_DIALOG_ID);
				saveDeadPeriod();
				finish();
			}
		});

	}

	void loadDeadPeriods() {
		Calendar now = Calendar.getInstance();
		sleepingStartTime = (Calendar) now.clone();
		sleepingEndTime = (Calendar) now.clone();
		dayStartTime = (Calendar) now.clone();

		ReadWriteConfigFiles.getInstance(this).loadDeadPeriodsDB();
		sleepingStartTime.setTimeInMillis(Constants.SLEEPSTART);
		sleepingEndTime.setTimeInMillis(Constants.SLEEPEND);
		dayStartTime.setTimeInMillis(Constants.DAYSTART);	    
	}

	@Override
	public void onResume() {
		super.onResume();
		loadDeadPeriods();
		// display the current date
		updateDisplay();    	
	}
	public void saveDeadPeriod()
	{
		boolean success=false;
		long sleepingStartTimeUnix = sleepingStartTime.getTimeInMillis();
		long sleepingEndTimeUnix = sleepingEndTime.getTimeInMillis();
		long dayStartTimeUnix=dayStartTime.getTimeInMillis();
		success=ReadWriteConfigFiles.getInstance(this).writeDeadPeriodsDB(-1, -1, sleepingStartTimeUnix, sleepingEndTimeUnix,dayStartTimeUnix);
		
		if(success==true)
			Toast.makeText(getApplicationContext(), "Information Saved Successfully", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getApplicationContext(), "Error Saving Network Setup", Toast.LENGTH_SHORT).show();
		settingsChanged = false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case SLEEPING_START_TIME_DIALOG_ID:
			return new TimePickerDialog(this,
					onSleepingStartTimeSetListener, sleepingStartTime.get(Calendar.HOUR_OF_DAY), sleepingStartTime.get(Calendar.MINUTE), false);

		case SLEEPING_END_TIME_DIALOG_ID:
			return new TimePickerDialog(this,
					onSleepingEndTimeSetListener, sleepingEndTime.get(Calendar.HOUR_OF_DAY), sleepingEndTime.get(Calendar.MINUTE), false);

		case DAYSTART_TIME_DIALOG_ID:
			return new TimePickerDialog(this,
					onDayStartTimeSetListener, dayStartTime.get(Calendar.HOUR_OF_DAY), dayStartTime.get(Calendar.MINUTE), false);

		case SLEEPING_START_DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					onSleepingStartDateSetListener, sleepingStartTime.get(Calendar.YEAR), sleepingStartTime.get(Calendar.MONTH), sleepingStartTime.get(Calendar.DAY_OF_MONTH));

		case SLEEPING_END_DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					onSleepingEndDateSetListener, sleepingEndTime.get(Calendar.YEAR), sleepingEndTime.get(Calendar.MONTH), sleepingEndTime.get(Calendar.DAY_OF_MONTH));

		case DAYSTART_DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					onDayStartDateSetListener, dayStartTime.get(Calendar.YEAR), dayStartTime.get(Calendar.MONTH), dayStartTime.get(Calendar.DAY_OF_MONTH));

		}		

		return null;
	}

	// updates the time we display in the TextView
	private void updateDisplay() {	
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);

		sleepingStartTimeButton.setText(timeFormatter.format(sleepingStartTime.getTime()));
		sleepingEndTimeButton.setText(timeFormatter.format(sleepingEndTime.getTime()));

		sleepingStartDateButton.setText(getDateText(sleepingStartTime));
		sleepingEndDateButton.setText(getDateText(sleepingEndTime));

		dayStartDateButton.setText(getDateText(dayStartTime));
		dayStartTimeButton.setText(timeFormatter.format(dayStartTime.getTime()));

	}

	private String getDateText(Calendar cal) {
		Calendar today = Calendar.getInstance();

		int years = cal.get(Calendar.YEAR) - today.get(Calendar.YEAR);
		int days = cal.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR);
		if (years == 0) {
			if (days == 0)
				return "Today";
			else if (days == 1)
				return "Tomorrow";
			else if(days==-1) return "Yesterday";
			//			else if (days > 1 && days < 7) 
			//				return new SimpleDateFormat("EEEE").format(cal.getTime());
			else 
				//				return new SimpleDateFormat("EEEE").format(cal.getTime());
				return DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
		}
		else if (years == 1) {
			days = days + today.getActualMaximum(Calendar.DAY_OF_YEAR);
			if (days == 0) 
				return "Tomorrow";
			else if (days > 0 && days < 6) 
				return new SimpleDateFormat("EEEE").format(cal.getTime());
			else {
				return DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
			}
		}
		else {
			return DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime());
		}
	}

	private TimePickerDialog.OnTimeSetListener onSleepingStartTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			sleepingStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			sleepingStartTime.set(Calendar.MINUTE, minute);
			sleepingStartTime.set(Calendar.SECOND, 0);

			settingsChanged = true;
			updateDisplay();
		}
	};
	private TimePickerDialog.OnTimeSetListener onDayStartTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			dayStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			dayStartTime.set(Calendar.MINUTE, minute);
			dayStartTime.set(Calendar.SECOND,0);
			settingsChanged = true;
			updateDisplay();
		}
	};

	private TimePickerDialog.OnTimeSetListener onSleepingEndTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			sleepingEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			sleepingEndTime.set(Calendar.MINUTE, minute);
			sleepingEndTime.set(Calendar.SECOND, 0);
			settingsChanged = true;
			updateDisplay();
		}
	};

	private DatePickerDialog.OnDateSetListener onSleepingStartDateSetListener = 
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			sleepingStartTime.set(Calendar.YEAR, year);
			sleepingStartTime.set(Calendar.MONTH, monthOfYear);
			sleepingStartTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			settingsChanged = true;
			updateDisplay();
		}
	};
	private DatePickerDialog.OnDateSetListener onDayStartDateSetListener = 
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			dayStartTime.set(Calendar.YEAR, year);
			dayStartTime.set(Calendar.MONTH, monthOfYear);
			dayStartTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			settingsChanged = true;
			updateDisplay();
		}
	};

	private DatePickerDialog.OnDateSetListener onSleepingEndDateSetListener = 
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			sleepingEndTime.set(Calendar.YEAR, year);
			sleepingEndTime.set(Calendar.MONTH, monthOfYear);
			sleepingEndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			settingsChanged = true;
			updateDisplay();
		}
	};
}
