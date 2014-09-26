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
package org.fieldstream.service.sensors.mote;

import org.fieldstream.Constants;

public class PacketLoss {

	public static final int MISSING_TOKEN_ECG = -100;
	public static final int MISSING_TOKEN_ACCEL_X = -101;
	public static final int MISSING_TOKEN_ACCEL_Y = -102;
	public static final int MISSING_TOKEN_ACCEL_Z = -103;
	public static final int MISSING_TOKEN_TEMP_BODY = -104;
	public static final int MISSING_TOKEN_TEMP_AMBIENT = -105;
	public static final int MISSING_TOKEN_GSR = -106;

	public static final int MISSING_TOKEN_RIP = -107;


	public static final int MISSING_TOKEN_ALCOHOL = -210;
	public static final int MISSING_TOKEN_GSRWRIST = -211;
	public static final int MISSING_TOKEN_TEMPWRIST = -212;

	public static int getMissingToken(int sensorID)
	{
		int missingToken = -1;

		switch(sensorID)
		{
			case Constants.SENSOR_ACCELCHESTX:
				missingToken = MISSING_TOKEN_ACCEL_X;
				break;

			case Constants.SENSOR_ACCELCHESTY:
				missingToken = MISSING_TOKEN_ACCEL_Y;
				break;

			case Constants.SENSOR_ACCELCHESTZ:
				missingToken = MISSING_TOKEN_ACCEL_Z;
				break;

			case Constants.SENSOR_ECK:
				missingToken = MISSING_TOKEN_ECG;
				break;

			case Constants.SENSOR_AMBIENT_TEMP:
				missingToken = MISSING_TOKEN_TEMP_AMBIENT;
				break;

			case Constants.SENSOR_BODY_TEMP:
				missingToken = MISSING_TOKEN_TEMP_BODY;
				break;

			case Constants.SENSOR_GSR:
				missingToken = MISSING_TOKEN_GSR;
				break;

			case Constants.SENSOR_RIP:
				missingToken = MISSING_TOKEN_RIP;
				break;

			case Constants.SENSOR_ALCOHOL:
				missingToken = MISSING_TOKEN_ALCOHOL;
				break;

			case Constants.SENSOR_GSRWRIST:
				missingToken = MISSING_TOKEN_GSRWRIST;
				break;

			case Constants.SENSOR_TEMPWRIST:
				missingToken = MISSING_TOKEN_TEMPWRIST;
				break;
		}
		return missingToken;
	}


}
