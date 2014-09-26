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

// @author Somnath Mitra
// @author Andrew Raij


package org.fieldstream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class NetworkSetup extends Activity {

	protected static final String TAG = "StressInferenceSetup";

	// inference service access
	public IInferrenceService service;

	/* END USER INTERFACE DECLARATIONS */


	private EditText edittextRIPECG = null;
	private EditText edittextNineAxisRight = null;
	private EditText edittextNineAxisLeft = null;
	private Button saveButton, backButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DatabaseLogger.active=true;

		setContentView(R.layout.network_setup_layout);
		this.loadActivationFromFile();
		this.loadConfigFromFile();
		initUI();
		saveButton = (Button) findViewById(R.id.SaveDeadPeriod);
		backButton = (Button) findViewById(R.id.BackDeadPeriod);
		backButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//				showDialog(DAYSTART_TIME_DIALOG_ID);
				finish();
			}
		});
		saveButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//				showDialog(DAYSTART_TIME_DIALOG_ID);
				long curtime=System.currentTimeMillis();
				String entry="";
				writeConfigToFile();
				DatabaseLogger.active=true;
				DatabaseLogger db=DatabaseLogger.getInstance(this);
				if(edittextRIPECG.getText()!=null){
					entry="RIP,"+edittextRIPECG.getText().toString().trim();
					db.logAnything("moteid", entry, curtime);
				}
				if(edittextNineAxisRight.getText()!=null) {
					entry="R9,"+edittextNineAxisRight.getText().toString().trim();
					db.logAnything("moteid", entry, curtime);
				}
				if(edittextNineAxisLeft.getText()!=null) {
					entry="L9,"+edittextNineAxisLeft.getText().toString().trim();
					db.logAnything("moteid", entry, curtime);
				}
				finish();
			}
		});

	}


	@Override
	public void onPause() {
		super.onPause();
		//if (configChanged) {
		//			writeConfigToFile();
		//	configChanged = false;
		//}
	}

	@Override
	public void onResume() {
		super.onResume();
		edittextRIPECG.setText(Constants.antAddress[Constants.MOTE_RIPECG_IND]);
		edittextNineAxisRight.setText(Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND]);
		edittextNineAxisLeft.setText(Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND]);
	}

	void loadActivationFromFile() {
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.ACTIVATION_CONFIG_FILENAME);
		if (!setupFile.exists())
			return;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document dom = null;
		try {
			dom = builder.parse(setupFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element xmlroot = dom.getDocumentElement();

		NodeList nodeList = xmlroot.getElementsByTagName("startup");
		if (nodeList.getLength() > 0) {
			Element element = (Element)nodeList.item(0);
			for(int i=0;i<Constants.MOTE_NO;i++)
				Constants.moteActive[i]=false;
			nodeList = element.getElementsByTagName("mote");
			for (int i=0; i < nodeList.getLength(); i++) {
				element = (Element)nodeList.item(i);
				String moteName = element.getFirstChild().getNodeValue();
				Field field;
				try {
					field = Constants.class.getField(moteName);
					int moteID = field.getInt(null);
					Constants.moteActive[moteID]=true;
					Log.h(TAG, "Activated : " + moteName + " = " + moteID);
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	void loadConfigFromFile() {
		File root = Environment.getExternalStorageDirectory();
		Constants.antAddress[Constants.MOTE_RIPECG_IND]="";
		Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND]="";
		Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND]="";

		try {
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();

			File setupFile = new File(dir, Constants.NETWORK_CONFIG_FILENAME);
			if (!setupFile.exists())
				return;

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(setupFile);

			Element xmlroot = dom.getDocumentElement();

			NodeList nodeList;
			for (int i=1;i<=Constants.MOTE_NO;i++){
				nodeList = xmlroot.getElementsByTagName("antdevice"+Integer.toString(i));
				Node node = nodeList.item(0);
				if(node!=null && node.hasChildNodes()){
					if(Log.DEBUG_MONOWAR_NEW) Log.mm("A","id="+i+" "+node.getFirstChild().getNodeValue());
					switch(i) {
					case 1:
						Constants.antAddress[Constants.MOTE_RIPECG_IND] = node.getFirstChild().getNodeValue();
						break;
					case 2:
						Constants.antAddress[Constants.MOTE_ALCOHOL_IND] = node.getFirstChild().getNodeValue();
						break;
					case 3:
						Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND] = node.getFirstChild().getNodeValue();
						break;
					case 4:
						Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND] = node.getFirstChild().getNodeValue();
						break;
					case 5:
						Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND] = node.getFirstChild().getNodeValue();
						Log.h(TAG, "Right 9-axis : " + Constants.MOTE_NINE_AXIS_RIGHT_IND + " : id = " + node.getFirstChild().getNodeValue());
						break;
					case 6:
						Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND] = node.getFirstChild().getNodeValue();
						Log.h(TAG, "Left 9-axis : " + Constants.MOTE_NINE_AXIS_LEFT_IND + " : id = " + node.getFirstChild().getNodeValue());
						break;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("SETUP",e.getMessage());
			e.printStackTrace();
		}
	}

	void writeConfigToFile() {
		try {
			ArrayList<Integer> sensorMotes = new ArrayList<Integer>();

			File root = Environment.getExternalStorageDirectory();
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();
			File setupFile = new File(dir, Constants.NETWORK_CONFIG_FILENAME);

			BufferedWriter writer = new BufferedWriter(new FileWriter(setupFile));
			writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			writer.write("<fieldstream>\n");
			writer.write("\t<network>\n");
			if(edittextRIPECG.getText()!=null)
				writer.write("\t\t<antdevice1>");   writer.write(edittextRIPECG.getText().toString().trim());   writer.write("</antdevice1>\n");

				if(edittextNineAxisRight.getText()!=null) {
					writer.write("\t\t<antdevice5>");   writer.write(edittextNineAxisRight.getText().toString().trim());   writer.write("</antdevice5>\n");
				}
				if(edittextNineAxisLeft.getText()!=null) {
					writer.write("\t\t<antdevice6>");   writer.write(edittextNineAxisLeft.getText().toString().trim());   writer.write("</antdevice6>\n");
				}

				for (Integer i : sensorMotes) {
					writer.write("\t\t<sensor_mote>");   writer.write(String.valueOf(i));   writer.write("</sensor_mote>\n");
				}
				writer.write("\t</network>\n");
				writer.write("</fieldstream>");

				writer.close();

				Toast.makeText(getApplicationContext(), "Network Setup Saved Successfully", Toast.LENGTH_SHORT).show();

		}
		catch(Exception e) {
			// TODO Auto-generated catch block
			Log.d("SETUP",e.getMessage());
			e.printStackTrace();

			Toast.makeText(getApplicationContext(), "Error Saving Network Setup", Toast.LENGTH_SHORT).show();
		}
	}


	public void initUI()
	{

		edittextRIPECG = (EditText) findViewById(R.id.edittextRIPECG);
		edittextNineAxisRight = (EditText) findViewById(R.id.edittextNineAxisRight);
		edittextNineAxisLeft = (EditText) findViewById(R.id.edittextNineAxisLeft);

		TextView t;
		if(Constants.moteActive[Constants.MOTE_RIPECG]==false){
			edittextRIPECG.setVisibility(View.GONE);
			t=(TextView)findViewById(R.id.textANTRIPECG);
			t.setVisibility(View.GONE);
			//			t=(TextView)findViewById(R.id.text0name);
			//			t.setVisibility(View.GONE);
		}
		if(Constants.moteActive[Constants.MOTE_9_AXIS_RIGHT]==false){
			edittextNineAxisRight.setVisibility(View.GONE);
			//			t=(TextView)findViewById(R.id.text40x);
			//			t.setVisibility(View.GONE);
			t=(TextView)findViewById(R.id.textANTWR);
			t.setVisibility(View.GONE);
		}
		if(Constants.moteActive[Constants.MOTE_9_AXIS_LEFT]==false){
			edittextNineAxisLeft.setVisibility(View.GONE);
			//			t=(TextView)findViewById(R.id.text50x);
			//			t.setVisibility(View.GONE);
			t=(TextView)findViewById(R.id.textANTWL);
			t.setVisibility(View.GONE);
		}


		edittextRIPECG.setText(Constants.antAddress[Constants.MOTE_RIPECG_IND]);
		edittextNineAxisRight.setText(Constants.antAddress[Constants.MOTE_NINE_AXIS_RIGHT_IND]);
		edittextNineAxisLeft.setText(Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND]);

		edittextRIPECG.setInputType(InputType.TYPE_CLASS_NUMBER);
		edittextNineAxisRight.setInputType(InputType.TYPE_CLASS_NUMBER);
		edittextNineAxisLeft.setInputType(InputType.TYPE_CLASS_NUMBER);
	}

	InputFilter inputFilterHex = new InputFilter() {

		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

			String str = "";
			if(dest.length()>=4) {
				return str;
			}
			for (int i = start; i < end; i++) {
				if (	Character.isDigit(source.charAt(i)) ||
						(source.charAt(i)>='a' && source.charAt(i)<='f' ) ||
						(source.charAt(i)>='A' && source.charAt(i)<='F' )
						) {
					str = str + source.charAt(i);
				}
			}
			if (str.length() > 4) {
				str = str.substring(0, 4);
			}
			Log.h(TAG, source + " --- " + str + " --- " + dest.toString());
			return str;
		}
	};

}
