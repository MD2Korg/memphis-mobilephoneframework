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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.oscilloscope.ECGRIPOscilloscopeActivity;
import org.fieldstream.oscilloscope.OscilloscopeActivity;
import org.fieldstream.service.IInferrenceService;
import org.fieldstream.service.IInferrenceServiceCallback;
import org.fieldstream.service.InferrenceService;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.logger.TopExceptionHandler;
import org.fieldstream.service.sensors.mote.ant.AntStateManager;
import org.fieldstream.service.sensors.mote.sensors.QualityBuffer.DataQualityColorEnum;
import org.fieldstream.service.sensors.mote.sensors.SensorDataQualitySingleton.SensorTypeQualityEnum;
import org.fieldstream.service.sensors.mote.sensors.SensorDataQualitySingleton;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import edu.cmu.ices.stress.phone.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;



import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;


@SuppressLint("NewApi")
public class BaseActivity extends Activity {

	private Intent oscopeIntent;
	private Intent ecgripDemoIntent;
	private boolean isConnected=false;
	private String failMessage="";
	protected Dialog keypad = null;
	protected boolean keypadVisible = false;
	protected ImageButton one;
	protected ImageButton two;
	protected ImageButton three;
	protected ImageButton four;
	protected ImageButton five;
	protected ImageButton six;
	protected ImageButton seven;
	protected ImageButton eight;
	protected ImageButton nine;
	protected ImageButton zero;

	protected final static String EXIT_CODE = "7556";
	protected final static String OSCOPE_CODE ="6557";
	protected final static String ECGRIP_DEMO_CODE ="3366";
	protected final static String REPORT_CODE = "8812";

	protected String keypadCode = "";

	// inference service access
	public IInferrenceService service;
	private Intent inferenceServiceIntent;
	protected IInferrenceService inferenceService;
	private Intent fieldReportIntent;

	// state variables
	static protected int start=0;
	public static BaseActivity mainActivity=null;

	// Alarm
/*
	PendingIntent pi;
    BroadcastReceiver br;
    AlarmManager am;
    final static private long ONE_SECOND = 1000;
    final static private long TWENTY_SECONDS = ONE_SECOND * 20;
*/
	// wake lock to make sure main looper events execute
//	WakeLock wakelock;
//	boolean mDoDestroy = true;

	/* END USER INTERFACE DECLARATIONS */

	protected String titleText="FieldStudy";
	public static final boolean CONNECTED=true;
	public static final boolean NOT_CONNECTED=false;
	void setTitleBar(boolean connection) {
		setTitleBar(connection, "");
	}

	void setTitleBar(boolean connection, String message) {
		View titleView = getWindow().findViewById(android.R.id.title);
	    if (titleView != null) {
	      ViewParent parent = titleView.getParent();
	      if (parent != null && (parent instanceof View)) {
	        View parentView = (View)parent;
	        parentView.setBackgroundColor(Color.BLUE);
	      }
	    }
/*
		if(connection==CONNECTED){
			TextView status=(TextView) findViewById(R.id.status);
			status.setBackgroundColor(Color.rgb(0, 100, 0));
			status.setText("Connected");
			TextView title=(TextView) findViewById(R.id.myTitle);
			title.setBackgroundColor(Color.rgb(0, 50, 0));
			title.setText(titleText);


		}
		else if (connection==NOT_CONNECTED){
			TextView status=(TextView) findViewById(R.id.status);
			status.setBackgroundColor(Color.RED);
			status.setText("Not Connected");
			TextView title=(TextView) findViewById(R.id.myTitle);
			title.setText(message);
			title.setBackgroundColor(Color.RED);
		}
*/
	}


