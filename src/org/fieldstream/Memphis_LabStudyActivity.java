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
package org.fieldstream;

//@author Syed Monowar Hossain

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import edu.cmu.ices.stress.phone.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Memphis_LabStudyActivity extends BaseActivity {

	SeekBar hSeekBar, sSeekBar  ;
	int i;
	TextView hProgressText, sProgressText;
    TextView hTrackingText, sTrackingText;
	TextView texth, texts;
//	TextView  texti;
	TextView text1,text2,text3,text4,text5,text6;
	TextView text7,text8,text9,text10,text11,text12;
	TextView l1,l2,l3,l4,l5,l6,l7,l8;
	RelativeLayout mScreen;
	boolean first = true;

	// exit dialog stuff
	DatabaseLogger db;
    
    
	// state variables
	private boolean starts;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		start=0;
		super.onCreate(savedInstanceState);
		// why imp??
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (LabStudyActivity)_onCreate()");
//		DatabaseLogger.makeActive();


		db = DatabaseLogger.getInstance(this);	
    	db.logAnything("labstudy_log", "Program Starts", System.currentTimeMillis());
		
//		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.memphis_labstuy_layout);   			
		
//		setContentView(R.layout.labstudy_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="Memphis LabStudy";
		setTitleBar(NOT_CONNECTED);

		starts = false;
        mScreen = (RelativeLayout) findViewById(R.id.myScreen);
//      mScreen.setBackgroundColor(Color.GRAY);
      hSeekBar = (SeekBar)findViewById(R.id.HappySlider);
      hProgressText = (TextView)findViewById(R.id.progress1);
      hTrackingText = (TextView)findViewById(R.id.tracking1);
      
      sSeekBar = (SeekBar)findViewById(R.id.SadSlider);
      sProgressText = (TextView)findViewById(R.id.progress2);
      sTrackingText = (TextView)findViewById(R.id.tracking2);
     
      
      hSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
     sSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
         
//       texti=(TextView) null;
      texth=(TextView)findViewById(R.id.Happy);
//      texth.setTextColor(Color.BLUE);
      text1=(TextView)findViewById(R.id.YES1);
//      text1.setTextColor(Color.BLUE);
      text2=(TextView)findViewById(R.id.YES2);
//      text2.setTextColor(Color.BLUE);
      text3=(TextView)findViewById(R.id.yes1);
//      text3.setTextColor(Color.BLUE);
      text4=(TextView)findViewById(R.id.no1);
//      text4.setTextColor(Color.BLUE);
      text5=(TextView)findViewById(R.id.NO1);
//      text5.setTextColor(Color.BLUE);
      text6=(TextView)findViewById(R.id.NO2);
//      text6.setTextColor(Color.BLUE);
      
      texts=(TextView)findViewById(R.id.Sad);
//      texts.setTextColor(Color.BLUE);
      text7=(TextView)findViewById(R.id.YES3);
//      text7.setTextColor(Color.BLUE);
     
          text8=(TextView)findViewById(R.id.YES4);
//          text8.setTextColor(Color.BLUE);

          text9=(TextView)findViewById(R.id.yes2);
//          text9.setTextColor(Color.BLUE);
          text10=(TextView)findViewById(R.id.no2);
//          text10.setTextColor(Color.BLUE);
          text11=(TextView)findViewById(R.id.NO3);
//          text11.setTextColor(Color.BLUE);
          text12=(TextView)findViewById(R.id.NO4);
//          text12.setTextColor(Color.BLUE);
      l1=(TextView)findViewById(R.id.line1);
      l1.setTextColor(Color.BLACK);
      l2=(TextView)findViewById(R.id.line2);
      l2.setTextColor(Color.BLACK);
      l3=(TextView)findViewById(R.id.line3);
      l3.setTextColor(Color.BLACK);
      l4=(TextView)findViewById(R.id.line4);
      l4.setTextColor(Color.BLACK);
      l5=(TextView)findViewById(R.id.line5);
      l5.setTextColor(Color.BLACK);
      l6=(TextView)findViewById(R.id.line6);
      l6.setTextColor(Color.BLACK);
      l7=(TextView)findViewById(R.id.line7);
      l7.setTextColor(Color.BLACK);
      l8=(TextView)findViewById(R.id.line8);
      l8.setTextColor(Color.BLACK);

		showDialog(0);
      
	}
	 @Override
	    public void onConfigurationChanged(Configuration newConfig) 
	    {
	        super.onConfigurationChanged(newConfig);
	    }	
	/* This is called when the app is killed. */
	@Override
	public void onResume() {
		super.onResume();
	}
	
    protected Dialog onCreateDialog(int id) {
        final CharSequence[] items = {"Practice", "Neutral", "Happy", "Sad", "Smoking"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Session");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
            	if (item == 0) {
                	db.logAnything("session", "start practice", System.currentTimeMillis());
                	db.logAnything("labstudy_log", "start practice", System.currentTimeMillis());
                	
            	}
            	else if (item == 1) {
                	db.logAnything("session", "start neutral video", System.currentTimeMillis());            		
                	db.logAnything("labstudy_log", "start neutral video", System.currentTimeMillis());

            	}
            	else if (item == 2) {
                	db.logAnything("session", "start amusement video", System.currentTimeMillis());            		
                	db.logAnything("labstudy_log", "start amusement video", System.currentTimeMillis());

            	}
            	else if (item == 3){
                	db.logAnything("session", "start sadness video", System.currentTimeMillis());            		
                	db.logAnything("labstudy_log", "start sadness video", System.currentTimeMillis());

            	}
            	else if (item == 4){
                	db.logAnything("session", "start smoking", System.currentTimeMillis());            		
                	db.logAnything("labstudy_log", "start smoking", System.currentTimeMillis());

            	}            	
        		handler.postDelayed(triggerRequest, 10000);		
            }
        });
        AlertDialog alert = builder.create();        
        
        return alert;
    }    

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		db.logAnything("labstudy_log", "Program Ends", System.currentTimeMillis());
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (LabStudyActivity)_onDestroy()");
        handler.removeCallbacks(triggerRequest);

		super.onDestroy();
	}
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener
    = new SeekBar.OnSeekBarChangeListener()
    {

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
//    	if (fromTouch) {
//	        handler.removeCallbacks(triggerRequest);
//	        handler.postDelayed(triggerRequest, TIME_UNTIL_REQUEST_LONG_MS);    	
//	    	
//	    	calculatevalue(seekBar);
//	    	
////	    	hProgressText.setText(progessvalue1 + " " + 
////	                getString(R.string.seekbar_from_touch) + "=" + "True");
////	    	hProgressText.setTextColor(Color.BLUE);
////	    	 sProgressText.setText(progessvalue2 + " " + 
////	                 getString(R.string.seekbar_from_touch) + "=" + "True") ;
////	    	 sProgressText.setTextColor(Color.BLUE);
//    	}      
    }
       
    public void onStartTrackingTouch(SeekBar seekBar) {
//        hTrackingText.setText(getString(R.string.seekbar_tracking_on));
//        sTrackingText.setText(getString(R.string.seekbar_tracking_on));
        
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
//        hTrackingText.setText(getString(R.string.seekbar_tracking_off));
//        sTrackingText.setText(getString(R.string.seekbar_tracking_off));       
        
		first = false;
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: onStopTracking");
        handler.removeCallbacks(triggerRequest);
        handler.postDelayed(triggerRequest, TIME_UNTIL_REQUEST_LONG_MS);    	    	
    	calculatevalue(seekBar);
        
    }
    
    
};


