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

import java.util.ArrayList;
import java.util.List;

import org.fieldstream.service.sensor.virtual.RipQualityCalculation;

public class QualityBuffer {

	private int sensorId;
	private int bufferLength = 10;
	List<QualityBufferRecord> listQualityRecord;

	public enum DataQualityEnum {
		GOOD(0), LOOSE(1), NOISE(2), OFF(3);
		private int value;

		private DataQualityEnum(int value) {
			this.value = value;
		}
		public void set(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	};

	public enum DataQualityColorEnum {
		RED(0), // No Data in last 10 window, each 3 sec
		YELLOW(1), // Other
		GREEN(2); // 8/10 window (3sec) contains good data
		private int value;

		private DataQualityColorEnum(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	};

	public class QualityBufferRecord {
		public long timestamp;
		public DataQualityEnum quality;
		public QualityBufferRecord(long timestamp, DataQualityEnum quality) {
			this.timestamp = timestamp;
			this.quality = quality;
		}
	}

	public QualityBuffer(int sensorId) {
		this.sensorId = sensorId;
		//RipQualityCalculation.DATA_QUALITY_GOOD
		listQualityRecord = new ArrayList<QualityBuffer.QualityBufferRecord>();
	}

	public void push(DataQualityEnum quality) {
		long timestamp = System.currentTimeMillis();;
		QualityBufferRecord record = new QualityBufferRecord(timestamp, quality);
		listQualityRecord.add(record);
		if(listQualityRecord.size()>bufferLength) {
			listQualityRecord.remove(0);
		}
	}

	public DataQualityColorEnum getQualityColor() {
		int goodCount = 0;
		int sampleCount = 0;
		long currentTimestamp = System.currentTimeMillis();
		long thresholdTT = currentTimestamp-bufferLength*3*1000;
		for(int i=0; i<listQualityRecord.size(); i++) {
			QualityBufferRecord record = listQualityRecord.get(i);
			if(record.timestamp>thresholdTT) {
				sampleCount++;
				switch(record.quality) {
				case GOOD:
					goodCount++;
					break;
				case LOOSE:
				case NOISE:
					break;
				case OFF:
				}
			}
		}
		if(sampleCount==0) {
			return DataQualityColorEnum.RED;
		} else if(goodCount>=8) {
			return DataQualityColorEnum.GREEN;
		} else {
			return DataQualityColorEnum.YELLOW;
		}
	}

}
