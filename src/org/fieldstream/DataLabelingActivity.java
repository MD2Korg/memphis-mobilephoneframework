//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided 
//that the following conditions are met:
//
//  * Redistributions of source code must retain the above copyright notice, this list of conditions and 
//    the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
//    and the following disclaimer in the documentation and/or other materials provided with the 
//    distribution.
//  * Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its 
//    contributors may be used to endorse or promote products derived from this software without specific 
//    prior written permission.
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

//@author Mahbubur Rahman
//@author Patrick Blitz
//@author Andrew Raij


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//remove EMA scheduler for data labeling

public class DataLabelingActivity extends BaseActivity {
	// EMA stuff
	//private Intent emaScheduler;
	//	private final static String REPORT_CODE = "8812";


	@SuppressWarnings("unused")
	private int result;

	//Mahbub
	//private Button backButton;
	private Button speakingStart;
	private Button smokingStart;
	private Button speakingEnd;
	private Button smokingEnd;
	//private Button listenStart;
	private Button quietStart;
	private Button puffStart;
	private Button puffEnd;
	private Button cancel;

	private Button pacingStart;
	private Button pacingEnd;

	private TextView text;

	private Button slowWalking;
	private Button fastWalking;

	private Button sitting;
	private Button standing;

	private Button lying;
	private Button running;

	private Button eating;
	private Button drinking;

	private Button stairWalking;

	private Button drivingStart;
	private Button drivingEnd;

	DatabaseLogger db;
	private FileOutputStream fout;
	private PrintStream printStrm;

