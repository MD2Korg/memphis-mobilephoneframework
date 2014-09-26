//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided 
//that the following conditions are met:
//
//* Redistributions of source code must retain the above copyright notice, this list of conditions and 
//  the following disclaimer.
//* Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
//  and the following disclaimer in the documentation and/or other materials provided with the 
//  distribution.
//* Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its 
//  contributors may be used to endorse or promote products derived from this software without specific 
//  prior written permission.
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

//remove EMA scheduler for data labeling

public class ContextLabeler extends BaseActivity {

	public final String CONFIG_DIR = "FieldStream/config";
	public final String CONFIG_FILENAME="labels.xml";
	
	private List<Button> listClickedButtons = new ArrayList<Button>();


	@SuppressWarnings("unused")
	private int result;

	private TextView text;

	DatabaseLogger db;
	private FileOutputStream fout;
	private PrintStream printStrm;

	private String label = "/sdcard/FieldStream/logs/ContextDataLabeling";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initialize();

		db = DatabaseLogger.getInstance(this);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.context_labeler_blank);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		titleText="FieldStudy (EXACT)";

		setTitleBar(NOT_CONNECTED);

		loadLabelConfig();
		text=(TextView)findViewById(R.id.TextView01);
	}

	public void loadLabelConfig() {

		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		File root = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(root+"/"+CONFIG_DIR);
			dir.mkdirs();

			File labelConfigFile = new File(dir, CONFIG_FILENAME);
			if (!labelConfigFile.exists()){
				Toast toast = Toast.makeText(context,"\""+CONFIG_FILENAME+"\" is not found", duration);
				toast.show();
				return;
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(labelConfigFile);

			Element xmlroot = dom.getDocumentElement();		
			NodeList nodeList;
			nodeList = xmlroot.getElementsByTagName("button");
			int numberOfItems=nodeList.getLength();
			for(int i=0;i<numberOfItems;i++)
			{
				Node node=nodeList.item(i);
				if(node.hasChildNodes())
				{
					addButtonToLayout(node.getFirstChild().getNodeValue());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void initialize()
	{
		long timeStamp = System.currentTimeMillis();
		label=label+timeStamp;
	}
	
	public String getToggleButtonText(String buttonText) {
		if(buttonText.toLowerCase().endsWith("start")) {
			int index = buttonText.toLowerCase().lastIndexOf("start");
			return (buttonText.substring(0, index) + "End");
		}
		if(buttonText.toLowerCase().endsWith("end")) {
			//text.toLowerCase().replaceAll("start", "end");
			//b.setText(replace(buttonText.toLowerCase(),"end","Start"));
			int index = buttonText.toLowerCase().lastIndexOf("end");
			return (buttonText.substring(0, index) + "Start");
		}
		return buttonText;
	}
	
	public void toggleButtonText(Button b) {
		String buttonText=(String) b.getText();
		if(buttonText.toLowerCase().endsWith("start")) {
			//text.toLowerCase().replaceAll("start", "end");
			//b.setText(replace(buttonText.toLowerCase(),"start","End"));
			//int index = buttonText.toLowerCase().lastIndexOf("start");
			//b.setText(buttonText.substring(0, index) + "End");
			b.setText(getToggleButtonText(buttonText));
			setButtonRed(b);
		}
		if(buttonText.toLowerCase().endsWith("end")) {
			//text.toLowerCase().replaceAll("start", "end");
			//b.setText(replace(buttonText.toLowerCase(),"end","Start"));
			//int index = buttonText.toLowerCase().lastIndexOf("end");
			//b.setText(buttonText.substring(0, index) + "Start");
			b.setText(getToggleButtonText(buttonText));
			setButtonGreen(b);
		}
	}
	
	public void vibratePhone() {
		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		// Vibrate for 500 milliseconds
		v.vibrate(500);
	}
	
	public void onClickButton(View v, Button b) {
		vibratePhone();
		String buttonText=(String) b.getText();
		if(buttonText.equalsIgnoreCase("RemoveLastLabel"))
		{
			try{
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which){
						case DialogInterface.BUTTON_POSITIVE:
							deleteLastLabel(label);
							int indexToRemove = listClickedButtons.size()-1;
							if(indexToRemove>=0) {
								Button lastClickedButton = (Button)listClickedButtons.get(indexToRemove);
								String sMessage = "Last label deleted : " + getToggleButtonText((String)lastClickedButton.getText());
								text.setText(sMessage);
								toggleButtonText(lastClickedButton);
								listClickedButtons.remove(indexToRemove);
								
								Toast toast = Toast.makeText(getApplicationContext(), sMessage, Toast.LENGTH_SHORT);
								toast.show();
							}
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							break;
						}
					}
				};
				if(listClickedButtons.size()>0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ContextLabeler.this);
					int indexToRemove = listClickedButtons.size()-1;
					Button lastClickedButton = (Button)listClickedButtons.get(indexToRemove);
					String lastButtonText = getToggleButtonText((String)lastClickedButton.getText());
					builder.setMessage("Do you want to delete the last label entry? : "+lastButtonText).setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
				} else {
					String sMessage = "No Last label to delete";
					text.setText(sMessage);
					Toast toast = Toast.makeText(getApplicationContext(), sMessage, Toast.LENGTH_SHORT);
					toast.show();
				}
			}catch(Exception exp)
			{
				exp.printStackTrace();
			}
		}
		else
		{
			writeLabeles(buttonText);
			toggleButtonText(b);
			listClickedButtons.add(b);
			Toast.makeText(getApplicationContext(), text.getText()+" button is pressed", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setButtonGreen(Button b) {
		//b.setBackgroundColor(Color.GREEN);
		Drawable d = b.getBackground();  
        PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.GREEN, Mode.SRC_ATOP);
        d.setColorFilter(filter);
	}
	
	public void setButtonRed(Button b) {
		//b.setBackgroundColor(Color.RED);
		Drawable d = b.getBackground();  
        PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, Mode.SRC_ATOP);
        d.setColorFilter(filter);
	}

	public void addButtonToLayout(String buttonName) {

		TableLayout tl = (TableLayout)findViewById(R.id.MarkingTableLayout);
		
		TableLayout.LayoutParams tableRowParams=new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.FILL_PARENT);

        int leftMargin=5;
        int topMargin=5;
        int rightMargin=5;
        int bottomMargin=5;

		tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);		
		
		int nChildren=tl.getChildCount();
		if(nChildren>0)
		{
			TableRow row=(TableRow) tl.getChildAt(nChildren-1); //take the last row
			int nButton=row.getChildCount();
			if(nButton==1)
			{
				//since this row has only one button, add a new button to it
				final Button b = new Button(this);
				b.setText(buttonName);
				//b.setBackgroundColor(Color.GREEN);
				//b.setId(1);
				b.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						//find the current text of the button
						onClickButton(v, b);
					}
				});
				if(buttonName.toLowerCase().endsWith("start")) {
					this.setButtonGreen(b);
				}
				row.addView(new TextView(this));
				row.addView(new TextView(this));
				row.addView(b);
				return;				
			}

		}

		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		/* Create a Button to be the row-content. */
		final Button b = new Button(this);
		b.setText(buttonName);
		//b.setId(1);
		//b.setBackgroundColor(Color.GREEN);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//find the current text of the button
				onClickButton(v, b);
			}
		});
		if(buttonName.toLowerCase().endsWith("start")) {
			setButtonGreen(b);
		}
		/* Add Button to row. */
		tr.addView(b);
		/* Add row to TableLayout. */
		//tl.addView(tr,new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		tl.addView(tr,tableRowParams);
		
