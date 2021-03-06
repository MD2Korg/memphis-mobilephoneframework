﻿//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
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

// @author Mishfaq Ahmed
// @author Patrick Blitz
// @author Monowar Hossain
// @author Somnath Mitra
// @author Kurt Plarre
// @author Mahbub Rahman
// @author Andrew Raij


package org.fieldstream;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

public class Constants {
	/**
	 * the subject used to identify the correct personalization attributes!
	 */
	public static String SUBJECT = "generic";

	public static final int MOTE_NO=6;

	public static final int MOTE_RIPECG_IND=0;
	public static final int MOTE_ALCOHOL_IND=1;
	public static final int MOTE_ALCOHOL_ACCL_RIGHT_IND=2;
	public static final int MOTE_ALCOHOL_ACCL_LEFT_IND=3;
	public static final int MOTE_NINE_AXIS_RIGHT_IND=4;
	public static final int MOTE_NINE_AXIS_LEFT_IND=5;

	public static final int MOTE_RIPECG=0;
	public static final int MOTE_ALCOHOL=1;
	public static final int MOTE_ALCOHOL_ACCL_RIGHT=2;
	public static final int MOTE_ALCOHOL_ACCL_LEFT=3;
	public static final int MOTE_9_AXIS_RIGHT=4;
	public static final int MOTE_9_AXIS_LEFT=5;

	public static String antAddress[]=new String[MOTE_NO];
	public static boolean moteActive[]=new boolean[MOTE_NO];

	/**
	 * application debug status, writes a trace to file!
	 */
	public final static boolean WRITETRACE=false;

	public final static boolean BUZZ=true;

	public final static boolean NETWORK_LOGGING=true;

	public final static boolean GAM_GMS_LOGGING=false;

	public final static boolean GPS_LOGGING=true;

	/**
	 * file that is used to read and write the configuration
	 */
	public final static String APP_DIR="FieldStream";
	public static final String LOG_DIR = APP_DIR + "/logs";
	public static final String CONFIG_DIR = APP_DIR + "/config";
	public final static String PARTICIPANT_DIR=APP_DIR+"/participant";
	public static String EMA_QUESTION_FILENAME[];// = "questions.xml";
	public static int INDEX_CURRENT_EMA_QUESTION_FILENAME;
	public static int INDEX_DEFAULT_EMA_QUESTION_FILENAME;

//	public static final int EMA_QUESTION_CONFIG_FILENAME_NO=0;// = "questions.xml";

	public static final String EMA_CONFIG_FILENAME = "ema_config.xml";
	public static final String NETWORK_CONFIG_FILENAME = "network.xml";
	public static final String DEAD_PERIOD_CONFIG_FILENAME = "dead_periods.xml";
	public static final String ALARM_CONFIG_FILENAME = "alarm_config.xml";
	public static final String FIELDINFO_CONFIG_FILENAME = "fieldinfo.xml";
	public static final String SELFREPORT_CONFIG_FILENAME = "selfreport.xml";

	public static final String ACTIVATION_CONFIG_FILENAME = "activation.xml";
	public static final String LABSTUDY_CONFIG_FILENAME = "labstudy.xml";
	public static final String PARTICIPANT_FILENAME="participant.db";
	public static final int SENSOR_GSR = 11;
	public static final int SENSOR_ECK = 12;
	public static final int SENSOR_BODY_TEMP = 13;
	public static final int SENSOR_AMBIENT_TEMP = 14;
	public static final int SENSOR_ACCELPHONEX = 30;
	public static final int SENSOR_ACCELPHONEY = 31;
	public static final int SENSOR_ACCELPHONEZ = 32;
	public static final int SENSOR_ACCELPHONEMAG = 33;
	public static final int SENSOR_COMPASSPHONEX = 34;
	public static final int SENSOR_COMPASSPHONEY = 35;
	public static final int SENSOR_COMPASSPHONEZ = 36;
	public static final int SENSOR_COMPASSPHONEMAG = 37;
	public static final int SENSOR_ACCELCHESTMAG = 41;
	public static final int SENSOR_ACCELCHESTX = 18;
	public static final int SENSOR_ACCELCHESTY = 19;
	public static final int SENSOR_ACCELCHESTZ = 20;
	public static final int SENSOR_RIP = 21;
	public static final int SENSOR_VIRTUAL_RR = 22;
	public static final int SENSOR_LOCATIONLATITUDE = 23;
	public static final int SENSOR_LOCATIONLONGITUDE = 24;
	public static final int SENSOR_LOCATIONSPEED = 25;
	public static final int SENSOR_BATTERY_LEVEL = 26;
	public static final int SENSOR_ALCOHOL = 27;
	public static final int SENSOR_GSRWRIST = 28;
	public static final int SENSOR_TEMPWRIST = 29;

