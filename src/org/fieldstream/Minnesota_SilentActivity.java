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

import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.functions.ReadWriteConfigFiles;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;

@SuppressLint("NewApi")
public class Minnesota_SilentActivity extends Activity{
	AlertDialog.Builder builder_sure,builder;
	public int silentTime=-1;
	private Button backToMain;
	String message="";
	AlertDialog alert_sure =null;
	static final int PICK_SILENT_PERIOD = 1;
	Button buttonOk;
	RadioGroup radioSilentGroup;
	RadioButton radioselectedButton;

	DatabaseLogger db;
	//Button saliva_back;

	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.minnesota_silent_layout);
		radioSilentGroup = (RadioGroup) findViewById(R.id.radioSilentGroup);
		radioselectedButton=(RadioButton) findViewById(R.id.radio10);
		radioselectedButton.setChecked(true);

		buttonOk=(Button) findViewById(R.id.button_ok);
		buttonOk.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				int selectedId = radioSilentGroup.getCheckedRadioButtonId();
				radioselectedButton = (RadioButton) findViewById(selectedId);
				switch(selectedId){
				case R.id.radio5: silentTime=5;break;
				case R.id.radio10:silentTime=10;break;
				case R.id.radio15:silentTime=15;break;
				case R.id.radio30:silentTime=30;break;
				}
				if(ReadWriteConfigFiles.getInstance(this).isexist_quiettime()==true){
					silentTime=-1;
					builder.setMessage("Sorry, EMA can be turned off only once in a day.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							//do things
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}
				else{
					//				Toast.makeText(Minnesota_SilentActivity.this,""+silentTime, Toast.LENGTH_SHORT).show();
					//				issleep=WAKE;
					builder_sure.setMessage("Are you sure you want to turn off EMA for "+silentTime+ " minutes?");
					alert_sure=builder_sure.create();
					//				alert_confirm=builder.create();
					alert_sure.show();
				}
			}
		});
		builder = new AlertDialog.Builder(this);

		builder_sure = new AlertDialog.Builder(this);
		builder_sure.setMessage("Are you sure you want to turn off EMA for "+silentTime+ " minutes?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intentMessage=new Intent();
				intentMessage.putExtra("MESSAGE",String.valueOf(silentTime));
				db = DatabaseLogger.getInstance(this);
				db.logAnything4("deadperiod", System.currentTimeMillis(),"quietstart",String.valueOf(System.currentTimeMillis()));
				db.logAnything4("deadperiod", System.currentTimeMillis(),"quietend",String.valueOf(System.currentTimeMillis()+silentTime*60*1000));

				setResult(PICK_SILENT_PERIOD,intentMessage);
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
				intentMessage.putExtra("MESSAGE",String.valueOf(silentTime));
				setResult(PICK_SILENT_PERIOD,intentMessage);
				finish();
			}}
				);

		Intent intentMessage=new Intent();
		intentMessage.putExtra("MESSAGE",String.valueOf(silentTime));
		setResult(PICK_SILENT_PERIOD,intentMessage);

	}

}
