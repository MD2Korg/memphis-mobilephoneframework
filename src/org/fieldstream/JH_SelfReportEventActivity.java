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

import org.fieldstream.service.logger.DatabaseLogger;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class JH_SelfReportEventActivity extends BaseActivity{

	public final static int SELF_YES = 1;

	private final static String LAB_CODE = "3841";
	private boolean[] selected=new boolean[5];
	private Button choiceCraving;
	private Button choiceSmoking;
	private Button choiceMovie;
	private Button choiceGame;

	AlertDialog.Builder builder;
	AlertDialog alert = null;
	AlertDialog alert_option=null;
	AlertDialog.Builder builder_option;
	DatabaseLogger db;


	final CharSequence[] items = {"Alcohol Use", "Violent", "Sexual","Drug Use","Other"};

	boolean selfreportcraving = false;
	boolean selfreportsmoking = false;
	boolean selfreportmovie = false;
	boolean selfreportgame = false;

	private final int selfReportLebel=SELF_YES;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = DatabaseLogger.getInstance(this);
		db.logAnything("labstudy_log", "Self Report Activity Starts", System.currentTimeMillis());
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(R.layout.jh_selfreport);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="Self Report (JH)";
		setTitleBar(NOT_CONNECTED);

		this.choiceCraving = (Button)findViewById(R.id.button_craving);
		this.choiceSmoking = (Button)findViewById(R.id.button_smoking);
		this.choiceMovie = (Button)findViewById(R.id.button_movie);
		this.choiceGame = (Button)findViewById(R.id.button_game);


		builder = new AlertDialog.Builder(this);

		builder.setMessage("Are you sure you want to report this event?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   String event="";
		        	   db = DatabaseLogger.getInstance(this);
			            if(selfreportcraving)
			            	event="craving";
			            else if(selfreportsmoking)
			            	event="smoking";
			            else if(selfreportmovie){
			            	event="movie";
			            	for(int i=0;i<5;i++)
			            		if(selected[i]==true){
			            			event+=","+items[i];
			            		}
			            }
			            else if(selfreportgame){
			            	event="game";
			            	for(int i=0;i<5;i++)
			            		if(selected[i]==true){
			            			event+=","+items[i];
			            		}
			            }
			            db.logAnything("selfreport", event, System.currentTimeMillis());
						Toast.makeText(getApplicationContext(), "Event saved", Toast.LENGTH_SHORT).show();

		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });

		alert = builder.create();

		builder_option = new AlertDialog.Builder(this);
		builder_option.setTitle("Select Content type(s)");
		builder_option.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				alert.show();

			}
		});

		builder_option.setMultiChoiceItems(items,null, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked==true){
					Toast.makeText(getApplicationContext(), items[which].toString()+" is selected", Toast.LENGTH_SHORT).show();
					selected[which]=true;

				}
				else{
					Toast.makeText(getApplicationContext(), items[which].toString()+" is removed", Toast.LENGTH_SHORT).show();
					selected[which]=false;

				}
			}
		});

//		alert_option = builder_option.create();

		choiceCraving.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				selfreportmovie=selfreportgame=selfreportsmoking=selfreportcraving=false;
				selfreportcraving = true;
				alert.show();
			}
		});

		choiceSmoking.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				selfreportmovie=selfreportgame=selfreportsmoking=selfreportcraving=false;

				selfreportsmoking = true;
				alert.show();
			}
		});
		choiceMovie.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				selfreportmovie=selfreportgame=selfreportsmoking=selfreportcraving=false;
				selfreportmovie = true;
				if(alert_option!=null) alert_option.dismiss();
				for(int i=0;i<5;i++) selected[i]=false;

				alert_option=builder_option.create();
				alert_option.show();
			}
		});
		choiceGame.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				selfreportmovie=selfreportgame=selfreportsmoking=selfreportcraving=false;

				selfreportgame = true;
				if(alert_option!=null) alert_option.dismiss();
				for(int i=0;i<5;i++) selected[i]=false;

				alert_option=builder_option.create();
				alert_option.show();
			}
		});

	}

	public int getDrinkingID(){
		return Constants.MODEL_SELF_DRINKING;
	}

	public int getSmokingID(){
		return Constants.MODEL_SELF_SMOKING;
	}

	public int getLabel(){
		return selfReportLebel;
	}

	@Override
	protected void checkKeypadCodes() {
		if (keypadCode.length() == LAB_CODE.length()) {
			if (keypadCode.equalsIgnoreCase(LAB_CODE)) {
				keypad.hide();
				keypadVisible = false;
				keypad.dismiss();
				keypad = null;
				db = DatabaseLogger.getInstance(this);
				db.logAnything("labstudy_log", "Self Report Activity Ends", System.currentTimeMillis());

				finish();
			}else if(!keypadCode.equalsIgnoreCase(EXIT_CODE)){
				super.checkKeypadCodes();
			}else{
				keypad.hide();
				keypadVisible = false;
			}
		}
	}

}