	public static final int SENSOR_NINE_AXIS_RIGHT_ACCL_X = 101;
	public static final int SENSOR_NINE_AXIS_RIGHT_ACCL_Y = 102;
	public static final int SENSOR_NINE_AXIS_RIGHT_ACCL_Z = 103;
	public static final int SENSOR_NINE_AXIS_RIGHT_GYRO_X = 104;
	public static final int SENSOR_NINE_AXIS_RIGHT_GYRO_Y = 105;
	public static final int SENSOR_NINE_AXIS_RIGHT_GYRO_Z = 106;
	public static final int SENSOR_NINE_AXIS_RIGHT_NULL_PACKET = 107;

	public static final int SENSOR_NINE_AXIS_LEFT_ACCL_X = 108;
	public static final int SENSOR_NINE_AXIS_LEFT_ACCL_Y = 109;
	public static final int SENSOR_NINE_AXIS_LEFT_ACCL_Z = 110;
	public static final int SENSOR_NINE_AXIS_LEFT_GYRO_X = 111;
	public static final int SENSOR_NINE_AXIS_LEFT_GYRO_Y = 112;
	public static final int SENSOR_NINE_AXIS_LEFT_GYRO_Z = 113;
	public static final int SENSOR_NINE_AXIS_LEFT_NULL_PACKET = 114;


	public static final int SENSOR_REPLAY_GSR = 91;
	public static final int SENSOR_REPLAY_ECK = 92;
	public static final int SENSOR_REPLAY_TEMP = 93;
	public static final int SENSOR_REPLAY_RESP = 94;		//this is actually SENSOR_VIRTUAL_PEAKVALLEY
	//added by mahbub for conversation from respiration
	public static final int SENSOR_VIRTUAL_INHALATION = 95;
	public static final int SENSOR_VIRTUAL_EXHALATION = 96;
	public static final int SENSOR_VIRTUAL_IERATIO = 97;
	public static final int SENSOR_VIRTUAL_REALPEAKVALLEY = 98;
	public static final int SENSOR_VIRTUAL_STRETCH = 99;
	public static final int SENSOR_VIRTUAL_BDURATION = 90;
	public static final int SENSOR_VIRTUAL_EXHALATION_FIRSTDIFF = 89;
	public static final int SENSOR_VIRTUAL_RESPIRATION = 88;
	public static final int SENSOR_VIRTUAL_MINUTEVENTILATION = 87;

	public static final int SENSOR_ZEPHYR_ECG = 41;
	public static final int SENSOR_ZEPHYR_RSP = 42;
	public static final int SENSOR_ZEPHYR_TMP = 43;
	public static final int SENSOR_ZEPHYR_ACL = 44;
	public static final int SENSOR_VIRTUAL_ECK_QUALITY = 45;
	public static final int SENSOR_VIRTUAL_RIP_QUALITY = 46;
	public static final int SENSOR_VIRTUAL_TEMP_QUALITY = 47;
	public static final int SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW = 50;
	public static final int SENSOR_VIRTUAL_ACCELCOMMUTING = 48;

	// ema constants
	public static final String quietStart = "QUIET_START";
	public static final String quietEnd = "QUIET_STOP";
	public static final String sleepStart = "SLEEP_START";
	public static final String sleepEnd = "SLEEP_END";
	public static final String dayStart = "DAY_START";
	public static final String dayEnd = "DAY_END";

//	public static long FIRSTDAYSTART=0;
	public static final long DAYMILLIS=24L*60L*60L*1000L;
	// motetypes
	public static final int MOTE_TYPE_AUTOSENSE_1_ECG = 201;
	public static final int MOTE_TYPE_AUTOSENSE_1_RIP = 202;
	public static final int MOTE_TYPE_AUTOSENSE_1_BRIDGE = 203;

	public static final int MOTE_TYPE_AUTOSENSE_2_ECG_RIP = 204;
	public static final int MOTE_TYPE_AUTOSENSE_2_ALCOHOL = 205;
	public static final int MOTE_TYPE_AUTOSENSE_2_BRIDGE = 206;
	public static final int MOTE_TYPE_AUTOSENSE_2_NINE_AXIS = 207;