private static long TIME_UNTIL_REQUEST_LONG_MS = 2 * 60 * 1000;
private static long TIME_UNTIL_REQUEST_SHORT_MS = 30 * 1000;

private Handler handler = new Handler();
private Runnable triggerRequest = new Runnable() {
	   public void run() {	     
       		db.logAnything("buzz", "buzz", System.currentTimeMillis());            		
       	
			// vibrate and beep to alert the user to a new interview
			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100);

			vibrator.vibrate(1000);
			tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP);

			String text = "";
			if (first) {
				text = "Please rate your emotions now.";		
				first = false;
			}
			else {
				text = "Have your emotions changed?";
			}
			
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
			toast.show();
        	db.logAnything("labstudy_log", "buzz", System.currentTimeMillis());
			
		    handler.postDelayed(triggerRequest, TIME_UNTIL_REQUEST_LONG_MS);			
	   }
	};

    private void calculatevalue(SeekBar seekBar)
    {    
    	Log.d("Memphis LabStudy","calculatevalue()");
    	int progessvalue=seekBar.getProgress();
    	
    	
    	 if(progessvalue<=12) 
		     	  progessvalue=0;
    	     	 
         if(progessvalue>12 & progessvalue<=30) 
          progessvalue=20; 	
        
         
         if(progessvalue>30 & progessvalue<=50)  
          progessvalue=40;
         
         
         if(progessvalue>50 & progessvalue<=70) 
          progessvalue=60;
         
         
         if(progessvalue>70 & progessvalue<=90) 
         	progessvalue=80;
        
         if(progessvalue>90) 
          progessvalue=100;
                 
         seekBar.setProgress(progessvalue);
         

	    // scale from 0 = NO!!! to 5 = YES!!!
        if (seekBar == this.hSeekBar){
        	db.logAnything("lab_self_report_happy", Integer.toString(progessvalue/20), System.currentTimeMillis());             
        	db.logAnything("labstudy_log", "lab_self_report_happy,"+Integer.toString(progessvalue/20), System.currentTimeMillis());
        
        }else{ 
        	db.logAnything("lab_self_report_sad", Integer.toString(progessvalue/20), System.currentTimeMillis());
        	db.logAnything("labstudy_log", "lab_self_report_sad,"+Integer.toString(progessvalue/20), System.currentTimeMillis());
        	
        }
    }
}