	protected IInferrenceServiceCallback inferenceCallback = new IInferrenceServiceCallback.Stub() {

		public void receiveCallback(int modelID, int value, long startTime,
				long endTime) throws RemoteException {

		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// why imp??
		mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 2000);
		DatabaseLogger.active=true;
//		DatabaseLogger.makeActive();
		Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

//		setContentView(R.layout.labstudy_layout);

		// grab a partial wake lock to make sure the main threads looper
		// continues running
		PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
//		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//		"BaseActivity");
//		wakelock.acquire();
		start++;
		if(start==1){
			loadNetworkConfig();
			startStuff();
		}
		keypad = createKeypad();
		initKeypad(keypad);
		oscopeIntent = new Intent(getBaseContext(), OscilloscopeActivity.class);
		ecgripDemoIntent = new Intent(getBaseContext(), ECGRIPOscilloscopeActivity.class);
		fieldReportIntent = new Intent(getBaseContext(),FieldReportActivity.class);
//		 setup();
//		am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TWENTY_SECONDS, pi );
	}
	void createAlertDialog(String msg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                finish();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}


	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		start--;
		if (start==0) {
			stopStuff();
			if (Constants.WRITETRACE)
				Debug.stopMethodTracing();

			// release the main activity wake lock so the processor can sleep
//			if (wakelock.isHeld()) {
//				wakelock.release();
//			}
//			DatabaseLogger.releaseInstance(this);
			DatabaseLogger.active=false;
		} else {

		}
//		Thread.currentThread().getName()
//		db.close();
//		db.close();

		// Alarm
//		am.cancel(pi);
//	    unregisterReceiver(br);
	    super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (inferenceService != null)
			try {
				inferenceService.logResume(System.currentTimeMillis());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	/* END ANDROID LIFE CYCLE */

	public void startStuff() {
		if (start==1)
			initService();
	}

	public void stopStuff() {
//		start--;
		if (start==0) {
			try {
				this.disconnect();
			} catch (RemoteException e) {
				Log.w("BaseActivity.stopStuff()", e.getMessage());
			}
		}
	}

	protected void connect() {
		// bindService(inferenceServiceIntent, service, 0);
		// service.subscribe(this);

	}

/*	public Object onRetainNonConfigurationInstance() {
		mDoDestroy = false;
		return new Object();
	}
*/

	protected void disconnect() throws RemoteException {
		inferenceService.unsubscribe(inferenceCallback);
		unbindService(inferenceConnection);
		stopService(inferenceServiceIntent);
		inferenceServiceIntent = null;
		inferenceConnection = null;
		inferenceCallback =  null;
	}

	private void initService() {
		// inferenceServiceIntent = new Intent();
		inferenceServiceIntent = new Intent(getBaseContext(),
				InferrenceService.class);
		// conn = new InferrenceServiceConnection();
		// Intent i = new Intent();
		// inferenceServiceIntent.setClassName( "edu.cmu.ices.stress.phone",
		//	// "edu.cmu.ices.stress.phone.service.InferrenceService" );
		startService(inferenceServiceIntent);
		bindService(inferenceServiceIntent, inferenceConnection, 0);

		Log.i("FeatureActivity", "bindService()");

	}
	public static ArrayList<Integer> activeMotesAutoSense2 = new ArrayList<Integer>() {
		{
			add(Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL);
			add(Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP);
			add(Constants.MOTE_TYPE_AUTOSENSE_2_NINE_AXIS);
		}
	};

	public static ArrayList<Integer> activeMotesAutoSense1 = new ArrayList<Integer>() {
		{
			add(Constants.MOTE_TYPE_AUTOSENSE_1_ECG);
			add(Constants.MOTE_TYPE_AUTOSENSE_1_RIP);
		}
	};

	private ServiceConnection inferenceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {

			inferenceService = IInferrenceService.Stub.asInterface(service);
			Log.i("inferenceConnection", "Connected to the inference service");

			//updateIncentivesDisplay();

			try {
				inferenceService.subscribe(inferenceCallback);
				Log.i("inferenceConnection",
				"Subscribed to the inference service callback");
				// inferenceService.activateModel(Constants.MODEL_STRESS);
				//				inferenceService.activateModel(Constants.MODEL_DATAQUALITY);
				//				inferenceService.activateModel(Constants.MODEL_CONVERSATION);
				//				inferenceService.activateModel(Constants.MODEL_ACTIVITY);

				// start the motes here
				if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_1)
				{
					for(Integer mote : activeMotesAutoSense1) {
						inferenceService.activateMote(mote);
					}
				}
				else if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_2)
				{
					for(Integer mote : activeMotesAutoSense2) {
						inferenceService.activateMote(mote);
					}
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName name) {

			if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_1)
			{
				for(Integer mote : activeMotesAutoSense1) {
					try {
					inferenceService.deactivateMote(mote);
					} catch (RemoteException e) {
						e.printStackTrace();
					} // end catch
				} // end for
			}
			else if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_2)
			{
				for(Integer mote : activeMotesAutoSense2) {
					try {
					inferenceService.deactivateMote(mote);
					} catch (RemoteException e) {
						e.printStackTrace();
					}// end catch
				} // end for
			} // end else if