	// mote connection types
	public static final int MOTE_CONNECTION_BRIDGE = 301;
	public static final int MOTE_CONNECTION_BLUETOOTH = 302;
	public static final int MOTE_CONNECTION_USB = 303;

	//number of sensors in each versions of the mote
	public static final int NO_OF_MOTE_SENSORS_AUTOSENSE_1_ECG = 7;
	public static final int NO_OF_MOTE_SENSORS_AUTOSENSE_1_RIP = 1;

	public static final int NO_OF_MOTE_SENSORS_AUTOSENSE_2_ECG_RIP = 8;
	public static final int NO_OF_MOTE_SENSORS_AUTOSENSE_2_ALCOHOL = 3;
	public static final int NO_OF_MOTE_SENSORS_AUTOSENSE_2_NINE_AXIS = 14;

	// sensor channel numbers coming from mote for each sensor
	// autosense 1
	public static final int CHANNEL_AUTOSENSE_1_ECG_ECG = 0;
	public static final int CHANNEL_AUTOSENSE_1_ECG_ACCEL_X = 1;
	public static final int CHANNEL_AUTOSENSE_1_ECG_ACCEL_Y = 2;
	public static final int CHANNEL_AUTOSENSE_1_ECG_ACCEL_Z = 3;
	public static final int CHANNEL_AUTOSENSE_1_ECG_TEMP_BODY = 4;
	public static final int CHANNEL_AUTOSENSE_1_ECG_TEMP_AMBIENT = 5;
	public static final int CHANNEL_AUTOSENSE_1_ECG_GSR = 6;

	public static final int CHANNEL_AUTOSENSE_1_RIP_RIP = 0;

	//autosense 2
	public static final int CHANNEL_AUTOSENSE_2_ECG_ECG = 0;
	public static final int CHANNEL_AUTOSENSE_2_ECG_ACCEL_X = 1;
	public static final int CHANNEL_AUTOSENSE_2_ECG_ACCEL_Y = 2;
	public static final int CHANNEL_AUTOSENSE_2_ECG_ACCEL_Z = 3;
	public static final int CHANNEL_AUTOSENSE_2_ECG_TEMP_BODY = 4;
	public static final int CHANNEL_AUTOSENSE_2_ECG_TEMP_AMBIENT = 5;
	public static final int CHANNEL_AUTOSENSE_2_ECG_GSR = 6;
	public static final int CHANNEL_AUTOSENSE_2_ECG_RIP = 7;

	public static final int CHANNEL_AUTOSENSE_2_ALCOHOL_ALCOHOL = 10;
	public static final int CHANNEL_AUTOSENSE_2_ALCOHOL_GSR = 11;
	public static final int CHANNEL_AUTOSENSE_2_ALCOHOL_TEMPERATURE = 12;

	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_ACCL_X = 19;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_ACCL_Y = 20;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_ACCL_Z = 21;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_GYRO_X = 22;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_GYRO_Y = 23;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_GYRO_Z = 24;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_RIGHT_NULL_PACKET = 25;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_ACCL_X = 26;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_ACCL_Y = 27;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_ACCL_Z = 28;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_GYRO_X = 29;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_GYRO_Y = 30;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_GYRO_Z = 31;
	public static final int CHANNEL_AUTOSENSE_2_NINE_AXIS_LEFT_NULL_PACKET = 32;

	// the current sensor suite that is being used
	public static final int SENSOR_SUITE_AUTOSENSE_1 = 1;
	public static final int SENSOR_SUITE_AUTOSENSE_2 = 2;

	public static int CURRENT_SENSOR_SUITE = SENSOR_SUITE_AUTOSENSE_2;

	public static final float MISSINGRATETHRESHOLD=0.8f;  //for RIP data

	public static final boolean ENABLE_OSCILLOSCOPE_RENDERING_OPTIMIZATION = true;

