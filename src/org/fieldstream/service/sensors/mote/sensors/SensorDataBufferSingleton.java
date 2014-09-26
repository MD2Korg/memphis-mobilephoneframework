package org.fieldstream.service.sensors.mote.sensors;

import java.util.HashMap;
import java.util.Map;

import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.ChannelToSensorMapping;

public class SensorDataBufferSingleton {
	
	final String TAG = "SensorDataBufferQuality";
	private static SensorDataBufferSingleton INSTANCE;
	
	Map<Integer, SensorBuffer> sBufferMap = new HashMap<Integer, SensorBuffer>();
	
	public SensorDataBufferSingleton() {
		
		sBufferMap.put(ChannelToSensorMapping.RIP, new SensorBuffer(ChannelToSensorMapping.RIP, 7, 64.0/3.0, true));
		sBufferMap.put(ChannelToSensorMapping.ECG, new SensorBuffer(ChannelToSensorMapping.ECG, 3, 64.0, false));
		sBufferMap.put(ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X, new SensorBuffer(ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X, 3, 16.0, false));
		sBufferMap.put(ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X, new SensorBuffer(ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X, 3, 16.0, false));
	}
	
	public static SensorDataBufferSingleton getInstance() {
		if(INSTANCE==null) {
			INSTANCE = new SensorDataBufferSingleton();
		}
		return INSTANCE;
	}
	
	public void pushData(int sensorId, int[] data) {
		SensorBuffer sBuffer = sBufferMap.get(sensorId);
		if(sBuffer!=null) {
			//Log.h(TAG, "Pushing data for sensorId=" + sensorId + ", dataCount="+data.length);
			sBuffer.pushData(data);
		}
	}
	
	public int[] getSensorBufferReset(int sensorId) {
		SensorBuffer sBuffer = sBufferMap.get(sensorId);
		if(sBuffer!=null) {
			return sBuffer.getBufferReset();
		}
		return null;
	}
}
