package org.fieldstream.service.sensors.mote.sensors;

import org.fieldstream.service.logger.Log;

public class SensorBuffer {
	
	private final String TAG = "SensorBuffer";
	
	private int sensorId;
	private double windowSec;
	private double samplingHz;
	private boolean roll;
	private long lastReceivedTimestamp = -1;
	
	private Object lock = new Object();
	
	private int buffer[];
	private int bCount = 0;
	
	public SensorBuffer(int sensorId, double windowSec, double samplingHz, boolean roll) {
		this.sensorId = sensorId;
		this.windowSec = windowSec;
		this.samplingHz = samplingHz;
		this.roll = roll;
		
		int windowSize = (int) Math.ceil(windowSec*samplingHz);
		this.buffer = new int[windowSize];
	}
	
	/*
	public void pushData(int data[]) {
		try {
			int bufferLength = Math.min(buffer.length-data.length, bCount);
			System.arraycopy(buffer, 0, buffer, data.length, bufferLength); // Shift to right
			System.arraycopy(data, 0, buffer, 0, data.length); // Copy at the beginning
			bCount = bCount + data.length;
		} catch(Exception e) {
			if(Log.DEBUG_HILLOL) {
				String msg = "buffer.length="+buffer.length
						+", data.length="+data.length
						+", bCount=" + bCount;
				Log.e(TAG, "Error: pushData: " + e.getMessage() + ": " + msg);
			}
		}
	}
	*/
	public void pushData(int data[]) {
		try {
			synchronized (lock) {
				lastReceivedTimestamp = System.currentTimeMillis();
				int bc = bCount;
				if(bc+data.length > buffer.length) {
					System.arraycopy(buffer, data.length, buffer, 0, bc-data.length); // Overflow! Shift left
					bc = bc - data.length;
				}
				System.arraycopy(data, 0, buffer, bc, data.length); // Copy at the end
				bCount = bc+data.length;
			}
		} catch(Exception e) {
			//if(Log.DEBUG_HILLOL) {
				String msg = "buffer.length="+buffer.length
						+", data.length="+data.length
						+", bCount=" + bCount;
				Log.e(TAG, "Error: pushData: " + e.getMessage() + ": " + msg);
			//}
		}
	}
	
	/*
	 * Hillol: Strange. This is generating exception randomly. Example:
	 * 05-12 02:45:32.651: D/SensorBuffer(4323): Hillol: Exception: bCount=65, buffer.length=64
	 * 05-12 02:46:32.551: D/SensorBuffer(4323): Hillol: Exception: bCount=45, buffer.length=48
	public synchronized int[] getBufferReset() {
		if(bCount>=buffer.length) {
			bCount = buffer.length;
		}
		int[] b = new int[bCount];
		System.arraycopy(buffer, 0, b, 0, bCount);
		bCount = 0;
		return b;
	}
	*/
	public int[] getBufferReset() {
		try {
			synchronized (lock) {
				long currTimestamp = System.currentTimeMillis();
				if (lastReceivedTimestamp==-1 || (currTimestamp-lastReceivedTimestamp)>3000) {
					
					if(Log.DEBUG_HILLOL) {
						Log.h(TAG, "sensorId="+sensorId+": No data received in the last 3 sec to check quality.");
					}
					
					
					this.bCount = 0;
					return new int[0];
				}
				
				int bc = this.bCount;
				if(bc>buffer.length) {
					bc = buffer.length;
				}
				int[] b = new int[bc];
				System.arraycopy(buffer, 0, b, 0, bc);
				if(!roll) {
					this.bCount = 0;
				}
				return b;
			}
		} catch(Exception e) {
			//if(Log.DEBUG_HILLOL) {
				String msg = "buffer.length="+buffer.length
						+", bCount=" + bCount;
				Log.e(TAG, "Error: getBufferReset: " + e.getMessage() + ": " + msg);
			//}
			return new int [0];
		}
	}

}