	private final static HashMap<Integer, String> moteDescriptions = new HashMap<Integer, String>() {
		{
		put(MOTE_TYPE_AUTOSENSE_1_ECG, "Autosense Version 1 ECG Mote");
		put(MOTE_TYPE_AUTOSENSE_1_RIP,  "Autosense Version 1 RIP Mote");
		put(MOTE_TYPE_AUTOSENSE_1_BRIDGE,  "Autosense Version 1 BRIDGE Mote");
		put(MOTE_TYPE_AUTOSENSE_2_ECG_RIP,  "Autosense Version 2 ECG AND RIP Mote");
		put(MOTE_TYPE_AUTOSENSE_2_ALCOHOL,  "Autosense Version 2 ALCOHOL Mote");
		put(MOTE_TYPE_AUTOSENSE_2_BRIDGE,  "Autosense Version 2 BRIDGE Mote");
		}
	};

	private final static HashMap<Integer, String>  moteSensorDescriptions = new HashMap<Integer, String>() {
		{
		put(CHANNEL_AUTOSENSE_1_ECG_ECG, "Autosense Version 1 Mote Electrocardiogram");
		put(CHANNEL_AUTOSENSE_1_ECG_ACCEL_X, "Autosense Version 1 Accelerometer X");
		put(CHANNEL_AUTOSENSE_1_ECG_ACCEL_Y, "Autosense Version 1 Accelerometer Y");
		put(CHANNEL_AUTOSENSE_1_ECG_ACCEL_Z, "Autosense Version 1 Accelerometer Z");
		put(CHANNEL_AUTOSENSE_1_ECG_TEMP_BODY, "Autosense Version 1 Temperature Body");
		put(CHANNEL_AUTOSENSE_1_ECG_TEMP_AMBIENT, "Autosense Version 1 Temperature Ambient");
		put(CHANNEL_AUTOSENSE_1_ECG_GSR, "Autosense Version 1 Galvanic Skin Response");
		put(CHANNEL_AUTOSENSE_1_RIP_RIP,"Autosense Version 1 Respiration");
		}
	};

	private final static HashMap<Integer, String>  moteSensorAutoSense1Descriptions = new HashMap<Integer, String>() {
		{
		put(CHANNEL_AUTOSENSE_1_ECG_ECG, "Autosense Version 1 Mote Electrocardiogram");
		put(CHANNEL_AUTOSENSE_1_ECG_ACCEL_X, "Autosense Version 1 Accelerometer X");
		put(CHANNEL_AUTOSENSE_1_ECG_ACCEL_Y, "Autosense Version 1 Accelerometer Y");
		put(CHANNEL_AUTOSENSE_1_ECG_ACCEL_Z, "Autosense Version 1 Accelerometer Z");
		put(CHANNEL_AUTOSENSE_1_ECG_TEMP_BODY, "Autosense Version 1 Temperature Body");
		put(CHANNEL_AUTOSENSE_1_ECG_TEMP_AMBIENT, "Autosense Version 1 Temperature Ambient");
		put(CHANNEL_AUTOSENSE_1_ECG_GSR, "Autosense Version 1 Galvanic Skin Response");
		put(CHANNEL_AUTOSENSE_1_RIP_RIP,"Autosense Version 1 Respiration");
		}
	};

	private final static HashMap<Integer, String>  moteSensorAutoSense2Descriptions = new HashMap<Integer, String>() {
		{
		put(CHANNEL_AUTOSENSE_2_ECG_ECG, "Autosense Version 2 Mote Electrocardiogram");
		put(CHANNEL_AUTOSENSE_2_ECG_ACCEL_X, "Autosense Version 2 Accelerometer X");
		put(CHANNEL_AUTOSENSE_2_ECG_ACCEL_Y, "Autosense Version 2 Accelerometer Y");
		put(CHANNEL_AUTOSENSE_2_ECG_ACCEL_Z, "Autosense Version 2 Accelerometer Z");
		put(CHANNEL_AUTOSENSE_2_ECG_TEMP_BODY, "Autosense Version 2 Temperature Body");
		put(CHANNEL_AUTOSENSE_2_ECG_TEMP_AMBIENT, "Autosense Version 2 Temperature Ambient");
		put(CHANNEL_AUTOSENSE_2_ECG_GSR, "Autosense Version 2 Galvanic Skin Response");
		put(CHANNEL_AUTOSENSE_2_ECG_RIP,"Autosense Version 2 Respiration");
		put(CHANNEL_AUTOSENSE_2_ALCOHOL_ALCOHOL,"Autosense Version 2 Alcohol on Alcohol mote");
		put(CHANNEL_AUTOSENSE_2_ALCOHOL_GSR,"Autosense Version 2 GSR on Alcohol mote");
		put(CHANNEL_AUTOSENSE_2_ALCOHOL_TEMPERATURE, "Autosense Version 2 Temperature on Alcohol mote");
		}
	};


