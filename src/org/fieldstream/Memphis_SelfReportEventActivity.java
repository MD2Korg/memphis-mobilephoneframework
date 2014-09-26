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

// @author Mishfaq Ahmed


package org.fieldstream;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;


public class Memphis_SelfReportEventActivity extends Activity{

	public final static int SELF_YES = 1;
	
	private Button choiceDrink;
	private Button choiceSmoke;
	private Button backToMain;
	AlertDialog.Builder builder;
	AlertDialog alert = null;
	boolean selfreportdrink = false;
	boolean selfreportsmoke = false;
	private final int selfReportLebel=SELF_YES;
	
	 static String  alarm[]=new String [10];
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.memphis_selfreport);
		//final Activity reference = this;
		this.backToMain = (Button)findViewById(R.id.selfReportBackButton);
		this.choiceDrink = (Button)findViewById(R.id.drinkButton);
		this.choiceSmoke = (Button)findViewById(R.id.smokeButton);
		this.choiceDrink.setEnabled(false);
		this.choiceDrink.setVisibility(View.INVISIBLE);
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to report this event?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
			            if(selfreportdrink)
			            {
			            	Log.d("Mahbub","Inside the drinking event report");
			             	ContextBus.getInstance().pushNewContext(getDrinkingID(), getLabel(), System.currentTimeMillis(), System.currentTimeMillis());
			            }
			            else if(selfreportsmoke)
			            	ContextBus.getInstance().pushNewContext(getSmokingID(), getLabel(), System.currentTimeMillis(), System.currentTimeMillis());
			            finish();
		           }

		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
			            finish();
		           }
		       });
		
		alert = builder.create();
		
		backToMain.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				finish();
			}}
		);
		
		
		choiceDrink.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				selfreportdrink = true;
				alert.show();
				Log.d("Button Clicked for reporting new Drinking Event","Before context pushed in Bus");
				//ContextBus.getInstance().pushNewContext(getDrinkingID(), getLabel(), System.currentTimeMillis(), System.currentTimeMillis());
				Log.d("Button Clicked for reporting new Drinking Event","After context pushed in Bus");
				
			}
		});
		  
		choiceSmoke.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				selfreportsmoke = true;
				alert.show();
				Log.d("Button Clicked for reporting new Smoking Event","Before context pushed in Bus");
				//ContextBus.getInstance().pushNewContext(getSmokingID(), getLabel(), System.currentTimeMillis(), System.currentTimeMillis());
				Log.d("Button Clicked for reporting new Smoking Event","After context pushed in Bus");
			}
		});
	}
	public int getDrinkingID(){
		return Constants.MODEL_SELF_DRINKING;
	}
	public int getCravingID(){
		return Constants.MODEL_SELF_CRAVING;
	}
	
	public int getSmokingID(){
		return Constants.MODEL_SELF_SMOKING;
	}
	
	public int getLabel(){
		return selfReportLebel;
	}
}
