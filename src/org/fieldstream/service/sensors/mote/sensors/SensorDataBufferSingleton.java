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