	private final static HashMap<Integer, String> sensorDescriptions = new HashMap<Integer, String>() {
		{
			put(SENSOR_GSR, "GSR on RIPECG mote");
			put(SENSOR_ECK, "Electrocardiogram");
			put(SENSOR_BODY_TEMP, "Body Temperature");
			put(SENSOR_AMBIENT_TEMP, "Ambient Temperature");
			put(SENSOR_ACCELPHONEX, "Phone Accel X value");
			put(SENSOR_ACCELPHONEY, "Phone Accel Y value");
			put(SENSOR_ACCELPHONEZ, "Phone Accel Z value");
			put(SENSOR_ACCELPHONEMAG, "Phone Accelerometer magnitude");

			put(SENSOR_COMPASSPHONEX,"Phone Compass X value");
			put(SENSOR_COMPASSPHONEY,"Phone Compass Y value");
			put(SENSOR_COMPASSPHONEZ,"Phone Compass Z value");
			put(SENSOR_COMPASSPHONEMAG,"Phone Compass Magnitude");

			put(SENSOR_ACCELCHESTMAG, "Chestband Accelerometer");
			put(SENSOR_ACCELCHESTX, "Chest Accl X value");
			put(SENSOR_ACCELCHESTY, "Chest Accl Y value");
			put(SENSOR_ACCELCHESTZ, "Chest Accl Z value");
			put(SENSOR_RIP, "Respiration");
			put(SENSOR_VIRTUAL_RR, "RR Intervals from Heart Rate Signal");
			put(SENSOR_REPLAY_GSR, "Galvanic Skin Response (Replay)");
			put(SENSOR_REPLAY_ECK, "Electrocardiogram (Replay)");
			put(SENSOR_REPLAY_TEMP, "Body Temperature (Replay)");
			put(SENSOR_REPLAY_RESP, "Respiration (Replay)");
			put(SENSOR_VIRTUAL_INHALATION, "Inhalation (virtual)");
			put(SENSOR_VIRTUAL_EXHALATION, "Exhalation (virtual)");
			put(SENSOR_VIRTUAL_RESPIRATION, "Respiration (virtual)");
			put(SENSOR_VIRTUAL_MINUTEVENTILATION,"Minute Ventilation(virtual)");
			put(SENSOR_VIRTUAL_IERATIO, "IEratio (virtual)");
			put(SENSOR_LOCATIONLATITUDE, "Latitude from GPS");
			put(SENSOR_LOCATIONLONGITUDE, "Longitude from GPS");
			put(SENSOR_LOCATIONSPEED, "Speed from GPS");
			put(SENSOR_BATTERY_LEVEL, "Changes in Battery Level");
			put(SENSOR_VIRTUAL_REALPEAKVALLEY, "Peaks and Valleys (virtual)");
			put(SENSOR_VIRTUAL_ECK_QUALITY, "ECK Quality (virtual)");
			put(SENSOR_VIRTUAL_RIP_QUALITY, "RIP Quality (virtual)");
			put(SENSOR_VIRTUAL_TEMP_QUALITY, "TEMP Quality (virtual)");
			put(SENSOR_VIRTUAL_STRETCH, "Stretch (virtual)");
			put(SENSOR_VIRTUAL_BDURATION, "Duration at the bottom of each stretch (virtual)");
			put(SENSOR_VIRTUAL_EXHALATION_FIRSTDIFF, "Virtual Sensor for Calculating First Difference of Exhalation");
			put(SENSOR_VIRTUAL_FIRSTDIFF_EXHALATION_NEW, "Virtual Sensor for Calculating First Difference of Exhalation");
			put(SENSOR_ALCOHOL, "Alcohol Consumption");
			put(SENSOR_GSRWRIST,"GSR On Alcohol Mote");
			put(SENSOR_TEMPWRIST,"Temperature on Alcohol Mote");
			put(SENSOR_VIRTUAL_ACCELCOMMUTING, "Virtual sensor for commuting detection using phone accelerometer");

			put(SENSOR_NINE_AXIS_RIGHT_ACCL_X, "Nine Right Accl X");
			put(SENSOR_NINE_AXIS_RIGHT_ACCL_Y, "Nine Right Accl Y");
			put(SENSOR_NINE_AXIS_RIGHT_ACCL_Z, "Nine Right Accl Z");
			put(SENSOR_NINE_AXIS_RIGHT_GYRO_X, "Nine Right Gyro X");
			put(SENSOR_NINE_AXIS_RIGHT_GYRO_Y, "Nine Right Gyro Y");
			put(SENSOR_NINE_AXIS_RIGHT_GYRO_Z, "Nine Right Gyro Z");
			put(SENSOR_NINE_AXIS_LEFT_ACCL_X, "Nine Left Accl X");
			put(SENSOR_NINE_AXIS_LEFT_ACCL_Y, "Nine Left Accl Y");
			put(SENSOR_NINE_AXIS_LEFT_ACCL_Z, "Nine Left Accl Z");
			put(SENSOR_NINE_AXIS_LEFT_GYRO_X, "Nine Left Gyro X");
			put(SENSOR_NINE_AXIS_LEFT_GYRO_Y, "Nine Left Gyro Y");
			put(SENSOR_NINE_AXIS_LEFT_GYRO_Z, "Nine Left Gyro Z");

		}
	};

