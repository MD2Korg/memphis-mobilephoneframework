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


import org.fieldstream.service.logger.Log;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class NIDA_CocaineStudyActivity extends BaseActivity {
	// EMA stuff

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
		incentives.setVisibility(View.INVISIBLE);

		selfReportButton =(Button) findViewById(R.id.saliva_init_wake);
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

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	/* This is called when the app is killed. */
	@Override
	public void onResume() {
		super.onResume();
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (FieldStudyActivity)_onResume().........."+inferenceService);
	}


	/* END ANDROID LIFE CYCLE */


}
