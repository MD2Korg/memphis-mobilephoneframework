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

import java.util.ArrayList;
import org.fieldstream.service.logger.Log;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


@SuppressLint("NewApi")
public class ChoiceActivity extends Activity{
	private Button confirmButton;
	private Button cancelButton;
	AlertDialog.Builder builder;	
	RelativeLayout layout;
	TextView questionView;
	ArrayAdapter<String> responseListAdapter;
	private ListView listResponseView = null;
	Intent data = new Intent();
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
	    super.onAttachedToWindow();
	    
	}	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		this.setContentView(R.layout.choice_layout);
        confirmButton = (Button) findViewById(R.id.ConfirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if(listResponseView.getCheckedItemPosition()==ListView.INVALID_POSITION){
            	    data.putExtra("key", -1);
            		Toast.makeText(getApplicationContext(), "Nothing is selected", Toast.LENGTH_SHORT).show();
            	}
            	else {
            	    data.putExtra("key",listResponseView.getCheckedItemPosition());
            		builder.show();
            	}
            }
        });
        cancelButton = (Button) findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	setResult(RESULT_OK, data);
            	finish();
            }
        });   
    	initResponseView();
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to report this event?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    setResult(RESULT_OK, data);
				dialog.cancel();
				finish();
			}

		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    data.putExtra("key", -1);
			    setResult(RESULT_OK,data);
				dialog.cancel();
			}
		});
        
	}
	public void initResponseView()
	{
    	ArrayList<String> list = new ArrayList<String>();
    	list.add("NO!!!"); 	list.add("NO"); list.add("no");
    	list.add("yes"); 	list.add("YES"); list.add("YES!!!");
    	
		responseListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.ema_response_row_layout, list);
        listResponseView = (ListView) findViewById(R.id.ResponseList);    
        listResponseView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listResponseView.setAdapter(responseListAdapter);
		listResponseView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			}
			});
	}
}