	public static final int FEATURE_MAD = 111;
	public static final int FEATURE_MEAN = 112;
	public static final int FEATURE_RMS = 113;
	public static final int FEATURE_VAR = 114;
	public static final int FEATURE_SD = 117;
	public static final int FEATURE_MEDIAN = 118;
	public static final int FEATURE_MIN = 119;
	public static final int FEATURE_MAX = 109;

	public static final int FEATURE_NULL = 110;
	public static final int FEATURE_MEANCROSS = 115;
	public static final int FEATURE_ZEROCROSS = 116;
	public static final int FEATURE_HR = 120;
	public static final int FEATURE_HR_LF = 121;
	public static final int FEATURE_HR_RSA = 122;
	public static final int FEATURE_HR_RATIO = 123;
	public static final int FEATURE_HR_MF = 124;
	public static final int FEATURE_HR_POWER_01 = 125;
	public static final int FEATURE_HR_POWER_12 = 126;
	public static final int FEATURE_HR_POWER_23 = 127;
	public static final int FEATURE_HR_POWER_34 = 128;

	public static final int FEATURE_GSRA = 130;
	public static final int FEATURE_GSRD = 131;
	public static final int FEATURE_SRR = 132;
	public static final int FEATURE_SRA = 133;

	public static final int FEATURE_RESP_RATE = 140;
	public static final int FEATURE_RAMP = 141;
	public static final int FEATURE_RESP_SD = 142;

	public static final int FEATURE_SECOND_BEST=143;
	public static final int FEATURE_NINETIETH_PERCENTILE=144;
	public static final int FEATURE_QRDEV=145;
	public static final int FEATURE_PERCENTILE=146;
	public static final int FEATURE_PERCENTILE80=147;

	private final static HashMap<Integer, String> featureDescriptions = new HashMap<Integer, String>() {
		{
			put(FEATURE_MAD, "Mean Adjusted Deviation");
			put(FEATURE_MEAN, "Mean");
			put(FEATURE_RMS, "Root Mean Square");
			put(FEATURE_VAR, "Variance");
			put(FEATURE_SD, "Standard Deviation");
			put(FEATURE_MIN, "Minimum");
			put(FEATURE_MAX, "Maximum");
			put(FEATURE_NULL, "Null");
			put(FEATURE_MEANCROSS, "Mean Crossing Rate");
			put(FEATURE_ZEROCROSS, "Zero Crossing Rate");
			put(FEATURE_HR, "Heart Rate");
			put(FEATURE_HR_LF, "EKG - Integration over the power of the LF Band");
			put(FEATURE_HR_RSA, "EKG - Respiratory sinus arrhythmia (RSA)");
			put(FEATURE_HR_RATIO, "EKG - Ratio between sympathetic / parasympathetic influences");
			put(FEATURE_HR_MF, "EKG - Integration over the power of the MF Band");
			put(FEATURE_HR_POWER_01, "EKG - Integration over the power of the 0.0-0.1 Band");
			put(FEATURE_HR_POWER_12, "EKG - Integration over the power of the 0.1-0.2 Band");
			put(FEATURE_HR_POWER_23, "EKG - Integration over the power of the 0.2-0.3 Band");
			put(FEATURE_HR_POWER_34, "EKG - Integration over the power of the 0.3-0.4 Band");
			put(FEATURE_GSRA, "GSR - Response Area");
			put(FEATURE_GSRD, "GSR - Response Duration");
			put(FEATURE_SRR, "GSR - Rate of nonspecific skin conductance responses");
			put(FEATURE_SRA, "GSR - Skin conductance response amplitude");
			put(FEATURE_RESP_RATE, "Respiration Rate");
			put(FEATURE_RAMP, "Respiration Amplitude");
			put(FEATURE_RESP_SD, "Respiration Standard Deviation");
			put(FEATURE_MEDIAN, "Median");
			put(FEATURE_SECOND_BEST,"Second best value");
			put(FEATURE_NINETIETH_PERCENTILE,"90th percentile");
			put(FEATURE_QRDEV, "Quartile Deviation");
			put(FEATURE_PERCENTILE, "Q'th Percentile");
			put(FEATURE_PERCENTILE80,"80th percentile");

		}
	};

