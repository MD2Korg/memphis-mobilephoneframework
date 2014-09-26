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
