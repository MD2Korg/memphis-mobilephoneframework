﻿//Copyright (c) 2010, University of Memphis
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

//@author Somnath Mitra
//@author Andrew Raij


package org.fieldstream.service.sensors.mote;

import org.fieldstream.Constants;

public class ChannelToSensorMapping {
	private static int AUTOSENSE_VERSION = 2;   // change this to 1 if you are using the
//												// old hardware with RIP and ECG on different motes
	public static final int ECG = 0;
	public static final int ACCELX = 1;
	public static final int ACCELY = 2;
	public static final int ACCELZ = 3;
	public static final int GSR = 4;
	public static final int BODY_TEMP = 5;
	public static final int AMBIENT_TEMP = 6;
	public static final int RIP = 7;
	public static final int ALCOHOL = 10;
	public static final int GSRWRIST=11;
	public static final int TEMPWRIST=12;

	public static final int ALCOHOL_ACCL_RIGHT_X = 13;
	public static final int ALCOHOL_ACCL_RIGHT_Y = 14;
	public static final int ALCOHOL_ACCL_RIGHT_Z = 15;

	public static final int ALCOHOL_ACCL_LEFT_X = 16;
	public static final int ALCOHOL_ACCL_LEFT_Y = 17;
	public static final int ALCOHOL_ACCL_LEFT_Z = 18;

	public static final int NINE_AXIS_RIGHT_ACCL_X = 19;
	public static final int NINE_AXIS_RIGHT_ACCL_Y = 20;
	public static final int NINE_AXIS_RIGHT_ACCL_Z = 21;
	public static final int NINE_AXIS_RIGHT_GYRO_X = 22;
	public static final int NINE_AXIS_RIGHT_GYRO_Y = 23;
	public static final int NINE_AXIS_RIGHT_GYRO_Z = 24;
	public static final int NINE_AXIS_RIGHT_NULL_PACKET = 25;

	public static final int NINE_AXIS_LEFT_ACCL_X = 26;
	public static final int NINE_AXIS_LEFT_ACCL_Y = 27;
	public static final int NINE_AXIS_LEFT_ACCL_Z = 28;
	public static final int NINE_AXIS_LEFT_GYRO_X = 29;
	public static final int NINE_AXIS_LEFT_GYRO_Y = 30;
	public static final int NINE_AXIS_LEFT_GYRO_Z = 31;
	public static final int NINE_AXIS_LEFT_NULL_PACKET = 32;

//
	public static final int MIN_AGREED_MOTE_CHANNEL = 0;
	public static final int MAX_AGREED_MOTE_CHANNEL = 10;
//
//	public static int mapMoteChannelToMoteSensor(int channel)
//	{
//		int map = -1;
//		if (channel >=MIN_AGREED_MOTE_CHANNEL && channel <= MAX_AGREED_MOTE_CHANNEL)
//			map = channel;
//		return map;
//	}
	public static int mapMoteChannelToPhoneSensor(int moteID, int channel)
	{
		int map = -1;

		if (AUTOSENSE_VERSION == 2) {
			switch(channel)
			{
			case(ECG):		map = Constants.SENSOR_ECK;
							break;
			case(GSR): 		map = Constants.SENSOR_GSR;
							break;
			case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
							break;
			case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
							break;
			case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
							break;
			case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
	 								break;
			case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
					 				break;
			case(RIP):		map = Constants.SENSOR_RIP;
	 						break;
			case(ALCOHOL):		map = Constants.SENSOR_ALCOHOL;
				break;
			case(GSRWRIST):		map = Constants.SENSOR_GSRWRIST;
			break;
			case(TEMPWRIST):		map = Constants.SENSOR_TEMPWRIST;
			break;

			default:		break;
			}
		}
		else {
			if (moteID % 10 == 1) {
				switch(channel)
				{
				case(ECG):		map = Constants.SENSOR_ECK;
								break;
				case(GSR): 		map = Constants.SENSOR_GSR;
								break;
				case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
								break;
				case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
								break;
				case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
								break;
				case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
		 								break;
				case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
						 				break;
				case(RIP):		map = Constants.SENSOR_RIP;
		 						break;
				default:		break;
				}
			}
			else {
				map = Constants.SENSOR_RIP;   // Allows backwards compatibility with AutoSense hardware v1, 											  // which had a separate mote for respiration whose moteID % 10 == 2
			}
		}

//		if (moteID == 1){
//			map = Constants.SENSOR_RIP;
//		}
//		else if (moteID % 10 == 1) {
//		switch(channel)
//		{
//		case(ECG):		map = Constants.SENSOR_ECK;
//						break;
//		case(GSR): 		map = Constants.SENSOR_GSR;
//						break;
//		case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
//						break;
//		case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
//						break;
//		case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
//						break;
//		case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
// 		break;
//		case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
//				 		break;
//		default:		break;
//		}
//	}

		return map;
	}