	public static final int MODEL_ACTIVITY = 1;
	public static final int MODEL_STRESS = 2;
	public static final int MODEL_TEST = 0;
	public static final int MODEL_DATAQUALITY = 4;
	public static final int MODEL_CONVERSATION=5;
	public static final int MODEL_GPSCOMMUTING=6;
	public static final int MODEL_STRESS_OLD = 7;
	public static final int MODEL_ACCUMULATION = 8;
	public static final int MODEL_ACCELCOMMUTING = 9;

// New model added for user self report

	public static final int MODEL_SELF_DRINKING = 200;
	public static final int MODEL_SELF_SMOKING = 201;
	public static final int MODEL_SELF_CRAVING = 202;
	public static final int MODEL_SELF_STRESS = 203;
	public static final int MODEL_SELF_CONVERSATION = 204;
	public static final int MODEL_COLLECT_SALIVA=205;

	public static final int MODEL_SELF_START=200;
	public static final int MODEL_SELF_END=300;

	private static HashMap<String,Integer> modelToIndex = new HashMap<String, Integer>() {
		{
			put("MODEL_SELF_DRINKING",MODEL_SELF_DRINKING);
			put("MODEL_SELF_SMOKING",MODEL_SELF_SMOKING);
			put("MODEL_SELF_CRAVING",MODEL_SELF_CRAVING);
			put("MODEL_SELF_STRESS",MODEL_SELF_STRESS);
			put("MODEL_SELF_CONVERSATION",MODEL_SELF_CONVERSATION);
			put("MODEL_COLLECT_SALIVA",MODEL_COLLECT_SALIVA);
		}
	};

	private static HashMap<Integer, String> modelDescriptions = new HashMap<Integer, String>() {
		{
			put(MODEL_ACTIVITY, "Physical Activity and Posture");
			put(MODEL_STRESS_OLD, "Stress (Intensity of Negative Affect)");
			put(MODEL_TEST, "NULL");
			put(MODEL_DATAQUALITY, "Data Quality Assessments");
			put(MODEL_CONVERSATION,"Detect Conversation Based on Respiration Signal");
			put(MODEL_GPSCOMMUTING,"Commuting detection from GPS");
			put(MODEL_STRESS, "Minute Classifier of Stress");
			put(MODEL_SELF_DRINKING,"User self reported drinking event recording");
			put(MODEL_SELF_SMOKING,"User self reported smoking event recording");
			put(MODEL_SELF_CRAVING,"User self reported craving event recording");
			put(MODEL_SELF_STRESS,"User self reported stress event recording");
			put(MODEL_SELF_CONVERSATION,"User self reported conversation event recording");

			put(MODEL_COLLECT_SALIVA, "Collect Saliva by the user");

			put(MODEL_ACCUMULATION,"Accumulation and Decay ofs Perceived Stress");
			put(MODEL_ACCELCOMMUTING,"Commuting detection from phone accelerometer");
		}
	};
/*	public static void add_model(int key, String value)
	{
		modelDescriptions.put(key, value);
	}

*/
	public static final int ACCELEROMETER_LOCATION = 0;
	public static final String DATALOG_FILENAME = "StressInferencePhone.db";
	public static final int COMPASS_LOCATION = 1;

	/**
	 * Log to Database (true) or to Flat file.
	 */
	public static final boolean LOGTODB = false;

	// everything listed in here will NOT be logged.  Make sure to put full feature-sensor combinations.
	public final static HashSet<Integer> DATALOG_FILTER = new HashSet<Integer>() {
		{
			// add(MODEL_NULL);
			// add(Constants.getId(FEATURE_NULL, SENSOR_NULL));
			// add(SENSOR_NULL);
		}
	};


