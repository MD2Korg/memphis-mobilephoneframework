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

// @author Somnath Mitra


package org.fieldstream.service.sensors.mote;

import org.fieldstream.Constants;
import org.fieldstream.service.sensors.mote.sensors.GenericMoteSensor;
import org.fieldstream.service.sensors.mote.tinyos.TOSOscopeIntPacket;

public class TimeStamping {
	public static int getWindowSize(int sensortype)
	{
		int windowSize = -1;

		switch(sensortype)
		{
		case Constants.SENSOR_ACCELCHESTX:
			windowSize = GenericMoteSensor.getAccelchestwindowsize();
			break;

		case Constants.SENSOR_ACCELCHESTY:
			windowSize = GenericMoteSensor.getAccelchestwindowsize();
			break;

		case Constants.SENSOR_ACCELCHESTZ:
			windowSize = GenericMoteSensor.getAccelchestwindowsize();
			break;

		case Constants.SENSOR_ECK:
			windowSize = GenericMoteSensor.getEcgwindowsize();
//			frequency = GenericMoteSensor.getEcgframerate();
			break;

		case Constants.SENSOR_GSR:
//			frequency = GenericMoteSensor.getGsrframerate();
			windowSize = GenericMoteSensor.getGsrwindowsize();

			break;

		case Constants.SENSOR_BODY_TEMP:
//			frequency = GenericMoteSensor.getBodytempframerate();
			windowSize = GenericMoteSensor.getBodytempwindowsize();

			break;

		case Constants.SENSOR_AMBIENT_TEMP:
//			frequency = GenericMoteSensor.getAmbienttempframerate();
			windowSize = GenericMoteSensor.getAmbienttempwindowsize();

			break;

		case Constants.SENSOR_RIP:
//			frequency = GenericMoteSensor.getRipframerate();
			windowSize = GenericMoteSensor.getRipwindowsize();

			break;

		case Constants.SENSOR_ALCOHOL:
//			frequency = GenericMoteSensor.getAlcoholframerate();
			windowSize = GenericMoteSensor.getAlcoholwindowsize();

			break;

		case Constants.SENSOR_GSRWRIST:
//			frequency = GenericMoteSensor.getGsrframerate();
			windowSize = GenericMoteSensor.getGsrwindowsize();

			break;

		case Constants.SENSOR_TEMPWRIST:
//			frequency = GenericMoteSensor.getBodytempframerate();
			windowSize = GenericMoteSensor.getBodytempwindowsize();

			break;


		default:
			break;
		}
		return windowSize;
	}

/*	public static int getFrequency(int sensortype)
	{
		int frequency = -1;

		switch(sensortype)
		{
		case Constants.SENSOR_ACCELCHESTX:
			frequency = GenericMoteSensor.getAccelchestframerate();
			break;

		case Constants.SENSOR_ACCELCHESTY:
			frequency = GenericMoteSensor.getAccelchestframerate();
			break;

		case Constants.SENSOR_ACCELCHESTZ:
			frequency = GenericMoteSensor.getAccelchestframerate();
			break;

		case Constants.SENSOR_ECK:
			frequency = GenericMoteSensor.getEcgframerate();
			break;

		case Constants.SENSOR_GSR:
			frequency = GenericMoteSensor.getGsrframerate();
			break;

		case Constants.SENSOR_BODY_TEMP:
			frequency = GenericMoteSensor.getBodytempframerate();
			break;

		case Constants.SENSOR_AMBIENT_TEMP:
			frequency = GenericMoteSensor.getAmbienttempframerate();
			break;

		case Constants.SENSOR_RIP:
			frequency = GenericMoteSensor.getRipframerate();
			break;

		case Constants.SENSOR_ALCOHOL:
			frequency = GenericMoteSensor.getAlcoholframerate();
			break;

		case Constants.SENSOR_GSRWRIST:
			frequency = GenericMoteSensor.getGsrframerate();
			break;

		case Constants.SENSOR_TEMPWRIST:
			frequency = GenericMoteSensor.getBodytempframerate();
			break;


		default:
			break;
		}
		return frequency;
	}
*/
	public static int calculateNumberOfSamples(int sensortype,long timestamps)
	{
		int windowSize=getWindowSize(sensortype);
		int numberOfSamples=(int)(((double)(timestamps)*((double)(windowSize)))/(60.0*1000.0)+0.1);
		return numberOfSamples;
	}

	public static long[] timestampCalculator(long startSampleTime,long sampleCount, int sensortype,int size)
	{
		long[] timeStamps = new long[size];
		int windowSize=getWindowSize(sensortype);
		double f=(60000.0/(double)windowSize);
		for(int i=0; i < size;i++)
		{
			timeStamps[i]=startSampleTime+(long)(f*(double)(sampleCount+i));
/*			if(i == 0 )
			{
				timeStamps[i] = starttimestamp+(long)((1/(float)frequency)*1000);
				continue;
			}
			timeStamps[i] = timeStamps[i-1] + (long)((1/(float)frequency)*1000);
*/		}

		return timeStamps;
	}
////////////Newly added start ////////////////

	public static long[] timestampCalculator(long timestamp, int sensortype)
	{
		long[] timeStamps = new long[TOSOscopeIntPacket.DATA_INT_SIZE];
		int frequency = -1;

		switch(sensortype)
		{
		case Constants.SENSOR_ACCELCHESTX:
			frequency = GenericMoteSensor.getAccelchestframerate();
			break;

		case Constants.SENSOR_ACCELCHESTY:
			frequency = GenericMoteSensor.getAccelchestframerate();
			break;

		case Constants.SENSOR_ACCELCHESTZ:
			frequency = GenericMoteSensor.getAccelchestframerate();
			break;

		case Constants.SENSOR_ECK:
			frequency = GenericMoteSensor.getEcgframerate();
			break;

		case Constants.SENSOR_GSR:
			frequency = GenericMoteSensor.getGsrframerate();
			break;

		case Constants.SENSOR_BODY_TEMP:
			frequency = GenericMoteSensor.getBodytempframerate();
			break;

		case Constants.SENSOR_AMBIENT_TEMP:
			frequency = GenericMoteSensor.getAmbienttempframerate();
			break;

		case Constants.SENSOR_RIP:
			frequency = GenericMoteSensor.getRipframerate();
			break;

		case Constants.SENSOR_ALCOHOL:
			frequency = GenericMoteSensor.getAlcoholframerate();
			break;


		default:
					break;


		}
		for(int i=0; i < TOSOscopeIntPacket.DATA_INT_SIZE;i++)
		{
			timeStamps[i] = timestamp + i*((long)1/frequency);
		}

		return timeStamps;
	}
////////////Newly added end ////////////////
}