	public static int getMappingRuleBase()
	{
		int mappingRuleBase = -1;
		if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_1)
		{
			mappingRuleBase = 10;
		}
		else if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_2)
		{
			mappingRuleBase = 1;
		}
		return mappingRuleBase;
	}

	public static int mappingRule(int MoteType)
	{
		int rule = -1;

		switch(MoteType)
		{
		case Constants.MOTE_TYPE_AUTOSENSE_1_ECG:
		{
			rule = 1;
			break;
		}

		case Constants.MOTE_TYPE_AUTOSENSE_1_RIP:
		{
			rule = 2;
			break;
		}

		case Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP:
		{
			rule = 0;
			break;
		}

		case Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL:
		{
			rule = 0;
			break;
		}

		}

		return rule;
	}

	public static int getMoteTypeAutosense2(int chanID)
	{
		int moteType = -1;
		// In AutoSense 2 the mote types
		// all have the same ids
		// the channel number are different
		/*
		if(chanID == 10 || chanID == 11 || chanID == 12 )
		{
			moteType = Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL;
		}
		else
		{
			moteType = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
		}*/
		switch(chanID) {
		case ChannelToSensorMapping.ECG:
		case ChannelToSensorMapping.ACCELX:
		case ChannelToSensorMapping.ACCELY:
		case ChannelToSensorMapping.ACCELZ:
		case ChannelToSensorMapping.GSR:
		case ChannelToSensorMapping.BODY_TEMP:
		case ChannelToSensorMapping.AMBIENT_TEMP:
		case ChannelToSensorMapping.RIP:
			moteType = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
			break;
		case ChannelToSensorMapping.ALCOHOL:
		case ChannelToSensorMapping.GSRWRIST:
		case ChannelToSensorMapping.TEMPWRIST:
			moteType = Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL;
			break;
		case ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_X:
		case ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Y:
		case ChannelToSensorMapping.NINE_AXIS_RIGHT_ACCL_Z:
		case ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_X:
		case ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_Y:
		case ChannelToSensorMapping.NINE_AXIS_RIGHT_GYRO_Z:
		case ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_X:
		case ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Y:
		case ChannelToSensorMapping.NINE_AXIS_LEFT_ACCL_Z:
		case ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_X:
		case ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_Y:
		case ChannelToSensorMapping.NINE_AXIS_LEFT_GYRO_Z:
			moteType = Constants.MOTE_TYPE_AUTOSENSE_2_NINE_AXIS;
			break;
		}
		return moteType;
	}

	public static int[][] getChannelToSensorMap(int moteType)
	{
		int[][] sensormap = null;
		switch(moteType)
		{
			case Constants.MOTE_TYPE_AUTOSENSE_1_ECG:
			{
				sensormap = new int[Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_ECG][2];

				// ecg
				sensormap[0][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_ECG;
				sensormap[0][1] = Constants.SENSOR_ECK;

				// accel x
				sensormap[1][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_ACCEL_X;
				sensormap[1][1] = Constants.SENSOR_ACCELCHESTX;

				// accel y
				sensormap[2][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_ACCEL_Y;
				sensormap[2][1] = Constants.SENSOR_ACCELCHESTY;

				// accel z
				sensormap[3][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_ACCEL_Z;
				sensormap[3][1] = Constants.SENSOR_ACCELCHESTZ;

				// body temp
				sensormap[4][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_TEMP_BODY;
				sensormap[4][1] = Constants.SENSOR_BODY_TEMP;

				// ambient temp
				sensormap[5][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_TEMP_AMBIENT;
				sensormap[5][1] = Constants.SENSOR_AMBIENT_TEMP;

				// gsr
				sensormap[6][0] = Constants.CHANNEL_AUTOSENSE_1_ECG_GSR;
				sensormap[6][1] = Constants.SENSOR_GSR;

				break;
			}

			case Constants.MOTE_TYPE_AUTOSENSE_1_RIP :
			{
				sensormap = new int[Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_1_RIP][2];

				// rip
				sensormap[0][0] = Constants.CHANNEL_AUTOSENSE_1_RIP_RIP;
				sensormap[0][1] = Constants.SENSOR_RIP;

				break;
			}

			case Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP :
			{
				sensormap = new int[Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ECG_RIP][2];

				// ecg
				// ecg
				sensormap[0][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_ECG;
				sensormap[0][1] = Constants.SENSOR_ECK;

				// accel x
				sensormap[1][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_ACCEL_X;
				sensormap[1][1] = Constants.SENSOR_ACCELCHESTX;

				// accel y
				sensormap[2][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_ACCEL_Y;
				sensormap[2][1] = Constants.SENSOR_ACCELCHESTY;

				// accel z
				sensormap[3][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_ACCEL_Z;
				sensormap[3][1] = Constants.SENSOR_ACCELCHESTZ;

				// body temp
				sensormap[4][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_TEMP_BODY;
				sensormap[4][1] = Constants.SENSOR_BODY_TEMP;

				// ambient temp
				sensormap[5][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_TEMP_AMBIENT;
				sensormap[5][1] = Constants.SENSOR_AMBIENT_TEMP;

				// gsr
				sensormap[6][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_GSR;
				sensormap[6][1] = Constants.SENSOR_GSR;

				//rip
				sensormap[7][0] = Constants.CHANNEL_AUTOSENSE_2_ECG_RIP;
				sensormap[7][1] = Constants.SENSOR_RIP;

				// sensormap[7][0]

				break;

			}
			case Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL:
			{
				sensormap = new int[Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_ALCOHOL][2];

				// alcohol
				sensormap[0][0] = Constants.CHANNEL_AUTOSENSE_2_ALCOHOL_ALCOHOL;
				sensormap[0][1] = Constants.SENSOR_ALCOHOL;

				//gsrwrist
				sensormap[1][0] = Constants.CHANNEL_AUTOSENSE_2_ALCOHOL_GSR;
				sensormap[1][1] = Constants.SENSOR_GSRWRIST;

				//tempwrist
				sensormap[2][0] = Constants.CHANNEL_AUTOSENSE_2_ALCOHOL_TEMPERATURE;
				sensormap[2][1] = Constants.SENSOR_TEMPWRIST;

				break;

			}

			case Constants.MOTE_TYPE_AUTOSENSE_2_NINE_AXIS:
			{
				sensormap = new int[Constants.NO_OF_MOTE_SENSORS_AUTOSENSE_2_NINE_AXIS][2];

				sensormap[0][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_ACCL_X;
				sensormap[0][1] = Constants.SENSOR_NINE_AXIS_RIGHT_ACCL_X;

				sensormap[1][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_ACCL_Y;
				sensormap[1][1] = Constants.SENSOR_NINE_AXIS_RIGHT_ACCL_Y;

				sensormap[2][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_ACCL_Z;
				sensormap[2][1] = Constants.SENSOR_NINE_AXIS_RIGHT_ACCL_Z;

				sensormap[3][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_GYRO_X;
				sensormap[3][1] = Constants.SENSOR_NINE_AXIS_RIGHT_GYRO_X;

				sensormap[4][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_GYRO_Y;
				sensormap[4][1] = Constants.SENSOR_NINE_AXIS_RIGHT_GYRO_Y;

				sensormap[5][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_GYRO_Z;
				sensormap[5][1] = Constants.SENSOR_NINE_AXIS_RIGHT_GYRO_Z;

				sensormap[6][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_NULL_PACKET;
				sensormap[6][1] = Constants.SENSOR_NINE_AXIS_RIGHT_NULL_PACKET;


				sensormap[7][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_ACCL_X;
				sensormap[7][1] = Constants.SENSOR_NINE_AXIS_LEFT_ACCL_X;

				sensormap[8][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_ACCL_Y;
				sensormap[8][1] = Constants.SENSOR_NINE_AXIS_LEFT_ACCL_Y;

				sensormap[9][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_ACCL_Z;
				sensormap[9][1] = Constants.SENSOR_NINE_AXIS_LEFT_ACCL_Z;

				sensormap[10][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_GYRO_X;
				sensormap[10][1] = Constants.SENSOR_NINE_AXIS_LEFT_GYRO_X;

				sensormap[11][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_GYRO_Y;
				sensormap[11][1] = Constants.SENSOR_NINE_AXIS_LEFT_GYRO_Y;

				sensormap[12][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_GYRO_Z;
				sensormap[12][1] = Constants.SENSOR_NINE_AXIS_LEFT_GYRO_Z;

				sensormap[13][0] = Constants.CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_NULL_PACKET;
				sensormap[13][1] = Constants.SENSOR_NINE_AXIS_LEFT_NULL_PACKET;

				break;

			}
		}
		return sensormap;
	}

	@SuppressWarnings("all")
	public static int getSensorToMoteTypeMap(int SensorID)
	{
		int motetype = -1;

		if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_2)
		{
			switch(SensorID)
			{
			case Constants.SENSOR_ECK:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_RIP:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_ACCELCHESTX:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_ACCELCHESTY:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_ACCELCHESTZ:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_GSR:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_AMBIENT_TEMP:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;

			case Constants.SENSOR_BODY_TEMP:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ECG_RIP;
				break;


			case Constants.SENSOR_ALCOHOL:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL;
				break;

			case Constants.SENSOR_GSRWRIST:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL;
				break;

			case Constants.SENSOR_TEMPWRIST:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_2_ALCOHOL;
				break;


			}
		}
		else if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_1)
		{
			switch(SensorID)
			{
			case Constants.SENSOR_ECK:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			case Constants.SENSOR_RIP:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_RIP;
				break;

			case Constants.SENSOR_ACCELCHESTX:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			case Constants.SENSOR_ACCELCHESTY:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			case Constants.SENSOR_ACCELCHESTZ:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			case Constants.SENSOR_GSR:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			case Constants.SENSOR_AMBIENT_TEMP:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			case Constants.SENSOR_BODY_TEMP:
				motetype = Constants.MOTE_TYPE_AUTOSENSE_1_ECG;
				break;

			}
		}

		return motetype;
	}
//	public static int mapMoteChannelToPhoneSensor(int moteID, int channel)
//	{
//		int map = -1;
//
//		if (AUTOSENSE_VERSION == 2) {
//			switch(channel)
//			{
//			case(ECG):		map = Constants.SENSOR_ECK;
//							break;
//			case(GSR): 		map = Constants.SENSOR_GSR;
//							break;
//			case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
//							break;
//			case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
//							break;
//			case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
//							break;
//			case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
//	 								break;
//			case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
//					 				break;
//			case(RIP):		map = Constants.SENSOR_RIP;
//	 						break;
//			case(ALCOHOL):		map = Constants.SENSOR_ALCOHOL;
//				break;
//			default:		break;
//			}
//		}
//		else {
//			if (moteID % 10 == 1) {
//				switch(channel)
//				{
//				case(ECG):		map = Constants.SENSOR_ECK;
//								break;
//				case(GSR): 		map = Constants.SENSOR_GSR;
//								break;
//				case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
//								break;
//				case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
//								break;
//				case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
//								break;
//				case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
//		 								break;
//				case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
//						 				break;
//				case(RIP):		map = Constants.SENSOR_RIP;
//		 						break;
//				default:		break;
//				}
//			}
//			else {
//				map = Constants.SENSOR_RIP;   // Allows backwards compatibility with AutoSense hardware v1, 											  // which had a separate mote for respiration whose moteID % 10 == 2
//			}
//		}
//
////		if (moteID == 1){
////			map = Constants.SENSOR_RIP;
////		}
////		else if (moteID % 10 == 1) {
////		switch(channel)
////		{
////		case(ECG):		map = Constants.SENSOR_ECK;
////						break;
////		case(GSR): 		map = Constants.SENSOR_GSR;
////						break;
////		case(ACCELX):	map = Constants.SENSOR_ACCELCHESTX;
////						break;
////		case(ACCELY):	map = Constants.SENSOR_ACCELCHESTY;
////						break;
////		case(ACCELZ):	map = Constants.SENSOR_ACCELCHESTZ;
////						break;
////		case(BODY_TEMP):		map = Constants.SENSOR_BODY_TEMP;
//// 		break;
////		case(AMBIENT_TEMP):		map = Constants.SENSOR_AMBIENT_TEMP;
////				 		break;
////		default:		break;
////		}
////	}
//
//		return map;
//	}

	public static int mapPhoneSensorToMoteChannel(int SensorID)
	{
		int map = -1;

		if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_1)
		{
			switch(SensorID)
			{
			case(Constants.SENSOR_ACCELCHESTX): 	map = Constants.CHANNEL_AUTOSENSE_1_ECG_ACCEL_X;
													break;

			case(Constants.SENSOR_ACCELCHESTY):		map = Constants.CHANNEL_AUTOSENSE_1_ECG_ACCEL_Y;
													break;

			case(Constants.SENSOR_ACCELCHESTZ):		map = Constants.CHANNEL_AUTOSENSE_1_ECG_ACCEL_Z;
													break;

			case(Constants.SENSOR_ECK): 			map = Constants.CHANNEL_AUTOSENSE_1_ECG_ECG;
													break;

			case(Constants.SENSOR_GSR):				map = Constants.CHANNEL_AUTOSENSE_1_ECG_GSR;
													break;

			case(Constants.SENSOR_RIP):				map = Constants.CHANNEL_AUTOSENSE_1_RIP_RIP;
													break;

			case(Constants.SENSOR_AMBIENT_TEMP):	map = Constants.CHANNEL_AUTOSENSE_1_ECG_TEMP_AMBIENT;
													break;

			case(Constants.SENSOR_BODY_TEMP):		map = Constants.CHANNEL_AUTOSENSE_1_ECG_TEMP_BODY;
													break;

			}
		}
		else if(Constants.CURRENT_SENSOR_SUITE == Constants.SENSOR_SUITE_AUTOSENSE_2)
		{
			switch(SensorID)
			{
			case(Constants.SENSOR_ACCELCHESTX): 	map = Constants.CHANNEL_AUTOSENSE_2_ECG_ACCEL_X;
													break;

			case(Constants.SENSOR_ACCELCHESTY):		map = Constants.CHANNEL_AUTOSENSE_2_ECG_ACCEL_Y;
													break;

			case(Constants.SENSOR_ACCELCHESTZ):		map = Constants.CHANNEL_AUTOSENSE_2_ECG_ACCEL_Z;
													break;

			case(Constants.SENSOR_ECK): 			map = Constants.CHANNEL_AUTOSENSE_2_ECG_ECG;
													break;

			case(Constants.SENSOR_GSR):				map = Constants.CHANNEL_AUTOSENSE_2_ECG_GSR;
													break;

			case(Constants.SENSOR_RIP):				map = Constants.CHANNEL_AUTOSENSE_2_ECG_RIP;
													break;

			case(Constants.SENSOR_AMBIENT_TEMP):	map = Constants.CHANNEL_AUTOSENSE_2_ECG_TEMP_AMBIENT;
													break;

			case(Constants.SENSOR_BODY_TEMP):		map = Constants.CHANNEL_AUTOSENSE_2_ECG_TEMP_BODY;
													break;

			case(Constants.SENSOR_ALCOHOL):			map = Constants.CHANNEL_AUTOSENSE_2_ALCOHOL_ALCOHOL;
													break;

			case(Constants.SENSOR_GSRWRIST):		map = Constants.CHANNEL_AUTOSENSE_2_ALCOHOL_GSR;
													break;

			case(Constants.SENSOR_TEMPWRIST):		map = Constants.CHANNEL_AUTOSENSE_2_ALCOHOL_TEMPERATURE;
													break;

			}


		}

		return map;
	}

}