	public static boolean isSensor(int id) {
		return (id > 9 && id <= 114);
	}

	public static boolean isFeatureSensor(int id) {
		return (id >= 10000);
	}

/*	public static boolean isModel(int id) {
		return (id <= 9);
	}
*/
	/**
	 * return a unique ID for each feature-sensor configuration.
	 * This allows us to use a single feature calculation on different sensors without reinstanciating or rewritting anything
	 * <br/>
	 * Really it just takes the feature ID (3 digits), multiplies it by 100, and adds the Sensorid (below 99) to it
	 *
	 * @param feature the unique ID of this feature
	 * @param sensor the unique ID of a sensor as defined in this class
	 * @return
	 */
	public static int getId(int feature, int sensor)
	{
		return feature*100+sensor;
	}


	/**
	 * Return the Feature ID from a number constructed by the above defined combined ID (see {@link #getId(int, int)});
	 * @param featureSensorID
	 * @return the feature ID
	 */

	public static int parseFeatureId(int featureSensorID) {
		return (int)(featureSensorID/100);
	}

	/**
	 * Return the Sensor ID from a number constructed by the above defined combined ID (see {@link #getId(int, int)});
	 * @param featureSensorID
	 * @return the sensor ID
	 */

	public static int parseSensorId(int featureSensorID) {
		return featureSensorID%100;
	}

	/*
	 * Returns the description of the specified mote
	 * @param moteType
	 * @return the mote type description (String)
	 */

	public static String getMoteDescription(int moteType) {
		return moteDescriptions.get(moteType);
	}

	/*
	 * the mote sensor descriptions
	 */
	public static String getMoteSensorDescriptions(int moteType, int chanID)
	{
		if(moteType == MOTE_TYPE_AUTOSENSE_1_ECG )
			return moteSensorAutoSense1Descriptions.get(chanID);
		else if(moteType == MOTE_TYPE_AUTOSENSE_1_RIP)
			return moteSensorAutoSense1Descriptions.get(chanID);
		else if(moteType == MOTE_TYPE_AUTOSENSE_2_ECG_RIP )
			return moteSensorAutoSense2Descriptions.get(chanID);
		else if(moteType == MOTE_TYPE_AUTOSENSE_2_ALCOHOL)
			return moteSensorAutoSense2Descriptions.get(chanID);

		return null;
	}



	/**
	 * Return the description of the specified sensor
	 * @param sensorID
	 * @return the description (String)
	 */

	public static String getSensorDescription(int sensorID) {
		return sensorDescriptions.get(sensorID);
	}

	/**
	 * Return the description of the specified feature
	 * @param featureID
	 * @return the description (String)
	 */

	public static String getFeatureDescription(int featureID) {
		return featureDescriptions.get(featureID);
	}

	/**
	 * Return the description of the specified model
	 * @param modelID
	 * @return the description (String)
	 */

	public static String getModelDescription(int modelID) {
		return modelDescriptions.get(modelID);
	}
	public static int getModelToIndex(String modeltext) {
		return modelToIndex.get(modeltext);
	}
	/* EMA related fields */
	public static int BEEP_COUNT=240; //4*60=4 minute beep
	public static long USER_DELAY=10*60*1000; // 10 minutes delay
	public static long START_TIMEOUT=5*60*1000; //5 minute time out;
	public static long INTERVIEW_TIMEOUT=5*60*1000; // 2 minutes
	public static boolean INTERVIEWRUNNING=false;
	public static boolean ALARMRUNNING=false;
	public static int ALARMDURATION;
	public static int REPEATALARM;
	public static int DIFF_BETWEEN_ALARM;
	public static long CLICK_DURATION=3*1000;
	public static String millisecondToDateTime(long milli)
	{
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(milli);
		String timeString =new SimpleDateFormat("MM-dd-yyyy HH:mm:ss:SSS").format(cal.getTime());
		return timeString;
	}
	public static String millisecondToTime(long milli)
	{
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(milli);
		String timeString =new SimpleDateFormat("hh:mm a").format(cal.getTime());
		return timeString;
	}
		public static long QUIETSTART=-1;
		public static long QUIETEND=-1;
		public static long DAYSTART=-1;
		public static long DAYEND=-1;
		public static long STUDYSTART=-1;
		public static long SLEEPSTART=-1;
		public static long SLEEPEND=-1;
}
