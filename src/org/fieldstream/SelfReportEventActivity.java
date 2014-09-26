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

import java.util.Vector;

import org.fieldstream.functions.ReadWriteConfigFiles;
import org.fieldstream.functions.ReadWriteConfigFiles.SELF;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.sensor.ContextBus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;


public class SelfReportEventActivity extends Activity{
	int selfreported_event=-1;
	int selfreported_event_value=-1;
	private Button backToMain;
	AlertDialog.Builder builder;
	AlertDialog alert_button = null;
	private Intent choiceIntent;
	Vector<SELF> self;
	@SuppressLint("NewApi")
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
	    super.onAttachedToWindow();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.selfreport_layout);
		ReadWriteConfigFiles wr=ReadWriteConfigFiles.getInstance(this);
		self=wr.readSelfreportConfig();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		LinearLayout ll = (LinearLayout)findViewById(R.id.selfReportButtonLayout);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);

		for (int i=0;i<self.size();i++){
			Button btn = new Button(this);
			btn.setText(self.get(i).text);
			btn.setId(self.get(i).modelID);
			btn.setTextSize(20);
			btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					selfreported_event = v.getId();
					int now;
					for (now=0;now<self.size();now++){
						if(self.get(now).modelID==selfreported_event)
							break;
					}
					if(self.get(now).type.compareTo("radiobutton")==0){
						choiceIntent = new Intent(getBaseContext(),ChoiceActivity.class);
						startActivityForResult(choiceIntent,1);
					}
					else if(self.get(now).type.compareTo("button")==0){
						alert_button.show();
					}
				}
			});
			ll.addView(btn,params);
		}
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to report this event?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String strReport="";
				for (int now=0;now<self.size();now++){
					if(self.get(now).modelID==selfreported_event){
						strReport=self.get(now).text;
						break;
					}
				}
				strReport=strReport+",1";
				DatabaseLogger.getInstance(this).logAnything("selfreport", strReport, System.currentTimeMillis());
				ContextBus.getInstance().pushNewContext(selfreported_event, 1, System.currentTimeMillis(), System.currentTimeMillis());
				dialog.cancel();
				finish();
			}

		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		alert_button = builder.create();

		this.backToMain = (Button)findViewById(R.id.selfReportBackButton);
		backToMain.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				finish();
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			if (data.hasExtra("key")) {
				int result=data.getExtras().getInt("key");
				String strReport="";
				for (int now=0;now<self.size();now++){
					if(self.get(now).modelID==selfreported_event){
						strReport=self.get(now).text;
						break;
					}
				}
				if(result!=-1){
					strReport=strReport+","+Integer.toString(result);
					DatabaseLogger.getInstance(this).logAnything("selfreport", strReport, System.currentTimeMillis());
					ContextBus.getInstance().pushNewContext(selfreported_event, result, System.currentTimeMillis(), System.currentTimeMillis());
				}
				finish();
			}
		}
	}
}
