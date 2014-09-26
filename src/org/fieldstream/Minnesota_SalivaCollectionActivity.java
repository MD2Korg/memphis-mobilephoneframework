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

import java.util.Calendar;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint({ "SimpleDateFormat", "NewApi" })
public class Minnesota_SalivaCollectionActivity extends Activity{
	AlertDialog.Builder builder_sure;
	AlertDialog.Builder builder_confirm;
	public int issleep=1;
	public final int SLEEP=1;
	public final int WAKE=2;
	public final int UNKNOWN=-1;
	private Button backToMain;
	String message="";
	AlertDialog alert_sure =null;
//	MyAlertDialog alert_confirm =null;

	Button saliva_init_wake;
	Button saliva_init_sleep;

	DatabaseLogger db;
	//Button saliva_back;
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
	    super.onAttachedToWindow();
	}

	boolean verify_time(int wakesleep)
	{
		Calendar c = Calendar.getInstance();
		if(wakesleep==WAKE){
			if(c.get(Calendar.HOUR_OF_DAY)>=4 && c.get(Calendar.HOUR_OF_DAY)<10)
				return true;
			else return false;
		}
		else if(wakesleep==SLEEP){
			if(c.get(Calendar.HOUR_OF_DAY)>=20)
				return true;
			else return false;
		}
		return false;
	}


	public void onCreate(Bundle savedInstanceState) {
		if(Log.DEBUG_MONOWAR) Log.m("Nusrat_saliva","OK\t: Saliva: new activity starts");
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);


		setContentView(R.layout.minnesota_saliva_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

		//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

		saliva_init_wake=(Button) findViewById(R.id.saliva_init_wake);
		saliva_init_sleep=(Button) findViewById(R.id.saliva_init_sleep);
		//	saliva_back=(Button) findViewById(R.id.saliva_back);

		saliva_init_wake.setOnClickListener(new View.OnClickListener() {

			//		Memphis_FieldStudyActivity resetAlarm= new Memphis_FieldStudyActivity();
			public void onClick(View v) {
				issleep=WAKE;
				alert_sure=builder_sure.create();
				//				alert_confirm=builder.create();
				alert_sure.show();
			}
		});
		saliva_init_sleep.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				issleep=SLEEP;
				alert_sure=(AlertDialog) builder_sure.create();
				alert_sure.show();
			}
		});
		builder_confirm = new AlertDialog.Builder(this);
		builder_confirm.setMessage("Saliva Collection Completed")
		.setCancelable(false)
		.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		builder_sure = new AlertDialog.Builder(this);
		builder_sure.setMessage("Are you sure you want to report this event?")

		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				long curtime=System.currentTimeMillis();
				if(verify_time(issleep)==false){
					Toast toast = Toast.makeText(getApplicationContext(), "Invalid wake up/sleep time\n(Wake up Time= 4:00am - 10:00am\nSleep Time=8:00pm - 12:00am)", Toast.LENGTH_LONG);
					toast.show();
					return;
				}
				Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY, 0);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND, 0);cal.set(Calendar.MILLISECOND, 0);
				long starttimestamp=cal.getTimeInMillis();
				long endtimestamp=starttimestamp+Constants.DAYMILLIS;
				if(issleep==WAKE){
					db = DatabaseLogger.getInstance(this);
					if(db.countwakeupsleep("Click: Wakeup Time", starttimestamp, endtimestamp)!=0){
						Toast toast = Toast.makeText(getApplicationContext(), "ERROR: This option cannot be selected more than once in a day.", Toast.LENGTH_LONG);
						toast.show();
						return;
					}
					db.logAnything("alarm_info", "Click: Wakeup Time", curtime+2000);
					db.logAnything4("deadperiod", System.currentTimeMillis(),"sleepend",String.valueOf(curtime+2000));
					message="sleepend";
				}
				else if(issleep==SLEEP){
					db = DatabaseLogger.getInstance(this);
					if(db.countwakeupsleep("Click: Sleep Time", starttimestamp, endtimestamp)!=0){
						Toast toast = Toast.makeText(getApplicationContext(), "ERROR: This option cannot be selected more than once in a day.", Toast.LENGTH_LONG);
						toast.show();
						return;
					}

					db.logAnything("alarm_info", "Click: Sleep Time", curtime+2000);
					db.logAnything4("deadperiod", System.currentTimeMillis(),"sleepstart",String.valueOf(curtime+2000));
					message="sleepstart";
				}
				Log.ema_alarm("", "message="+message);
				Intent intentMessage=new Intent();
				intentMessage.putExtra("MESSAGE",message);
		        setResult(RESULT_OK,intentMessage);
				finish();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		this.backToMain = (Button)findViewById(R.id.BackButton);
		backToMain.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Intent intentMessage=new Intent();

				intentMessage.putExtra("MESSAGE",message);
		        setResult(RESULT_OK,intentMessage);
				finish();
			}}
				);


	}

}
