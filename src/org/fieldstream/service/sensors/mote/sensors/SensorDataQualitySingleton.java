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

import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.virtual.EckQualityCalculation;
import org.fieldstream.service.sensor.virtual.RipQualityCalculation;
import org.fieldstream.service.sensor.virtual.WristSensorOnBody;
import org.fieldstream.service.sensors.mote.ChannelToSensorMapping;
import org.fieldstream.service.sensors.mote.sensors.QualityBuffer.DataQualityColorEnum;
import org.fieldstream.service.sensors.mote.sensors.QualityBuffer.DataQualityEnum;

public class SensorDataQualitySingleton {

	final String TAG = "SensorDataQualitySingleton";
	private static SensorDataQualitySingleton INSTANCE;
	Map<Integer, QualityBuffer> qBufferMap = new HashMap<Integer, QualityBuffer>();

	final int[] sensorIds = {
			ChannelToSensorMapping.RIP,
			ChannelToSensorMapping.ECG,
			ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X,
			ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X
	};

	public enum SensorTypeQualityEnum {
		RIP(0), ECG(1), WristLeft(2), WristRight(3);
		private int value;

		private SensorTypeQualityEnum(int value) {
			this.value = value;
		}
		public void set(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	};

	private SensorDataQualitySingleton() {
		for(int sensorId: sensorIds) {
			qBufferMap.put(sensorId, new QualityBuffer(sensorId));
		}
	}

	public static SensorDataQualitySingleton getInstance() {
		if(INSTANCE==null) {
			INSTANCE = new SensorDataQualitySingleton();
		}
		return INSTANCE;
	}

	public DataQualityColorEnum getChestMoteDataQualityColor(int sensorId) {
		QualityBuffer qualityBuffer = qBufferMap.get(sensorId);
		int[] samples = SensorDataBufferSingleton.getInstance().getSensorBufferReset(sensorId);
		if(Log.DEBUG_HILLOL) {
			//Log.h(TAG, "sensorId="+sensorId+" : Sample Count="+samples.length+" over the last 3 second");
		}
		// Get quality
		if(samples.length<5) {
			//return DataQualityColorEnum.RED;
			return qualityBuffer.getQualityColor();
		}

		int quality=3;
		if(sensorId==ChannelToSensorMapping.RIP) {
			RipQualityCalculation ripQualityCalculation = new RipQualityCalculation();
			quality=ripQualityCalculation.currentQuality(samples);
		} else if(sensorId==ChannelToSensorMapping.ECG) {
			EckQualityCalculation ecgQualityCalculation = new EckQualityCalculation();
			quality=ecgQualityCalculation.currentQuality(samples);
		}

		DataQualityEnum qualityEnum = getDataQualityEnum(quality);
		if(Log.DEBUG_HILLOL) {
			//Log.h(TAG, "quality = " + quality);
			//Log.h(TAG, "qualityEnum = " + qualityEnum.toString());
		}
		qualityBuffer.push(qualityEnum);

		if(qualityEnum==DataQualityEnum.GOOD) {
			return DataQualityColorEnum.GREEN; // Good news should be passed immediately.
		}

		return qualityBuffer.getQualityColor();
	}

	public DataQualityColorEnum getDataQualityColorRip() {
		return getChestMoteDataQualityColor(ChannelToSensorMapping.RIP);
	}

	public DataQualityColorEnum getDataQualityColorEcg() {
		return getChestMoteDataQualityColor(ChannelToSensorMapping.ECG);
	}

	public DataQualityEnum getDataQualityEnum(int quality) {
		switch(quality) {
		case 0:
			return DataQualityEnum.GOOD;
		case 1:
			return DataQualityEnum.LOOSE;
		case 2:
			return DataQualityEnum.NOISE;
		case 3:
			return DataQualityEnum.OFF;
		}
		return DataQualityEnum.OFF;
	}

	public DataQualityColorEnum getWristMoteDataQualityColor(int sensorIdX, int sensorIdY, int sensorIdZ) {
		QualityBuffer qualityBuffer = qBufferMap.get(sensorIdX);

		int[] samplesX = SensorDataBufferSingleton.getInstance().getSensorBufferReset(sensorIdX);
		//int[] samplesY = SensorDataBufferSingleton.getInstance().getSensorBufferReset(sensorIdY);
		//int[] samplesZ = SensorDataBufferSingleton.getInstance().getSensorBufferReset(sensorIdZ);
		if(Log.DEBUG_HILLOL) {
			//Log.h(TAG, "sensorId="+sensorIdX+" : Sample Count="+samplesX.length+" over the last 3 second");
			//Log.h(TAG, "sensorId="+sensorIdY+" : Sample Count="+samplesY.length+" over the last 3 second");
			//Log.h(TAG, "sensorId="+sensorIdZ+" : Sample Count="+samplesZ.length+" over the last 3 second");
		}
		// Get quality
		if(samplesX.length<5) { //|| samplesY.length<5 || samplesZ.length<5) {
			return qualityBuffer.getQualityColor();
		}


		WristSensorOnBody wristSensorOnBody = new WristSensorOnBody();
		//int quality = wristSensorOnBody.currentQuality(samplesX, samplesY, samplesZ);
		int quality = wristSensorOnBody.currentQuality(samplesX);

		DataQualityEnum qualityEnum = getDataQualityEnum(quality);

		if(Log.DEBUG_HILLOL) {
			//String msg = "["+sensorIdX + "," + sensorIdY + "," + sensorIdZ +"]";
			String msg = "["+sensorIdX + "]";
			//Log.h(TAG, "Wrist"+msg+" quality = " + quality);
			//Log.h(TAG, "Wrist"+msg+" qualityEnum = " + qualityEnum.toString());
		}

		qualityBuffer.push(qualityEnum);

		if(qualityEnum==DataQualityEnum.GOOD) {
			return DataQualityColorEnum.GREEN; // Good news should be passed immediately.
		}

		return qualityBuffer.getQualityColor();
	}

	public DataQualityColorEnum getDataQualityColorWristLeft() {
		return getWristMoteDataQualityColor(ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X, ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Y, ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Z);
	}
	public DataQualityColorEnum getDataQualityColorWristRight() {
		return getWristMoteDataQualityColor(ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X, ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Y, ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Z);
	}

	public void logSensorDataQuality(SensorTypeQualityEnum sensorType, long timestampFrom, long timestampTo, DataQualityColorEnum quality) {
		DatabaseLogger.getInstance(this).logDataQuality(sensorType.getValue(), timestampFrom, timestampTo, quality.getValue());
	}

}