//		TableRow tr1 = new TableRow(this);
//		//int children=tr.getChildCount();
//		tr1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
//		tr1.addView(new TextView(this));
//		tl.addView(tr1,new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	}

	public void writeLabeles(String label)
	{
		long timeStamp = System.currentTimeMillis();
		java.util.Date d = new java.util.Date(timeStamp);
		//write to the file
		//String str=timeStamp+","+d.toString()+","+label+"\n";
		String str=timeStamp+","+label+"\n";

		markTimestampWithLabel(str);

		try{
			db = DatabaseLogger.getInstance(this);
			db.logAnything("activity_lebel", label, System.currentTimeMillis());
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		text.setText(label+" at: "+d.toLocaleString());
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

		if(aFile.length()<=0)
			return;
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
	/**
	 * 
	 * @param str longer string
	 * @param pattern shorter string
	 * @param replace the content for replacement
	 * @return string after replacement
	 */
	public String replace(String str, String pattern, String replace) {
	    int s = 0;
	    int e = 0;
	    StringBuffer result = new StringBuffer();

	    while ((e = str.indexOf(pattern, s)) >= 0) {
	        result.append(str.substring(s, e));
	        result.append(replace);
	        s = e+pattern.length();
	    }
	    result.append(str.substring(s));
	    return result.toString();
	}


}