			inferenceService = null;
			Log.i("inferenceConnection", "Disconnected from inference service");
//			label.setText("Stopped the study");
		}
	};

	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HOME){
        	Log.ema_alarm("", "KEY: home");
        	return true;
        }
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// disable the back button while in the interview
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_CALL) {
			// disable the call button
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			// disable camera
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_POWER) {
			// don't know if this can be disabled here
//			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			// disable search
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (keypadVisible == false) {
				//keypad = createKeypad();
				//initKeypad(keypad);
				keypadCode = "";
				keypadVisible = true;
				keypad.show();
			} else {
				keypad.hide();
				keypadVisible = false;
			}
			//finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private Dialog createKeypad() {
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.keypad_layout);
		dialog.setTitle("Enter exit key, or press back");
		dialog.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_CALL) {
					return true;
				}
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_CAMERA) {
					return true;
				}
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					if (keypadVisible) {
						keypad.hide();
						keypadVisible = false;
					}
					return true;
				}
				return false;
			}

		});

		return dialog;
	}

	private void initKeypad(Dialog dialog) {
		one = (ImageButton) dialog.findViewById(R.id.one);
		two = (ImageButton) dialog.findViewById(R.id.two);
		three = (ImageButton) dialog.findViewById(R.id.three);
		four = (ImageButton) dialog.findViewById(R.id.four);
		five = (ImageButton) dialog.findViewById(R.id.five);
		six = (ImageButton) dialog.findViewById(R.id.six);
		seven = (ImageButton) dialog.findViewById(R.id.seven);
		eight = (ImageButton) dialog.findViewById(R.id.eight);
		nine = (ImageButton) dialog.findViewById(R.id.nine);
		zero = (ImageButton) dialog.findViewById(R.id.zero);

		// set up key handlers
		one.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "1";
				checkKeypadCodes();
			}

		});
		two.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "2";
				checkKeypadCodes();
			}

		});
		three.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "3";
				checkKeypadCodes();
			}

		});
		four.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "4";
				checkKeypadCodes();
			}

		});
		five.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "5";
				checkKeypadCodes();
			}

		});
		six.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "6";
				checkKeypadCodes();
			}

		});
		seven.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "7";
				checkKeypadCodes();
			}

		});
		eight.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "8";
				checkKeypadCodes();
			}

		});
		nine.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "9";
				checkKeypadCodes();
			}

		});
		zero.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				keypadCode += "0";
				checkKeypadCodes();
			}

		});

	}

	protected void checkKeypadCodes() {


		if (keypadCode.length() == EXIT_CODE.length()
				|| keypadCode.length() == OSCOPE_CODE.length()
				|| keypadCode.length() == ECGRIP_DEMO_CODE.length()
				|| keypadCode.length() == REPORT_CODE.length()) {
			if (keypadCode.equalsIgnoreCase(EXIT_CODE)) {
				keypad.hide();
				keypadVisible = false;
				keypad.dismiss();
				keypad = null;
				finish();
			} else if (keypadCode.equalsIgnoreCase(OSCOPE_CODE)) {
				keypad.hide();
				keypadVisible = false;
				this.startActivity(oscopeIntent);
			} else if (keypadCode.equalsIgnoreCase(ECGRIP_DEMO_CODE)) {
				keypad.hide();
				keypadVisible = false;
				this.startActivity(ecgripDemoIntent);

			} else if (keypadCode.equalsIgnoreCase(REPORT_CODE)) {

				keypad.hide();
				keypadVisible = false;
				startActivity(fieldReportIntent);

			} else {
				keypad.hide();
				keypadVisible = false;

			}
		}
	}
	static void loadNetworkConfig() {
		File root = Environment.getExternalStorageDirectory();
		try {
			File dir = new File(root+"/"+Constants.CONFIG_DIR);
			dir.mkdirs();

			File setupFile = new File(dir, Constants.NETWORK_CONFIG_FILENAME);
			if (!setupFile.exists()){
//				createAlertDialog("\""+Constants.NETWORK_CONFIG_FILENAME+"\" is not found");
				return;
			}

	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(setupFile);

            Element xmlroot = dom.getDocumentElement();
            NodeList nodeList;
            Constants.antAddress[Constants.MOTE_RIPECG_IND]="";
            Constants.antAddress[Constants.MOTE_ALCOHOL_IND]="";
            Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]="";
            Constants.antAddress[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]="";

            for (int i=1;i<=Constants.MOTE_NO;i++){
            	nodeList = xmlroot.getElementsByTagName("antdevice"+Integer.toString(i));
            	Node node = nodeList.item(0);
            	if(node!=null && node.hasChildNodes()){
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
            			break;
            		case 6:
            			Constants.antAddress[Constants.MOTE_NINE_AXIS_LEFT_IND] = node.getFirstChild().getNodeValue();
            			break;
            		}
            	}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("LabStudyActivity",e.getMessage());
			e.printStackTrace();
		}
	}
	private Handler mHandler = new Handler();

