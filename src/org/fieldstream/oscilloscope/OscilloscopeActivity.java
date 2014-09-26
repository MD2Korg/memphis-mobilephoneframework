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


//@author Andrew Raij

package org.fieldstream.oscilloscope;

import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.R;
import org.fieldstream.service.ActivationManager;
import org.fieldstream.service.logger.Log;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.opengl.GLSurfaceView;

public class OscilloscopeActivity extends Activity {

	private static String TAG = "OscilloscopeActivity";
	public static boolean renderingSignals = false;

	private SignalRenderer sr;
	private OscilloscopeRenderer renderer;
	private GLSurfaceView oscopeView;
	private	Spinner signalSpinner;
	private ArrayAdapter<String> spinnerArrayAdapter;
	private HashMap<Integer, Integer> spinnerIndexToSignalID;

	WakeLock wakelock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(Log.DEBUG) Log.d("Monowar_ALL","OK    : (OscilloscopeAct) - Create");

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.oscilloscope_layout);

		PowerManager pm = (PowerManager)this.getSystemService(POWER_SERVICE);
		//wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AbstractInterview");
		wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "OscilloscopeActivity");

		initSpinner();

		initRenderer();


	}

	private void initSpinner() {
		signalSpinner = (Spinner) findViewById(R.id.OscilloscopeSpinner);
		spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		signalSpinner.setAdapter(spinnerArrayAdapter);
		signalSpinner.setOnItemSelectedListener(signalSpinnerOnItemSelectedListener);

		if(Constants.ENABLE_OSCILLOSCOPE_RENDERING_OPTIMIZATION) {
			signalSpinner.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_UP) {
						Log.h(TAG, "Spinner popup. So disable drawing");
						renderer.setDrawingEnabled(false);
						sr.setRenderingEnabled(false);
					}
					return false;
				}
			});
		}
	}

	private void initRenderer() {
		oscopeView = (GLSurfaceView) findViewById(R.id.OscilloscopeView);

		renderer = new OscilloscopeRenderer();
		if(sr!=null) {sr.stop();sr=null;}
		sr = new SignalRenderer();
		renderer.addSignal(sr);
		oscopeView.setRenderer(renderer);

		oscopeView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.h(TAG, "Click in Oscilloscope view. So enable drawing(if in case disabled).");
				if(renderer!=null) {
					renderer.setDrawingEnabled(true);
					sr.setRenderingEnabled(true);
				}
			}
		});
	}


    @Override
    protected void onPause() {
    	OscilloscopeActivity.renderingSignals = false;
    	if(Log.DEBUG_HILLOL) {
			Log.h(TAG, "onPause rendering signals");
		}
    	wakelock.release();
		if(sr!=null) {sr.stop();sr=null;}
        super.onPause();
        oscopeView.onPause();
		if(Log.DEBUG) Log.d("Monowar_ALL","OK    : (OscilloscopeAct) - Pause");

    }

    @Override
    protected void onResume() {
    	OscilloscopeActivity.renderingSignals = true;
    	if(Log.DEBUG_HILLOL) {
			Log.h(TAG, "onResume rendering signal");
		}
        super.onResume();

		spinnerIndexToSignalID = new HashMap<Integer, Integer>();
		addSensorToListIfAvailable(Constants.SENSOR_ECK);
		addSensorToListIfAvailable(Constants.SENSOR_BODY_TEMP);
		addSensorToListIfAvailable(Constants.SENSOR_RIP);
		addSensorToListIfAvailable(Constants.SENSOR_GSR);
		addSensorToListIfAvailable(Constants.SENSOR_ACCELCHESTX);
		addSensorToListIfAvailable(Constants.SENSOR_ACCELCHESTY);
		addSensorToListIfAvailable(Constants.SENSOR_ACCELCHESTZ);

		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_RIGHT_ACCL_X);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_RIGHT_ACCL_Y);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_RIGHT_ACCL_Z);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_RIGHT_GYRO_X);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_RIGHT_GYRO_Y);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_RIGHT_GYRO_Z);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_LEFT_ACCL_X);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_LEFT_ACCL_Y);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_LEFT_ACCL_Z);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_LEFT_GYRO_X);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_LEFT_GYRO_Y);
		addSensorToListIfAvailable(Constants.SENSOR_NINE_AXIS_LEFT_GYRO_Z);

//		addSensorToListIfAvailable(Constants.SENSOR_ALCOHOL);
//		addSensorToListIfAvailable(Constants.SENSOR_TEMPWRIST);
//		addSensorToListIfAvailable(Constants.SENSOR_GSRWRIST);
//		addSensorToListIfAvailable(Constants.SENSOR_ALCOHOL);
//		addFeatureToListIfAvailable(Constants.getId(Constants.FEATURE_HR, Constants.SENSOR_VIRTUAL_RR));
//		addFeatureToListIfAvailable(Constants.getId(Constants.FEATURE_RESP_RATE, Constants.SENSOR_RIP));

		wakelock.acquire();

		if (spinnerIndexToSignalID.size() > 0) {
			sr.setSignal(spinnerIndexToSignalID.get(0));
		}
		if(Log.DEBUG) Log.d("Monowar_ALL","OK    : (OscilloscopeAct) - Resume");

		oscopeView.onResume();
    }


	private void addSensorToListIfAvailable(int sensorID) {
		if (ActivationManager.sensors != null) {
			if (ActivationManager.sensors.containsKey(sensorID)) {
				if (!spinnerIndexToSignalID.containsValue(sensorID)) {
					spinnerIndexToSignalID.put(spinnerArrayAdapter.getCount(), sensorID);
					spinnerArrayAdapter.add(Constants.getSensorDescription(sensorID));
				}
			}
		}
	}

	/* This is called when the app is killed. */
	@Override
	protected void onDestroy() {
		if(Log.DEBUG) Log.d("Monowar_ALL","OK    : (OscilloscopeAct) - Destroy");
		if(sr!=null) {
			sr.stop();sr=null;
		}
		wakelock = null;

		super.onDestroy();
	}

	/* END ANDROID LIFE CYCLE */


@SuppressWarnings("unchecked")
private Spinner.OnItemSelectedListener signalSpinnerOnItemSelectedListener = new Spinner.OnItemSelectedListener()
{

	public void onItemSelected(AdapterView parent, View v, int position, long id)
	{
		renderer.setDrawingEnabled(true);
		sr.setRenderingEnabled(true);
		int value = spinnerIndexToSignalID.get(position);
		Log.d(TAG, "Selected signal " + value + ": " + spinnerArrayAdapter.getItem(position));
		sr.setSignal(value);
		sr.setSignalLabel(value);

	}

	public void onNothingSelected(AdapterView parent)
	{

	}
};

}