	private String label = "/sdcard/FieldStream/logs/ContextDataLabeling.txt";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = DatabaseLogger.getInstance(this);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.context_labeler);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="FieldStudy (EXACT)";

		setTitleBar(NOT_CONNECTED);

		smokingStart=(Button)findViewById(R.id.SmokingStart);
		smokingEnd=(Button)findViewById(R.id.SmokingEnd);

		puffStart=(Button)findViewById(R.id.PuffStart);
		//puffEnd=(Button)findViewById(R.id.PuffEnd);

		cancel=(Button)findViewById(R.id.Cancel);

		slowWalking=(Button)findViewById(R.id.SlowWalking);
		fastWalking=(Button)findViewById(R.id.FastWalking);

		sitting=(Button)findViewById(R.id.Sitting);
		standing=(Button)findViewById(R.id.Standing);

		lying=(Button)findViewById(R.id.Lying);
		running=(Button)findViewById(R.id.Running);

		eating=(Button)findViewById(R.id.Eating);
		drinking=(Button)findViewById(R.id.Drinking);

		stairWalking=(Button)findViewById(R.id.Stair);

		drivingStart=(Button)findViewById(R.id.DrivingStart);
		drivingEnd=(Button)findViewById(R.id.DrivingEnd);

		//pacingEnd=(Button)findViewById(R.id.PacingEnd);
		//pacingStart=(Button)findViewById(R.id.PacingStart);

		//quietStart=(Button)findViewById(R.id.QuietStart);
		//listenStart=(Button)findViewById(R.id.ListeningStart);
		text=(TextView)findViewById(R.id.TextView01);

		slowWalking.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("slowWalking");
				Toast.makeText(getApplicationContext(), "SlowWalking button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		fastWalking.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("fastWalking");
				Toast.makeText(getApplicationContext(), "fastWalking button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		sitting.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("sitting");
				Toast.makeText(getApplicationContext(), "sitting button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		standing.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("standing");
				Toast.makeText(getApplicationContext(), "standing button is pressed", Toast.LENGTH_SHORT).show();
			}
		});	

		lying.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("lying");
				Toast.makeText(getApplicationContext(), "lying button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		running.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("running");
				Toast.makeText(getApplicationContext(), "running button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		eating.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("eating");
				Toast.makeText(getApplicationContext(), "eating button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		drinking.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("drinking");
				Toast.makeText(getApplicationContext(), "drinking button is pressed", Toast.LENGTH_SHORT).show();
			}
		});
		stairWalking.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("stairWalking");
				Toast.makeText(getApplicationContext(), "stairWalking button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		drivingStart.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("drivingStart");
				Toast.makeText(getApplicationContext(), "drivingStart button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		drivingEnd.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("drivingEnd");
				Toast.makeText(getApplicationContext(), "drivingEnd button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		smokingStart.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("smokingStart");
				Toast.makeText(getApplicationContext(), "smokingStart button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		smokingEnd.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("smokingEnd");
				Toast.makeText(getApplicationContext(), "smokingEnd button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			//remove the last entry of the file since the user cancels the event
			//also remove the last entry from the database
			public void onClick(View v) {
				//finish();  //finish() function is used for going back to the previous page
				long timeStamp = System.currentTimeMillis();
				java.util.Date d = new java.util.Date(timeStamp);
				//write to the file
				//String str=timeStamp+" "+d.toString()+" "+": Last label cancelled"+"\n";
				try{
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which){
							case DialogInterface.BUTTON_POSITIVE:
								deleteLastLabel(label);
								Toast toast = Toast.makeText(getApplicationContext(), "Last label is deleted", Toast.LENGTH_SHORT);
								toast.show();
								text.setText("Last label cancelled");
								break;

							case DialogInterface.BUTTON_NEGATIVE:
								break;
							}
						}
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(DataLabelingActivity.this);
					builder.setMessage("Do you want to delete the last label entry?").setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
				}catch(Exception exp)
				{
					exp.printStackTrace();
				}

				//cancel.isEnabled(false);
			}
		});

		puffStart.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				writeLabeles("puffStart");
				Toast.makeText(getApplicationContext(), "puff button is pressed", Toast.LENGTH_SHORT).show();
			}
		});

	}
	
	public void writeLabeles(String label)
	{
		long timeStamp = System.currentTimeMillis();
		java.util.Date d = new java.util.Date(timeStamp);
		//write to the file
		String str=timeStamp+","+d.toString()+","+label+"\n";

		markTimestampWithLabel(str);

		try{
			db = DatabaseLogger.getInstance(this);
			db.logAnything("activity_lebel", label, System.currentTimeMillis());
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		//text.setText(label+" at: "+d.toLocaleString());
	}
	@Override
	protected void onDestroy() {
		//stopService(emaScheduler);
		super.onDestroy();

	}

	/* This is called when the app is killed. */
	@Override
	public void onResume() {
		super.onResume();
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (FieldStudyActivity)_onResume().........."+inferenceService);

		//updateIncentivesDisplay();		
	}

	public void markTimestampWithLabel(String str)
	{
		try
		{
			fout = new FileOutputStream(label,true);
			printStrm = new PrintStream(fout);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		long timeStamp = System.currentTimeMillis();
		java.util.Date d = new java.util.Date(timeStamp);
		//write to the file
		//String str="Urge of Speaking: "+d.toString()+","+timeStamp;
		try
		{
			printStrm.print(str);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try{
			printStrm.close();
			fout.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	//public void deleteLastLine(File aFile) {
	public void deleteLastLabel(String path) {
		File aFile=new File(path);
		//StringBuilder contents = new StringBuilder();
		ArrayList<String> content=new ArrayList<String>();

		//reading the file and saving it into an arraylist after removing the last line
		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				/*
				 * readLine is a bit quirky :
				 * it returns the content of a line MINUS the newline.
				 * it returns null only for the END of the stream.
				 * it returns an empty String if two newlines appear in a row.
				 */
				while (( line = input.readLine()) != null){
					content.add(line);
				}
				content.remove(content.size()-1);
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		//writing to the file again
		try
		{
			fout = new FileOutputStream(path,false);
			printStrm = new PrintStream(fout);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


		try
		{
			for(int i=0;i<content.size();i++)
			{
				String str=content.get(i).toString()+"\n";
				printStrm.print(str);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try{
			printStrm.close();
			fout.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