private Runnable mUpdateTimeTask = new Runnable() {

	private int getColor(DataQualityColorEnum dataQualityColorEnum) {
		switch(dataQualityColorEnum) {
		case RED:
			return Color.RED;
		case YELLOW:
			return Color.YELLOW;
		case GREEN:
			return Color.GREEN;
		}
		return Color.BLACK;
	}

	long lastLogTimestamp = -1;

	   public void run() {
//		   if(true) return;
		   TextView txtLeftMoteTitle = (TextView)findViewById(R.id.txtLeftMoteTitle);
		   TextView txtRipTitle = (TextView)findViewById(R.id.txtRipTitle);
		   TextView txtEcgTitle = (TextView)findViewById(R.id.txtEcgTitle);
		   TextView txtRightMoteTitle = (TextView)findViewById(R.id.txtRightMoteTitle);

		   long timestampCurrent = System.currentTimeMillis();
		   if(lastLogTimestamp==-1) {
			   lastLogTimestamp = timestampCurrent-3000;
		   }

		   try {
			   if(Constants.moteActive[Constants.MOTE_RIPECG_IND]==true){
				   DataQualityColorEnum dataQualityColorRip = SensorDataQualitySingleton.getInstance().getDataQualityColorRip();
				   txtRipTitle.setBackgroundColor(getColor(dataQualityColorRip));
				   SensorDataQualitySingleton.getInstance().logSensorDataQuality(SensorTypeQualityEnum.RIP, lastLogTimestamp, timestampCurrent, dataQualityColorRip);
			   }
			   if(Constants.moteActive[Constants.MOTE_RIPECG_IND]==true){
				   DataQualityColorEnum dataQualityColorEcg = SensorDataQualitySingleton.getInstance().getDataQualityColorEcg();
				   txtEcgTitle.setBackgroundColor(getColor(dataQualityColorEcg));
				   SensorDataQualitySingleton.getInstance().logSensorDataQuality(SensorTypeQualityEnum.ECG, lastLogTimestamp, timestampCurrent, dataQualityColorEcg);
			   }
			   if(Constants.moteActive[Constants.MOTE_NINE_AXIS_RIGHT_IND]==true){
				   DataQualityColorEnum dataQualityColorWristRight = SensorDataQualitySingleton.getInstance().getDataQualityColorWristRight();
				   txtRightMoteTitle.setBackgroundColor(getColor(dataQualityColorWristRight));
				   SensorDataQualitySingleton.getInstance().logSensorDataQuality(SensorTypeQualityEnum.WristRight, lastLogTimestamp, timestampCurrent, dataQualityColorWristRight);
			   }
			   if(Constants.moteActive[Constants.MOTE_NINE_AXIS_LEFT_IND]==true){
				   DataQualityColorEnum dataQualityColorWristLeft = SensorDataQualitySingleton.getInstance().getDataQualityColorWristLeft();
				   txtLeftMoteTitle.setBackgroundColor(getColor(dataQualityColorWristLeft));
				   SensorDataQualitySingleton.getInstance().logSensorDataQuality(SensorTypeQualityEnum.WristLeft, lastLogTimestamp, timestampCurrent, dataQualityColorWristLeft);
			   }
		   } catch (Exception e) {
			   Log.e("BaseActivity", e.getMessage());
		   }
		   lastLogTimestamp = timestampCurrent;
		   /*
	       //final long start = mStartTime;
	       //long millis = SystemClock.uptimeMillis() - start;
	       //int seconds = (int) (millis / 1000);
	       //int minutes = seconds / 60;
	       //seconds     = seconds % 60;

	       //if (seconds < 10) {
	       //    mTimeLabel.setText("" + minutes + ":0" + seconds);
	       //} else {
	       //    mTimeLabel.setText("" + minutes + ":" + seconds);
	       //}

		   //if(isConnected!=AntStateManager.isReceived[Constants.MOTE_RIPECG_IND]){
			//   isConnected=AntStateManager.isReceived[Constants.MOTE_RIPECG_IND];
			//   setTitleBar(isConnected);
		   //}
		   String newFailMessage="";
		   Boolean newIsConnected = true;

		   if(!AntStateManager.isReceived[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND] && Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_LEFT_IND]==true){
			   newIsConnected = false;
			   if(newFailMessage.length()>0) {
				   newFailMessage += ",";
			   }
			   newFailMessage += "Left";
		   }
		   if(!AntStateManager.isReceived[Constants.MOTE_NINE_AXIS_LEFT_IND] && Constants.moteActive[Constants.MOTE_NINE_AXIS_LEFT_IND]==true){
			   newIsConnected = false;
			   if(newFailMessage.length()>0) {
				   newFailMessage += ",";
			   }
			   newFailMessage += "L(9)";

			   txtLeftMoteTitle.setBackgroundColor(Color.RED);
		   } else {
			   txtLeftMoteTitle.setBackgroundColor(Color.GREEN);
		   }

		   if(!AntStateManager.isReceived[Constants.MOTE_RIPECG_IND] && Constants.moteActive[Constants.MOTE_RIPECG_IND]==true){
			   newIsConnected = false;
			   if(newFailMessage.length()>0) {
				   newFailMessage += ",";
			   }
			   newFailMessage += "Chest";

			   txtEcgTitle.setBackgroundColor(Color.RED);
			   //txtRipTitle.setBackgroundColor(Color.RED);
		   } else {
			   txtEcgTitle.setBackgroundColor(Color.GREEN);
			   //txtRipTitle.setBackgroundColor(Color.GREEN);
		   }

		   if(!AntStateManager.isReceived[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND] && Constants.moteActive[Constants.MOTE_ALCOHOL_ACCL_RIGHT_IND]==true){
			   newIsConnected = false;
			   if(newFailMessage.length()>0) {
				   newFailMessage += ",";
			   }
			   newFailMessage += "Right";
		   }
		   if(!AntStateManager.isReceived[Constants.MOTE_NINE_AXIS_RIGHT_IND] && Constants.moteActive[Constants.MOTE_NINE_AXIS_RIGHT_IND]==true){
			   newIsConnected = false;
			   if(newFailMessage.length()>0) {
				   newFailMessage += ",";
			   }
			   newFailMessage += "R(9)";
			   txtRightMoteTitle.setBackgroundColor(Color.RED);
		   } else {
			   txtRightMoteTitle.setBackgroundColor(Color.GREEN);
		   }
		   newFailMessage = "Sensor: " + newFailMessage;

		   if(newIsConnected != isConnected || !newFailMessage.equals(failMessage)) {
			   isConnected = newIsConnected;
			   failMessage = newFailMessage;
			   setTitleBar(isConnected, failMessage);
			   Log.h("Hillol", "Connected: " + isConnected + ", " + failMessage);
		   }
		   */

		   mHandler.postDelayed(this,3000);
	   }
	};

/*	private void setup() {
	    br = new BroadcastReceiver() {
	           @Override
	           public void onReceive(Context c, Intent i) {
	                  Toast.makeText(c, "Rise and Shine!", Toast.LENGTH_LONG).show();
	                  }
	           };
	    registerReceiver(br, new IntentFilter("org.fieldstream") );
	    pi = PendingIntent.getBroadcast( this, 0, new Intent("org.fieldstream"),
	0 );
	    am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
	}
	*/
}
