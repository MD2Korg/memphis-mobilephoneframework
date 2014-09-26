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

// @author Andrew Raij

package org.fieldstream.service.logger;


import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.fieldstream.Constants;
import org.fieldstream.FieldReportActivity.ReportValueObject;
import org.fieldstream.gui.ema.EMALogConstants;
import org.fieldstream.gui.ema.IContent;
import org.fieldstream.gui.ema.InterviewScheduler;
import org.fieldstream.incentives.AbstractIncentivesManager;
import org.fieldstream.service.ActivationManager;
import org.fieldstream.service.context.model.ModelCalculation;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensors.mote.sensors.QualityBuffer.DataQualityColorEnum;
import org.fieldstream.service.sensors.mote.sensors.SensorDataQualitySingleton.SensorTypeQualityEnum;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.DateFormat;


//NOTES for new logger
//--------------------
//TODO Maybe add a dbHelper to handle auto-creation and upgrades of db.
//Can't use SQLiteOpenHelper as is.  It requires db be in data dir rather than sdcard.
//see http://www.anddev.org/custom_content_providers_and_files-t9272.html for one solution.
//
//TODO VERIFY chestband hours incentive logging


/**
 * A class that handles all data logging from sensors, features, and models (contexts)
 *
 * All data should be timestamped in terms of milliseconds from the epoch (January 1, 1970 00:00:00 GMT).
 *
 * Will NOT log any sensor/feature/model listed in Constants.DATALOG_FILTER
 */



public class DatabaseLogger extends AbstractLogger  {
	private String dbFilename;
	private String dbDirectory;
	private SQLiteDatabase db;
	public static boolean active=true;
	//	private int count[]=new int[30];
	private HashMap<String, Boolean> tableExistsCache;
	private static int thread_count=0;
	private static final String TAG = "DataLoggerService";
	private HandlerThread mythread;
	private Handler myhandel;
	private static TextFileLogger txtfilelog=null;
	private FileOutputStream fout;
	private PrintStream printStrm;

	private String labStudyMark = "/sdcard/FieldStream/logs/LabStudyMark.txt";

	private static DatabaseLogger INSTANCE = null;

	//	private static HashSet<Object> refHolders;
	public static DatabaseLogger getInstance(Object holder) {
		if(active==false) return null;
		if (INSTANCE == null) {
			INSTANCE = new DatabaseLogger();
			//			refHolders = new HashSet<Object>();
		}
		//		if(!refHolders.contains(holder)){
		//			refHolders.add(holder);
		//			if(Log.DEBUG_MONOWAR ) Log.m("Monowar_ALL","DatabaseLogger: new_instance refHolder="+refHolders.size()+" "+holder.toString());
		//	}
		return INSTANCE;
	}

	public static void releaseInstance(Object holder) {
		active=false;

		//		if(refHolders.contains(holder)){
		//			refHolders.remove(holder);
		if(Log.DEBUG_MONOWAR ) Log.m("Monowar_ALL","DatabaseLogger: release_instance()");
		//		}
		//		if (refHolders.isEmpty()) {
		// time to shut down the db
		//while(thread_count>0);
		if(INSTANCE!=null){
			txtfilelog.close();
			INSTANCE.close();

		}
		INSTANCE = null;
		//			refHolders = null;
		//		}
	}
	/*	public static void releaseAllInstance() {
		refHolders.clear();

		if(Log.DEBUG_MONOWAR ) Log.m("Monowar_ALL","DatabaseLogger: release instance refHolder="+refHolders.size());
		if (refHolders.isEmpty()) {
			// time to shut down the db
			INSTANCE.close();
			INSTANCE = null;
			refHolders = null;
		}
	}
	 */




	/**
	 * Constructor for DataLogger.
	 * @param dbDirectory path to database
	 */
	protected DatabaseLogger() {
		super(true);
		mythread = new HandlerThread("DataLogService");
		mythread.setName("fs_DatabaseLogger_"+System.currentTimeMillis());
		mythread.start();

		myhandel = new Handler(mythread.getLooper());

		this.dbFilename = Constants.DATALOG_FILENAME;
		File root = Environment.getExternalStorageDirectory();
		this.dbDirectory = root + "/" + Constants.LOG_DIR;
		db = null;
		txtfilelog=new TextFileLogger(root + "/" + Constants.LOG_DIR, true);

		initDB();
		Log.m("Andrew Hood", "Database maximum size: " + db.getMaximumSize());

	}

	public void initDBEMA()
	{
		for(int i=0;i<Constants.EMA_QUESTION_FILENAME.length;i++){
			String filename=Constants.EMA_QUESTION_FILENAME[i];
			filename=filename.substring(0, filename.length()-4);
			Log.mm("IS", filename);

			if (!tableExists("EMA_metadata_"+filename)) {
				// create EMA metadata table
				String create = "CREATE TABLE " + "EMA_metadata_" + filename +
						" (_id INTEGER PRIMARY KEY, table_name TEXT, trigger_types TEXT, status_types TEXT, delay_questions_desc, delay_responses_desc TEXT, questions_desc TEXT, responses_desc TEXT);";
				db.execSQL(create);
				tableExistsCache.put("EMA_metadata_"+filename, true);
			}
		}

	}

	/**
	 * Constructor for DataLogger.
	 * @param dbDirectory path to database
	 */
	protected DatabaseLogger(String dbDirectory, boolean automaticLogging) {
		super(automaticLogging);

		mythread = new HandlerThread("DataLogService");
		mythread.setName("fs_DatabaseLogger1_"+System.currentTimeMillis());
		mythread.start();

		myhandel = new Handler(mythread.getLooper());
		this.dbFilename = Constants.DATALOG_FILENAME;
		this.dbDirectory = dbDirectory;
		db = null;

		initDB();
	}

	/**
	 *
	 * Opens the database if it exists.  Otherwise, it creates a new
	 * database and sets up the metadata tables
	 */
	protected void initDB() {
		String pathToFile = dbDirectory +"/" + dbFilename;
		tableExistsCache = new HashMap<String, Boolean>();

		try {
			File dir = new File(dbDirectory);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			db = SQLiteDatabase.openDatabase(pathToFile, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);

			if (!tableExists("sensor_metadata")) {
				// create sensors metadata table
				String create = "CREATE TABLE " + "sensor_metadata" +
						" (_id INTEGER PRIMARY KEY, table_name TEXT, sensor_desc TEXT);";
				db.execSQL(create);
				tableExistsCache.put("sensor_metadata", true);
			}

			if (!tableExists("feature_metadata")) {
				// create features metadata table
				String create = "CREATE TABLE " + "feature_metadata" +
						" (_id INTEGER PRIMARY KEY, table_name TEXT, sensor_desc TEXT, feature_desc TEXT);";
				db.execSQL(create);
				tableExistsCache.put("feature_metadata", true);
			}

			if (!tableExists("model_metadata")) {
				// create models metadata table
				String create = "CREATE TABLE " + "model_metadata" +
						" (_id INTEGER PRIMARY KEY, table_name TEXT, model_desc TEXT, model_outputs TEXT);";
				db.execSQL(create);
				tableExistsCache.put("model_metadata", true);
			}

			if (!tableExists("incentives_metadata")) {
				// create incentives metadata table
				String create = "CREATE TABLE " + "incentives_metadata" +
						" (_id INTEGER PRIMARY KEY, table_name TEXT, incentive_desc TEXT, total_earned DOUBLE);";
				db.execSQL(create);
				tableExistsCache.put("incentives_metadata", true);
			}

		} catch (SQLiteException ex) {
			Log.e(TAG, "Problem initializing DB: " + ex.getLocalizedMessage());
		}

		if (Log.DEBUG) Log.d(TAG, "DB ready");
	}



	/**
	 * Checks if the specified table exists
	 * @param tableName The unique id of the table.
	 */
	protected Boolean tableExists(String tableName) {
		if (db == null)
			return false;

		Boolean exists = tableExistsCache.get(tableName);

		// this table is not cached yet
		if (exists == null) {
			// SELECT name FROM sqlite_master WHERE name='table_name'
			String[] columns = {"name"};
			Cursor c = db.query("sqlite_master", columns, "name='" + tableName + "'", null, null, null, null);
			exists = c.getCount() > 0;
			c.close();

			tableExistsCache.put(tableName, exists);
		}

		return exists;
	}


	// SENSOR LOGGING
	// --------------

	/**
	 * Creates a table for the specified sensor.  This should only get called if the table does not exist already.
	 * @param tableName The unique id of the sensor (from Constants).
	 */
	protected Boolean createSensorTable(int sensorID) {
		/*		if (db == null)
			return null;

		String tableName = "sensor" + sensorID;

		try {
			// add the table to metadata
			ContentValues values = new ContentValues();
			//values.put("_id", p.getId());
			values.put("table_name", tableName);
			values.put("sensor_desc", Constants.getSensorDescription(Constants.parseSensorId(sensorID)));
			db.insertOrThrow("sensor_metadata", null, values);

			// create the table
			String create = "CREATE TABLE " + tableName +
					" (_id INTEGER PRIMARY KEY, start_timestamp INTEGER, end_timestamp INTEGER, num_samples INTEGER, timestamps BLOB, samples BLOB);";

			db.execSQL(create);

			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create sensor table " + tableName + ": " + e.getMessage());
		}
		 */		return null;
	}

	/**
	 * Reads data in the DB from the specified sensor.  This enables context inferencing algorithms
	 * to use older data no longer available in the feature/sensor/context buffers.
	 * @param sensorID The unique id of the sensor (from Constants).
	 * @param startTime, endTime The returned cursor will contain data in between (and including) startTime and endTime.
	 */
	public Cursor readSensorData(int sensorID, long startTime, long endTime) {
		/*		if (db == null)*/
		return null;
		/*
		String tableName = "sensor" + sensorID;
		if (!tableExists(tableName))
			return null;

		String[] columns = {"start_timestamp", "end_timestamp", "samples"};
		String where = "start_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		where +=  " OR end_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);

		return c;
		 */	}
	public Cursor readdataquality(int sensorID, long startTime, long endTime) {
		if (db == null)
			return null;

		String tableName = "dataquality" + Integer.toString(sensorID);
		if (!tableExists(tableName))
			return null;

		String[] columns = {"timestamp", "entry"};
		String where = "timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);
		return c;
	}


	/*	public int readValidSensorData(int sensorID, long startTime, long endTime) {
		if (db == null)
			return 0;

		String tableName = "dataquality" + sensorID;
		if (!tableExists(tableName))
			return 0;

		String[] columns = {"start_timestamp", "end_timestamp", "samples"};
		String where = "start_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		where +=  " OR end_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);

		return c;
	}
	 */
	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.logger.ILogger#logSensorData(int, long[], int[])
	 */
	public synchronized void logSensorData(int sensorID, long[] timestamps, int[] buffer, int startNewData, int endNewData){
		if(sensorID>=30 && sensorID<=37 || sensorID==26)
			txtfilelog.logSensorData(sensorID, timestamps, buffer, startNewData, endNewData);
		/*		if (timestamps.length <=0 || buffer.length <= 0)
			return;
		if (Constants.DATALOG_FILTER.contains(sensorID))
			return;
		if (db == null)
			return;

		//		count[sensorID]++;
		//ignores to save to DB file since these are saved in TOS file. We avoid duplication of data here to keep the file size small.
		if(sensorID==12 || sensorID==21 || sensorID==27 || sensorID==11 || sensorID==13 || sensorID==14 || sensorID==18 || sensorID==19 || sensorID==20)
			return;
		//			if(Log.DEBUG_MONOWAR) Log.d("Monowar_ALL","OK    : (DBLOGGER count="+count[sensorID]+")ID="+sensorID+" DLength="+buffer.length+" STime="+timestamps[0]);

		String tableName = "sensor" + sensorID;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createSensorTable(sensorID);
		}
		thread_count++;
		SensorLogRunner sensorLogRunner = new SensorLogRunner(tableName, timestamps, buffer, startNewData, endNewData);
		myhandel.post(sensorLogRunner);
		thread_count--;
		 */	}

	/**
	 * A class that makes sensor logging occur in a new thread
	 */
	/*	class SensorLogRunner implements Runnable {
		private String tableName;
		private long[] timestamps;
		private int[] buffer;
		private int startNewData;
		private int endNewData;

		/**
	 * Constructor
	 */
	/*		public SensorLogRunner(String tableName, long[] timestamps, int[] buffer, int startNewData, int endNewData) {
			this.tableName = tableName;
			this.timestamps = timestamps;
			this.buffer = buffer;
			this.startNewData = startNewData;
			this.endNewData = endNewData;

		}

		/**
	 * Handles logging
	 */
	/*	public synchronized void run() {
			long id = -1;

			try{
				int start, end;
				Thread.currentThread().setName("fs_sensor_LogRunner_"+tableName+"_"+System.currentTimeMillis());
				if (startNewData == endNewData) {
					start = 0;
					end = buffer.length;
				}
				else {
					start = startNewData;
					end = endNewData;
				}
				ContentValues values = new ContentValues();
				values.put("num_samples", end - start);
				values.put("start_timestamp", timestamps[start]);
				values.put("end_timestamp", timestamps[end-1]);

				byte [] bytes = longSubArrayToByteArray(timestamps, start, end);
				values.put("timestamps", bytes);
				bytes = intSubArrayToByteArray(buffer, start, end);
				values.put("samples", bytes);


				if(Log.DEBUG_MONOWAR && (tableName.endsWith("12")||tableName.endsWith("21")||tableName.endsWith("27"))) Log.m("Monowar_ALL","(DatabaseLogger) table="+tableName+" DSize="+buffer.length+" STime="+timestamps[0]);
				while (db.isOpen()) {
					id = db.insertOrThrow(tableName, null, values);
					if(id<=0) {
						if(Log.DEBUG_MONOWAR ) Log.m("Monowar_ALL","Exception: table="+tableName+" DSize="+buffer.length+" STime="+timestamps[0]);
						if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("All_Sensor", "Exception: table="+tableName+" DSize="+buffer.length+" STime="+timestamps[0],System.currentTimeMillis());}
					}
					else {
						if(Log.DEBUG_MONOWAR && (tableName.endsWith("12")||tableName.endsWith("21")||tableName.endsWith("27"))) Log.m("Monowar_ALL","(DatabaseLogger) table="+tableName+"ID="+id+" DSize="+buffer.length+" STime="+timestamps[0]);
						break;
					}
				}
			}
			catch (SQLiteException e) {
				Log.e(TAG, "Could not write sensor sample to table " + tableName + ": " + e.getMessage());
				if(Log.DEBUG_MONOWAR) Log.d("Monowar_ALL","Could not write sensor sample to table " + tableName + ": " + e.getMessage());
			}
		}
	}
	 */
	private static final byte[] intSubArrayToByteArray(int[] input, int start, int end) {
		byte[] output = new byte[(end-start) * 4];
		int j = 0;
		for (int i=start; i < end; i++, j++) {
			output[4*j] = (byte)(input[i] >>> 24);
			output[4*j + 1] = (byte)(input[i] >>> 16);
			output[4*j + 2] =(byte)(input[i] >>> 8);
			output[4*j + 3] = (byte)input[i];
		}
		return output;
	}

	private static final byte[] longSubArrayToByteArray(long[] input, int start, int end) {
		byte[] output = new byte[(end-start) * 8];
		int j = 0;
		for (int i=start; i < end; i++, j++) {
			output[8*j] = (byte)(input[i] >>> 56);
			output[8*j + 1] = (byte)(input[i] >>> 48);
			output[8*j + 2] =(byte)(input[i] >>> 40);
			output[8*j + 3] = (byte)(input[i] >>> 32);
			output[8*j + 4] = (byte)(input[i] >>> 24);
			output[8*j + 5] = (byte)(input[i] >>> 16);
			output[8*j + 6] =(byte)(input[i] >>> 8);
			output[8*j + 7] = (byte)input[i];

		}
		//		if(Log.MONOWAR && end-start>1200) Log.d("Monowar_ALL","OK    : (DBLOGGER):start+"+start+"end="+end+","+input[0]+","+input[1]+","+input[2]+","+input[end-1]);

		return output;
	}

	public static final int byteArrayToInt(byte [] b) {
		return (b[0] << 24)
				+ ((b[1] & 0xFF) << 16)
				+ ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}

	public static final long byteArrayToLong(byte [] b) {
		return (b[0] << 56) + ((b[1] & 0xFF) << 48) + ((b[2] & 0xFF) << 40) + (b[3] & 0xFF << 32) + (b[4] & 0xFF << 24) + (b[5] & 0xFF << 16) + (b[6] & 0xFF << 8) + (b[7] & 0xFF);
	}

	// FEATURE LOGGING
	// ---------------

	/**
	 * Creates a table for the specified sensor-feature pair.  This should only get called if the table does not exist already.
	 * @param featureID The unique id of the feature-sensor (from Constants).
	 */
	protected Boolean createFeatureTable(int featureID) {
		if (db == null)
			return null;

		String tableName = "feature" + featureID;

		try {
			// add the table to metadata
			ContentValues values = new ContentValues();
			values.put("table_name", tableName);
			values.put("sensor_desc", Constants.getSensorDescription(Constants.parseSensorId(featureID)));
			values.put("feature_desc", Constants.getFeatureDescription(Constants.parseFeatureId(featureID)));
			db.insertOrThrow("feature_metadata", null, values);

			// create the table
			String create = "CREATE TABLE " + tableName +
					" (_id INTEGER PRIMARY KEY, start_timestamp INTEGER, end_timestamp INTEGER, value DOUBLE);";

			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create feature table " + tableName + ": " + e.getMessage());
		}

		return null;
	}


	/**
	 * Reads data in the DB from the specified sensor-feature pair.  This enables context inferencing algorithms
	 * to use older data no longer available in the feature/sensor/context buffers.
	 * @param featureID The unique id of the sensor-feature pair (from Constants).
	 * @param startTime, endTime The returned cursor will contain data in between (and including) startTime and endTime.
	 */
	public Cursor readFeatureData(int featureID, long startTime, long endTime) {
		if (db == null)
			return null;

		String tableName = "feature" + featureID;
		if (!tableExists(tableName))
			return null;

		String[] columns = {"start_timestamp", "end_timestamp", "value"};
		String where = "start_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		where +=  " OR end_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);

		return c;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.logger.ILogger#logFeatureData(int, long, double)
	 */
	public void logFeatureData(int featureID, long timeBegin, long timeEnd, double value){
		if (Constants.DATALOG_FILTER.contains(featureID))
			return;

		if (db == null)
			return;

		String tableName = "feature" + featureID;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createFeatureTable(featureID);
		}

		// write to DB
		try{
			ContentValues values = new ContentValues();
			values.put("start_timestamp", timeBegin);
			values.put("end_timestamp", timeEnd);
			values.put("value", value);

			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write feature value to table " + tableName + ": " + e.getMessage());
		}

		//		FeatureLogRunner featureLogRunner = new FeatureLogRunner(tableName, timestamp, value);
		//		myhandel.post(featureLogRunner);
	}


	//	/**
	//	 * A class that makes feature logging occur in a new thread
	//	 */
	//	class FeatureLogRunner implements Runnable {
	//		private String tableName;
	//		private long timestamp;
	//		private double value;
	//
	//		/**
	//		 * Constructor
	//		 */
	//		public FeatureLogRunner(String tableName, long timestamp, double value) {
	//			this.tableName = tableName;
	//			this.timestamp = timestamp;
	//			this.value = value;
	//		}
	//
	//		/**
	//		 * Handles logging
	//		 */
	//		public void run() {
	//			try{
	//				ContentValues values = new ContentValues();
	//				values.put("timestamp", timestamp);
	//				values.put("value", value);
	//
	//				db.insertOrThrow(tableName, null, values);
	//			}
	//			catch (SQLiteException e) {
	//				Log.e(TAG, "Could not write feature value to table " + tableName + ": " + e.getMessage());
	//			}
	//		}
	//	}



	// CONTEXT/MODEL LOGGING
	// ---------------------

	/**
	 * Creates a table for the specified model.  This should only get called if the table does not exist already.
	 * @param modelID The unique id of the model (from Constants).
	 */
	protected Boolean createModelTable(int modelID) {
		if (db == null)
			return null;

		String tableName = "model" + modelID;

		try {
			// add the table to metadata
			ContentValues values = new ContentValues();
			values.put("table_name", tableName);
			values.put("model_desc", Constants.getModelDescription(modelID));

			Log.mm("aa", tableName+" "+Constants.getModelDescription(modelID));

			ModelCalculation model = ActivationManager.models.get(modelID);
			Log.mm("aa", model+" "+ActivationManager.models.size());

			Iterator<Entry<Integer,String>> itr = model.getOutputDescription().entrySet().iterator();
			String outputs = "";
			while(itr.hasNext()) {
				Entry<Integer,String> entry = itr.next();
				outputs += entry.getKey() + ":" + entry.getValue();
				if (itr.hasNext()) {
					outputs+="\n";
				}
			}
			values.put("model_outputs", outputs);

			db.insertOrThrow("model_metadata", null, values);

			// create the table
			String create = "CREATE TABLE " + tableName +
					" (_id INTEGER PRIMARY KEY, start_timestamp INTEGER, end_timestamp INTEGER, label INTEGER);";

			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create model table " + tableName + ": " + e.getMessage());
		}
		return null;
	}


	/**
	 * Reads data in the DB from the specified model.  This enables context inferencing algorithms
	 * to use older data no longer available in the feature/sensor/context buffers.
	 * @param modelID The unique id of the model (from Constants).
	 * @param startTime, endTime The returned cursor will contain data in between (and including) startTime and endTime.
	 */
	public long readFirstDay() {
		long firstday=System.currentTimeMillis();
		if (db == null)
			return firstday;

		String tableName = "prog_stat";
		if (!tableExists(tableName))
			logAnything("prog_stat", "first", System.currentTimeMillis());
		String[] columns = {"_id", "timestamp", "entry"};
		Cursor c = db.query(tableName, columns, null, null, null, null, null);
		if (c!=null) {
			if (c.moveToFirst()) {
				do {
					long temp=c.getLong(c.getColumnIndex("timestamp"));
					if(firstday>temp) firstday=temp;
				} while (c.moveToNext());
			}
			c.close();
		}
		return firstday;
	}
	public long getdeadperiodtoday(String str) {
		String s;
		int ID=-1,id;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,0);		cal.set(Calendar.MINUTE,0);		cal.set(Calendar.SECOND,0);		cal.set(Calendar.MILLISECOND,0);
		long starttime=cal.getTimeInMillis();
		cal.set(Calendar.HOUR_OF_DAY,23);		cal.set(Calendar.MINUTE,59);		cal.set(Calendar.SECOND,59);		cal.set(Calendar.MILLISECOND,0);
		long endtime=cal.getTimeInMillis();

		long time,TIME=-1;
		if (db == null)
			return -1;

		String tableName = "deadperiod";
		if (!tableExists(tableName))
			return -1;
		String[] columns = {"_id", "timestamp", "entry1","entry2"};
		Cursor c = db.query(tableName, columns, null, null, null, null, null);
		if (c!=null) {
			if (c.moveToFirst()) {
				do {
					s=c.getString(c.getColumnIndex("entry1"));
					if(!s.equals(str))continue;
					id=c.getInt(c.getColumnIndex("_id"));
					time=c.getLong(c.getColumnIndex("entry2"));
					if(time>=starttime && time<=endtime)
						if(id>ID){ID=id;TIME=time;}
				} while (c.moveToNext());
			}
			c.close();
		}
		if(ID==-1) return -1;
		return TIME;
	}

	public long getlastfromdeadperiod(String str) {
		String s;
		int ID=-1,id;
		long time,TIME=-1;
		if (db == null)
			return -1;

		String tableName = "deadperiod";
		if (!tableExists(tableName))
			return -1;
		String[] columns = {"_id", "timestamp", "entry1","entry2"};
		Cursor c = db.query(tableName, columns, null, null, null, null, null);
		if (c!=null) {
			if (c.moveToFirst()) {
				do {
					s=c.getString(c.getColumnIndex("entry1"));
					if(!s.equals(str))continue;
					id=c.getInt(c.getColumnIndex("_id"));
					time=c.getLong(c.getColumnIndex("entry2"));
					if(id>ID) {ID=id;TIME=time;}
				} while (c.moveToNext());
			}
			c.close();
		}
		if(ID==-1) return -1;
		return TIME;
	}
	public int isdeadperiod(String str,long timestamp) {
		String s;
		long time;
		if (db == null)
			return 0;

		String tableName = "deadperiod";
		if (!tableExists(tableName))
			return 0;
		String[] columns = {"_id", "timestamp", "entry1","entry2"};
		Cursor c = db.query(tableName, columns, null, null, null, null, null);
		if (c!=null) {
			if (c.moveToFirst()) {
				do {
					s=c.getString(c.getColumnIndex("entry1"));
					if(!s.equals(str))continue;
					time=c.getLong(c.getColumnIndex("entry2"));
					if(time==timestamp) return 1;
				} while (c.moveToNext());
			}
			c.close();
		}
		return 0;
	}
	public long getdeadperiod(String str,long starttimestamp,long endtimestamp) {
		String s;
		long time;
		int ID=-1,id;
		long TIME=-1;
		if (db == null)
			return -1;
		String tableName = "deadperiod";

		if (!tableExists(tableName))
			return -1;
		String[] columns = {"_id", "timestamp", "entry1","entry2"};
		Cursor c = db.query(tableName, columns, null, null, null, null, null);
		if (c!=null) {
			if (c.moveToFirst()) {
				do {
					s=c.getString(c.getColumnIndex("entry1"));
					if(!s.equals(str))continue;
					time=c.getLong(c.getColumnIndex("entry2"));
					id=c.getInt(c.getColumnIndex("_id"));
					if (time>=starttimestamp && time<=endtimestamp) if(id>ID) {TIME=time;ID=id;}
				} while (c.moveToNext());
			}
			c.close();
		}
		return TIME;
	}

	public int getNumReportObserved(int modelID,long starttime,long endtime)
	{
		int count=0;
		Cursor c=readModelData(modelID, starttime, endtime);
		if (c==null)
			return 0;
		count=c.getCount();
		c.close();
		return count;
	}

	public Cursor readModelData(int modelID, long startTime, long endTime) {
		if (db == null)
			return null;

		String tableName = "model" + modelID;
		if (!tableExists(tableName))
			return null;

		String[] columns = {"start_timestamp", "end_timestamp", "label"};
		String where = "start_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		where +=  " OR end_timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);

		return c;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.ices.stress.phone.service.logger.ILogger#logModelData(int, long, int)
	 */
	public void logModelData(int modelID, int label, long startTime, long endTime){
		//		if (Constants.DATALOG_FILTER.contains(modelID))
		//			return;

		if (db == null)
			return;

		String tableName = "model" + modelID;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			Log.mm("a", tableName+" "+modelID);
			createModelTable(modelID);
		}

		// write to the db
		try{
			ContentValues values = new ContentValues();
			values.put("start_timestamp", startTime);
			values.put("end_timestamp", endTime);
			values.put("label", label);

			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write model label to table " + tableName + ": " + e.getMessage());
		}

		//		ModelLogRunner modelLogRunner = new ModelLogRunner(tableName, timestamp, label);
		//		myhandel.post(modelLogRunner);
	}


	//	/**
	//	 * A class that makes feature logging occur in a new thread
	//	 */
	//	class ModelLogRunner implements Runnable {
	//		private String tableName;
	//		private long timestamp;
	//		private int label;
	//
	//		/**
	//		 * Constructor
	//		 */
	//		public ModelLogRunner(String tableName, long timestamp, int label) {
	//			this.tableName = tableName;
	//			this.timestamp = timestamp;
	//			this.label = label;
	//		}
	//
	//		/**
	//		 * Handles logging
	//		 */
	//		public void run() {
	//			try{
	//				ContentValues values = new ContentValues();
	//				values.put("timestamp", timestamp);
	//				values.put("label", Integer.toString(label));
	//
	//				db.insertOrThrow(tableName, null, values);
	//			}
	//			catch (SQLiteException e) {
	//				Log.e(TAG, "Could not write model label to table " + tableName + ": " + e.getMessage());
	//			}
	//		}
	//	}


	// EMA LOGGING
	// -----------

	/**
	 * Creates a table for logging EMA data  (labels, timing, etc.).
	 * This should only get called if the table does not exist already.
	 * This is tied very closely to the InterviewContent class.
	 */
	protected Boolean createEMATable() {
		if (db == null)
			return null;
		String filename=Constants.EMA_QUESTION_FILENAME[Constants.INDEX_CURRENT_EMA_QUESTION_FILENAME];
		filename=filename.substring(0, filename.length()-4);

		String tableName = "ema_"+filename;
		// add the table to metadata
		ContentValues values = new ContentValues();
		values.put("table_name", tableName);

		// trigger types
		Iterator<Entry<Integer,String>> itr = EMALogConstants.emaTriggerDescriptions.entrySet().iterator();
		String triggerTypes = "";
		while(itr.hasNext()) {
			Entry<Integer,String> entry = itr.next();
			triggerTypes += entry.getKey() + ":" + entry.getValue();
			if (itr.hasNext()) {
				triggerTypes+="\n";
			}
		}
		values.put("trigger_types", triggerTypes);

		// status types
		itr = EMALogConstants.emaStatusDescriptions.entrySet().iterator();
		String statusTypes = "";
		while(itr.hasNext()) {
			Entry<Integer,String> entry = itr.next();
			statusTypes += entry.getKey() + ":" + entry.getValue();
			if (itr.hasNext()) {
				statusTypes+="\n";
			}
		}
		values.put("status_types", statusTypes);

		IContent content = InterviewScheduler.getContent();

		// delay questions
		int len = content.getNumberDelayQuestions();
		String questions = "";
		for (int i = 0; i < len; i++) {
			questions += i + ":" + content.getDelayQuestion(i);
			if (i < len - 1) {
				questions += "\n";
			}
		}
		values.put("delay_questions_desc", questions);

		// delay responses
		String responses = "";
		for (int i = 0; i < len; i++) {
			responses += i + ":[";

			String[] temp = content.getDelayResponses(i);
			for (int j=0; j < temp.length; j++) {
				responses += temp[j];
				if (j < temp.length -1) {
					responses += ", ";
				}
			}

			responses += "]\n";
		}
		values.put("delay_responses_desc", responses);

		// questions
		len = content.getNumberQuestions(false);
		questions = "";
		for (int i = 0; i < len; i++) {
			questions += i + ":" + content.getQuestion(i);
			if (i < len - 1) {
				questions += "\n";
			}
		}
		values.put("questions_desc", questions);

		// responses
		responses = "";
		for (int i = 0; i < len; i++) {
			responses += i + ":[";

			String[] temp = content.getResponses(i);
			if (temp != null) {
				for (int j=0; j < temp.length; j++) {
					responses += temp[j];
					if (j < temp.length -1) {
						responses += ", ";
					}
				}
			}
			responses += "]\n";
		}
		values.put("responses_desc", responses);

		try {
			String ffilename=Constants.EMA_QUESTION_FILENAME[Constants.INDEX_CURRENT_EMA_QUESTION_FILENAME];
			ffilename=ffilename.substring(0, ffilename.length()-4);

			db.insertOrThrow("ema_metadata_"+ffilename, null, values);

			// create the table
			String create = "CREATE TABLE " + tableName +
					" (_id INTEGER PRIMARY KEY, trigger_type INTEGER, context TEXT, status INTEGER, " +
					" prompt_timestamp INTEGER, delay_duration INTEGER, start_timestamp INTEGER";

			len = content.getNumberDelayQuestions();
			for (int i=0; i < len; i++) {
				create += ", delay_response" + i + " TEXT, delay_response_time" + i + " INTEGER";
			}

			len = content.getNumberQuestions(false);
			for (int i=0; i < len; i++) {
				create += ", response" + i + " TEXT, response_time" + i + " INTEGER";
			}

			create += ");";

			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create EMA table " + tableName + ": " + e.getMessage());
		}
		return null;
	}
	public synchronized void createEMACountTable(String tableName)
	{
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, trigger_type INTEGER, context TEXT, status INTEGER, " +
				" prompt_timestamp INTEGER, completed_timestamp INTEGER";

		create += ");";

		db.execSQL(create);
		tableExistsCache.put(tableName, true);

	}

	public synchronized void logEMAcount(int triggerType, String emaContext, int status, long prompt,
			long completed){
		if (db == null)
			return;

		String tableName = "ema_count";

		if (!tableExists(tableName)) {
			// if not, create the table
			createEMACountTable(tableName);
		}
		try{
			ContentValues values = new ContentValues();
			values.put("trigger_type", triggerType);
			values.put("context", emaContext);
			values.put("status", status);
			values.put("prompt_timestamp", prompt);
			values.put("completed_timestamp", completed);

			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write EMA data to table " + tableName + ": " + e.getMessage());
		}
	}

	public void logEMA(int triggerType, String emaContext, int status, long prompt,
			long delayDuration, String[] delayResponses, long[] delayResponseTimes, long start,
			String[] responses, long[] responseTimes) {
		Log.ema_alarm("", "Trig="+triggerType+" emaContext="+emaContext+" status="+status);
		if (db == null)
			return;
		String filename=Constants.EMA_QUESTION_FILENAME[Constants.INDEX_CURRENT_EMA_QUESTION_FILENAME];
		filename=filename.substring(0, filename.length()-4);

		String tableName = "ema_"+filename;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			initDBEMA();
			createEMATable();
		}

		EMALogRunner emaLogRunner = new EMALogRunner(tableName, triggerType, emaContext, status,
				prompt, delayDuration, delayResponses, delayResponseTimes,
				start, responses, responseTimes);
		myhandel.post(emaLogRunner);

		this.logEMAcount(triggerType, emaContext, status, prompt, System.currentTimeMillis());
	}

	/**
	 * A class that makes sensor logging occur in a new thread
	 */
	class EMALogRunner implements Runnable {
		String tableName;
		int triggerType;
		int status;
		long prompt;
		long delayDuration;
		String[] delayResponses;
		long[] delayResponseTimes;
		long start;
		String[] responses;
		long[] responseTimes;
		String emaContext;

		/**
		 * Constructor
		 */
		public EMALogRunner(String tableName, int triggerType, String emaContext, int status, long prompt,
				long delayDuration, String[] delayResponses, long[] delayResponseTimes, long start,
				String[] responses, long[] responseTimes) {
			this.tableName = tableName;
			this.triggerType = triggerType;
			this.status = status;
			this.prompt = prompt;
			this.delayDuration = delayDuration;
			this.delayResponses = delayResponses;
			this.delayResponseTimes = delayResponseTimes;
			this.start = start;
			this.responses = responses;
			this.responseTimes = responseTimes;
			this.emaContext = emaContext;

		}

		/**
		 * Handles logging
		 */
		public void run() {
			// write to the db
			Thread.currentThread().setName("fs_EMALogRunner_"+System.currentTimeMillis());

			try{
				ContentValues values = new ContentValues();
				values.put("trigger_type", triggerType);
				values.put("context", emaContext);
				values.put("status", status);
				values.put("prompt_timestamp", prompt);
				values.put("start_timestamp", start);

				values.put("delay_duration", delayDuration);

				IContent content = InterviewScheduler.getContent();
				int len = content.getNumberDelayQuestions();
				for (int i=0; i < len; i++) {
					values.put("delay_response" + i, delayResponses[i]);
					values.put("delay_response_time" + i, delayResponseTimes[i]);
				}

				len = content.getNumberQuestions(false);
				for (int i=0; i < len; i++) {
					values.put("response" + i, responses[i]);
					values.put("response_time" + i, responseTimes[i]);
				}


				db.insertOrThrow(tableName, null, values);
			}
			catch (SQLiteException e) {
				Log.e(TAG, "Could not write EMA data to table " + tableName + ": " + e.getMessage());
			}
		}
	}

	public int getNumEMAsWithoutSaliva(long startTime,long endTime) {
		Cursor c = readEMAcount(startTime,endTime);
		if(c==null) return 0;
		int contextcolumn = c.getColumnIndex("context");
		int value,count=0;

		Log.ema_alarm("", "starttime="+Constants.millisecondToDateTime(startTime)+" endtime="+Constants.millisecondToDateTime(endTime)+" contextcolumn="+contextcolumn+" count="+c.getCount()+" column="+c.getColumnCount());

		if (c.moveToFirst()) {
			do {
				String str=c.getString(contextcolumn);
				if(str==null || str.length()==0) {count++;continue;}
				value =Integer.parseInt(str);
				if(value!=Constants.MODEL_COLLECT_SALIVA) count++;

			} while (c.moveToNext());
		}

		c.close();
		return count;
	}
	public int getNumEMAsAll(long startTime,long endTime) {
		Cursor c = readEMAcount(startTime,endTime);
		if(c==null) return 0;
		int contextcolumn = c.getColumnIndex("context");
		int value,count=0;

		Log.ema_alarm("", "starttime="+Constants.millisecondToDateTime(startTime)+" endtime="+Constants.millisecondToDateTime(endTime)+" contextcolumn="+contextcolumn+" count="+c.getCount()+" column="+c.getColumnCount());

		if (c.moveToFirst()) {
			do {
				String str=c.getString(contextcolumn);
				if(str==null || str.length()==0) {count++;continue;}
				value =Integer.parseInt(str);
				//if(value!=Constants.MODEL_COLLECT_SALIVA) count++;
				count++;
			} while (c.moveToNext());
		}

		c.close();
		return count;
	}

	public int getNumEMAsWithin7Mins(long startTime,long endTime) {
		Cursor c = readEMAcount(startTime,endTime);
		if(c==null) return 0;
		int contextcolumn = c.getColumnIndex("context");
		int value=0,count=0;

		Log.ema_alarm("", "starttime="+Constants.millisecondToDateTime(startTime)+" endtime="+Constants.millisecondToDateTime(endTime)+" contextcolumn="+contextcolumn+" count="+c.getCount()+" column="+c.getColumnCount());

		if (c.moveToFirst()) {
			do {
				String str;
				str=c.getString(contextcolumn);
				if(str==null || str.length()==0){
					value=0;
				}
				else{
					value =Integer.parseInt(str);
				}
//				if(value==Constants.MODEL_COLLECT_SALIVA) continue;
				long ptime=c.getLong(c.getColumnIndex("prompt_timestamp"));
				long ctime=c.getLong(c.getColumnIndex("completed_timestamp"));
				int status=c.getInt(c.getColumnIndex("status"));
				if(status!=0) continue;
				if(ctime-ptime<=7*60*1000)
					count++;
			} while (c.moveToNext());
		}

		c.close();
		return count;
	}
	public boolean getEmaStatistics(long startTime, long endTime, long thresholdTime, ReportValueObject reportValueObject) {
		reportValueObject.emaPromptCount = 0;
		reportValueObject.emaAnsweredCount = 0;
		reportValueObject.emaAnswered7MinCount = 0;

		Cursor c = readEMAcount(startTime,endTime);
		if(c==null) return false;
		int contextcolumn = c.getColumnIndex("context");
		int value=0;

		Log.ema_alarm("", "starttime="+Constants.millisecondToDateTime(startTime)+" endtime="+Constants.millisecondToDateTime(endTime)+" contextcolumn="+contextcolumn+" count="+c.getCount()+" column="+c.getColumnCount());

		if (c.moveToFirst()) {
			do {
				String str;
				str=c.getString(contextcolumn);
				if(str==null || str.length()==0){
					value=0;
				}
				else{
					value =Integer.parseInt(str);
				}
//				if(value==Constants.MODEL_COLLECT_SALIVA) continue;
				long ptime=c.getLong(c.getColumnIndex("prompt_timestamp"));
				long ctime=c.getLong(c.getColumnIndex("completed_timestamp"));
				int status=c.getInt(c.getColumnIndex("status"));
				reportValueObject.emaPromptCount++;
				if(status!=0) continue;
				reportValueObject.emaAnsweredCount++;
				if(ctime-ptime<=thresholdTime) {
					reportValueObject.emaAnswered7MinCount++;
				}
			} while (c.moveToNext());
		}
		c.close();
		return true;
	}

	public long getLastEMATime(String fieldname) {
		Cursor c = readEMAcount(0,Long.MAX_VALUE);
		long value=0,max=0;
		if (c==null)
			return 0;
		if(c.getCount()!=0){
			int contextcolumn = c.getColumnIndex(fieldname);
			c.moveToFirst();
			value = c.getLong(contextcolumn);
			max=value;
			while (c.moveToNext()) {
				value = c.getLong(contextcolumn);
				if (value>max) {
					max=value;
				}
			}
		}
		c.close();
		return max;
	}

	public int getNumContextEMAs(int modelID,long starttime,long endtime)
	{
		int count=0,value;
		Cursor c=readEMAcount(starttime, endtime);
		if (c==null)
			return 0;
		if (c.getCount() != 0) {
			int contextcolumn = c.getColumnIndex("context");
			c.moveToFirst();
			value = c.getInt(contextcolumn);
			if(value==modelID) count++;
			while (c.moveToNext()) {
				value = c.getInt(contextcolumn);
				if (value==modelID) {
					count++;
				}
			}
		}
		c.close();
		return count;
	}
	public long getLastEMA_completed_or_prompted()
	{
		long completed=this.getLastEMATime("completed_timestamp");
		long prompted=getLastEMATime("prompt_timestamp");
		if(completed>prompted) return completed;
		else return prompted;
	}
	public Cursor readEMAcount(long startTime, long endTime) {
		if (db == null)
			return null;

		String tableName = "ema_count";
		if (!tableExists(tableName))
			return null;


		String[] columns = new String[5];
		columns[0] = "trigger_type";
		columns[1] = "context";
		columns[2] = "status";
		columns[3] = "prompt_timestamp";
		columns[4] = "completed_timestamp";

		Cursor c = db.query(tableName, columns, "prompt_timestamp BETWEEN'" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) +"'", null, null, null, null);

		return c;
	}


	@Override
	public void logUIData(String data) {
		// TODO Auto-generated method stub

	}

	// INCENTIVES LOGGING
	// ------------------

	/**
	 * Creates a table for logging earned incentives.  This should only get called if the table does not exist already.
	 */
	protected Boolean createIncentivesTable(int incentiveID) {
		if (db == null) {
			Log.d("db", "createIncentiveTable - db is null");
			return null;
		}

		String tableName = "incentives" + incentiveID;

		Log.d("db", "trying to create incentives table");

		try {
			// add the table to metadata
			ContentValues values = new ContentValues();
			values.put("table_name", tableName);

			String incentiveDesc = AbstractIncentivesManager.getIncentiveDesc(incentiveID);

			values.put("incentive_desc",incentiveDesc);
			values.put("total_earned", 0.00);
			db.insertOrThrow("incentives_metadata", null, values);

			// create the table
			String create = "CREATE TABLE " + tableName +
					" (_id INTEGER PRIMARY KEY, timestamp INTEGER, comment TEXT, amount DOUBLE);";
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create incentives table " + tableName + ": " + e.getMessage());
		}
		return null;
	}

	public void logIncentiveEarned(int incentiveID, String comment, long timestamp, float amount, float total) {
		if (db == null) {
			Log.d("db", "logincentive - db is null");
			return;
		}

		Log.d("db", "trying to log incentives");

		String tableName = "incentives" + incentiveID;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createIncentivesTable(incentiveID);
		}

		// write to the db
		try{
			ContentValues values = new ContentValues();
			if (amount != 0) {
				values.put("timestamp", timestamp);
				values.put("comment", comment);
				values.put("amount", amount);
				db.insertOrThrow(tableName, null, values);
			}

			values.clear();
			values.put("total_earned", total);
			db.update("incentives_metadata", values, "table_name='" + tableName + "'", null);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write incentive to table " + tableName + ": " + e.getMessage());
		}
	}

	/**
	 * Reads incentives data in the DB.  This enables reloading incentives data.
	 */
	public Cursor readIncentivesData(int incentiveID) {
		if (db == null) {
			Log.d("db", "db is null");
			return null;
		}

		String tableName = "incentives" + incentiveID;
		if (!tableExists(tableName)) {
			Log.d("db", "table " + tableName + "does not exist");
			return null;
		}

		String[] columns = {"amount", "timestamp", "comment"};
		Cursor c = db.query(tableName, columns, null, null, null, null, null);

		return c;
	}

	//	/**
	//	 * Reads incentives data in the DB.  This enables reloading incentives data.
	//	 */
	//	public long readIncentivesTimeWearingBand() {
	//		if (db == null)
	//			return 0;
	//
	//		String tableName = "incentives_metadata";
	//		if (!tableExists(tableName))
	//			return 0;
	//
	//		String[] columns = {"ms_wearing_band"};
	//		Cursor c = db.query(tableName, columns, null, null, null, null, null);
	//		if (c.moveToFirst()) {
	//			return c.getLong(0);
	//		}
	//
	//		return 0;
	//	}


	// PERFORMANCE/DEBUG LOGGING
	// -------------------------

	/**
	 * Creates a table for logging performance values.  This should only get called if the table does not exist already.
	 */
	protected Boolean createPerformanceTable() {
		if (db == null)
			return null;

		String tableName = "performance";

		// create the table
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, timestamp INTEGER, location INTEGER, report TEXT);";

		try {
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create performance table " + tableName + ": " + e.getMessage());
		}
		return null;
	}


	@Override
	public void logPerformance(int location, long timestamp, String logString) {
		if (db == null)
			return;

		String tableName = "performance";

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createPerformanceTable();
		}

		// write to the db
		try{
			ContentValues values = new ContentValues();
			values.put("location", location);
			values.put("timestamp", timestamp);
			values.put("report", logString);

			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write performance string to table " + tableName + ": " + e.getMessage());
		}

	}



	public double getTotalIncentivesEarned() {

		double total = 0.0;
		String columns[] = {"total_earned"};
		Cursor c = db.query("incentives_metadata", columns, null, null, null, null, null);

		int index = c.getColumnIndex(columns[0]);

		if (c.moveToFirst()) {
			do {
				total += c.getDouble(index);
			} while (c.moveToNext());
		}

		c.close();

		return total;
	}

	/**
	 * Creates a table for logging dead periods.  This should only get called if the table does not exist already.
	 */
	/*	protected Boolean createDeadPeriodTable() {
		if (db == null)
			return null;

		String tableName = "deadperiod";

		// create the table
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, start INTEGER, end INTEGER);";

		try {
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create dead period table " + tableName + ": " + e.getMessage());
		}
		return null;
	}


	 */
	@Override
	public void close() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (DatabaseLogger)_close() db="+db.toString());
		if (db != null && db.isOpen())
			db.close();

		db = null;

		myhandel.removeCallbacks(mythread);
		myhandel.getLooper().quit();
		myhandel = null;
		super.close();
		INSTANCE=null;

		if (Log.DEBUG) Log.d("DatabaseLogger","closing db");
	}


	protected Boolean createResumeTable() {
		if (db == null)
			return null;

		String tableName = "resume";

		// create the table
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, timestamp INTEGER);";

		try {
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create resume table " + tableName + ": " + e.getMessage());
		}
		return null;
	}

	public void logResume(long timestamp) {
		if (db == null)
			return;

		String tableName = "resume";

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createResumeTable();
		}

		// write to the db
		ContentValues values = new ContentValues();
		values.put("timestamp", timestamp);

		try{
			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write performance string to table " + tableName + ": " + e.getMessage());
		}

	}
	public long countwakeupsleep(String str,long starttimestamp,long endtimestamp) {
		String s;
		int count=0;
		if (db == null)
			return 0;

		String tableName = "alarm_info";
		if (!tableExists(tableName))
			return 0;
		String[] columns = {"timestamp", "entry"};
		 String where = "timestamp BETWEEN '" + Long.toString(starttimestamp) + "' AND '" + Long.toString(endtimestamp) + "'";
		 Cursor c = db.query(tableName, columns, where, null, null, null, null);

		if (c!=null) {
			if (c.moveToFirst()) {
				do {
					s=c.getString(c.getColumnIndex("entry"));
					if(s.equals(str))	count++;
				} while (c.moveToNext());
			}
			c.close();
		}
		return count;
	}
	@Override
	public void logAnything(String tableName, String entry, long timestamp) {
		if (db == null)
			return;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createAnythingTable(tableName);
		}

		// write to the db
		ContentValues values = new ContentValues();
		values.put("timestamp", timestamp);
		values.put("entry", entry);

		try{
			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write generic entry to table " + tableName + ": " + e.getMessage());
		}
	}
	public boolean logAnything4(String tableName, long timestamp, String entry1, String entry2) {
		Date date = new Date(Long.valueOf(entry2));		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		String dateFormatted = formatter.format(date);		Log.mm("", "table="+tableName+" entry1="+entry1+"entry2="+dateFormatted);
		if (db == null)
			return false;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createAnythingTable4(tableName);
		}

		// write to the db
		ContentValues values = new ContentValues();
		values.put("timestamp", timestamp);
		values.put("entry1", entry1);
		values.put("entry2", entry2);

		try{
			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write generic entry to table " + tableName + ": " + e.getMessage());
			return false;
		}
		return true;
	}

	protected Boolean createAnythingTable(String tableName) {
		if (db == null)
			return null;

		// create the table
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, timestamp INTEGER, entry TEXT);";

		try {
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create anything table " + tableName + ": " + e.getMessage());
		}
		return null;
	}
	protected Boolean createAnythingTable4(String tableName) {
		if (db == null)
			return null;

		// create the table
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, timestamp INTEGER, entry1 TEXT, entry2 TEXT);";

		try {
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create anything table " + tableName + ": " + e.getMessage());
		}
		return null;
	}

	// Lab Study LOGGING
	// -------------------------

	/**
	 * Creates a table for logging lab study events.  This should only get called if the table does not exist already.
	 */


	public void logLabStudy_mark(String tableName, String sessionname, String eventname,long starttime,long endtime) {
		if (db == null)
			return;

		// check if the table for this data exists
		if (!tableExists(tableName)) {
			// if not, create the table
			createLabStudy_markTable(tableName);
		}

		// write to the db
		ContentValues values = new ContentValues();
		values.put("sessionname",sessionname);
		values.put("eventname", eventname);
		values.put("starttime", starttime);
		values.put("endtime", endtime);

		try{
			db.insertOrThrow(tableName, null, values);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not write generic entry to table " + tableName + ": " + e.getMessage());
		}
		logLabStudy_mark_to_text_file(sessionname,eventname,starttime,endtime);
	}
	public void logLabStudy_mark_to_text_file(String sessionname, String eventname,long starttime,long endtime)
	{
		try
		{
			fout = new FileOutputStream(labStudyMark,true);
			printStrm = new PrintStream(fout);
			File f=new File(labStudyMark);
			if(f.length()==0)
			{
				String header="sessionname,eventname,starttime,endtime\n";
				printStrm.print(header);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		//long timeStamp = System.currentTimeMillis();
		//java.util.Date d = new java.util.Date(timeStamp);
		//write to the file
		String str=sessionname+","+eventname+","+starttime+','+endtime+"\n";
		try
		{
			printStrm.print(str);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try{
			printStrm.close();
			fout.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	protected Boolean createLabStudy_markTable(String tableName) {
		if (db == null)
			return null;

		// create the table
		String create = "CREATE TABLE " + tableName +
				" (_id INTEGER PRIMARY KEY, sessionname TEXT, eventname TEXT, starttime INTEGER, endtime INTEGER);";

		try {
			db.execSQL(create);
			tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create anything table " + tableName + ": " + e.getMessage());
		}
		return null;
	}
	protected Boolean createValidDataTable(int sensorID) {
		if (db == null)
			return null;

		String tableName = "validdata"+sensorID;

		try {
			// create the table
			String create = "CREATE TABLE " + tableName +
					" (_id INTEGER PRIMARY KEY, timestamps INTEGER, total_minutes INTEGER, valid_minutes INTEGER);";

			db.execSQL(create);

			//		    tableExistsCache.put(tableName, true);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "Could not create ema table " + tableName + ": " + e.getMessage());
		}
		return null;
	}


	/**
	 * Reads data in the DB from the specified sensor.  This enables context inferencing algorithms
	 * to use older data no longer available in the feature/sensor/context buffers.
	 * @param sensorID The unique id of the sensor (from Constants).
	 * @param startTime, endTime The returned cursor will contain data in between (and including) startTime and endTime.
	 */
	public Cursor readValidData(int sensorID, long timestamps) {
		if (db == null)
			return null;

		String tableName = "validdata" + sensorID;
		if (!tableExists(tableName))
			return null;

		String[] columns = {"timestamps", "total_minutes", "valid_minutes"};
		String where = "timestamps= '" + Long.toString(timestamps)+ "'";

		//		String where = "timestamps BETWEEN '" + Long.toString(timestamps) + "' AND '" + Long.toString(timestamps) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);
		return c;
	}
	/*	public Cursor readEMAData(long timestamps) {
		if (db == null)
			return null;

		String tableName = "emacount";
		if (!tableExists(tableName))
			return null;

		String[] columns = {"timestamps", "num_emas", "num_emas_answered"};
		String where = "timestamps= '" + Long.toString(timestamps)+ "'";

//		String where = "timestamps BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		Cursor c = db.query(tableName, columns, where, null, null, null, null);
		return c;
	}
	 */
	public synchronized void logValidData(int sensorID, long timestamps, int total_minutes, int valid_minutes){
		Cursor c;
		if (db == null)
			return;

		String tableName = "validdata" + sensorID;

		if (!tableExists(tableName)) {
			// if not, create the table
			createValidDataTable(sensorID);
		}
		c=this.readValidData(sensorID, timestamps);
		if(c==null || c.getCount()==0){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: DB_P logValidData()"+db+" INSERT");

			ContentValues values = new ContentValues();
			values.put("timestamps",timestamps);
			values.put("total_minutes", total_minutes);
			values.put("valid_minutes", valid_minutes);
			db.insertOrThrow(tableName, null, values);
			if(c!=null) c.close();
		}
		else{
			if (c.moveToFirst()) {
				int curMinutes=c.getInt(c.getColumnIndex("total_minutes"))+total_minutes;
				int validMinutes=c.getInt(c.getColumnIndex("valid_minutes"))+valid_minutes;
				ContentValues values = new ContentValues();
				values.put("timestamps",timestamps);
				values.put("total_minutes", curMinutes);
				values.put("valid_minutes", validMinutes);

				db.update(tableName, values, "timestamps='" + Long.toString(timestamps) + "'", null);
				if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: DB_P logValidData()"+db+" UPDATE");

			}
			c.close();
		}
	}
	/*	public synchronized void logEMAData(long timestamps, int ema_no, int ema_ans){
		Cursor c;
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: DB_P logEMAData()"+db);

		if (db == null)
			return;

		String tableName = "emacount";

		if (!tableExists(tableName)) {
			// if not, create the table
			createEMACountTable();
		}
		c=this.readEMAData(timestamps);
		if(c==null || c.getCount()==0){
			ContentValues values = new ContentValues();
			values.put("timestamps",timestamps);
			values.put("num_emas", ema_no);
			values.put("num_emas_answered", ema_ans);
			db.insertOrThrow(tableName, null, values);
			if(c!=null) c.close();
		}
		else{
			if (c.moveToFirst()) {
				int numEMAs=c.getInt(c.getColumnIndex("num_emas"))+ema_no;
				int numEMAsAnswered=c.getInt(c.getColumnIndex("num_emas_answered"))+ema_ans;
				ContentValues values = new ContentValues();
				values.put("timestamps",timestamps);
				values.put("num_emas", numEMAs);
				values.put("num_emas_answered", numEMAsAnswered);
				db.update(tableName, values, "timestamps='" + Long.toString(timestamps) + "'", null);
			}
			c.close();
		}
	}
	 */	protected Boolean createEMACountTable() {
		 if (db == null)
			 return null;

		 String tableName = "emacount";

		 try {
			 // create the table
			 String create = "CREATE TABLE " + tableName +
					 " (_id INTEGER PRIMARY KEY, timestamps INTEGER, num_emas INTEGER, num_emas_answered INTEGER);";

			 db.execSQL(create);

			 //		    tableExistsCache.put(tableName, true);
		 }
		 catch (SQLiteException e) {
			 Log.e(TAG, "Could not create ema table " + tableName + ": " + e.getMessage());
		 }
		 return null;
	 }
	 public  int getSalivaConfirmation(long startTime, long endTime) {
		 int count=0;
		 if (db == null)
			 return 0;

		 String tableName = "alarm_info";
		 if (!tableExists(tableName))
			 return 0;
		 String[] columns = {"timestamp", "entry"};
		 String where = "timestamp BETWEEN '" + Long.toString(startTime) + "' AND '" + Long.toString(endTime) + "'";
		 Cursor c = db.query(tableName, columns, where, null, null, null, null);
		 if (c!=null) {
			 if (c.moveToFirst()) {
				 do {
					 String str=c.getString(c.getColumnIndex("entry"));
					 if(str.equalsIgnoreCase("Click: Confirm"))
						 count++;
				 } while (c.moveToNext());
			 }
		 }
		 c.close();
		 return count;
	 }

	private final String DATA_QUALITY_TABLE_NAME = "DataQualitySensors";
	public synchronized void createDataQualityTable()
	{
		String create = "CREATE TABLE " + DATA_QUALITY_TABLE_NAME +
				" (_id INTEGER PRIMARY KEY, sensor_type INTEGER, timestamp_from INTEGER, timestamp_to INTEGER, quality INTEGER);";


		db.execSQL(create);
		tableExistsCache.put(DATA_QUALITY_TABLE_NAME, true);

	}

	public synchronized void logDataQuality(int sensorType, long timestampFrom, long timestampTo, int quality){
		if (db == null) {
			return;
		}

		if (!tableExists(this.DATA_QUALITY_TABLE_NAME)) {
			// if not, create the table
			createDataQualityTable();
		}

		ContentValues values = new ContentValues();
		values.put("sensor_type", sensorType);
		values.put("timestamp_from", timestampFrom);
		values.put("timestamp_to", timestampTo);
		values.put("quality", quality);
		db.insertOrThrow(this.DATA_QUALITY_TABLE_NAME, null, values);
	}

	public long getQualityDuration(SensorTypeQualityEnum sensorType, long timestampFrom, long timestampTo, DataQualityColorEnum quality) {
		if (db == null) {
			return 0;
		}

		if (!tableExists(this.DATA_QUALITY_TABLE_NAME)) {
			return 0;
		}
		String sql = "SELECT sum(timestamp_to-timestamp_from) as duration_milli FROM "+this.DATA_QUALITY_TABLE_NAME+" WHERE "
				+" sensor_type="+sensorType.getValue()
				+" AND timestamp_from BETWEEN "+timestampFrom+" AND "+timestampTo+" "
				+" AND timestamp_to BETWEEN "+timestampFrom+" AND "+timestampTo+" "
				+" AND quality="+quality.getValue();
		//Log.h(TAG, sql);
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		long durationMilli = c.getLong(0);
		c.close();
		return durationMilli;
	}
	public long getTotalDuration(SensorTypeQualityEnum sensorType, long timestampFrom, long timestampTo) {
		if (db == null) {
			return 0;
		}

		if (!tableExists(this.DATA_QUALITY_TABLE_NAME)) {
			return 0;
		}
		String sql = "SELECT sum(timestamp_to-timestamp_from) as duration_milli FROM "+this.DATA_QUALITY_TABLE_NAME+" WHERE "
				+" sensor_type="+sensorType.getValue()
				+" AND timestamp_from BETWEEN "+timestampFrom+" AND "+timestampTo+" "
				+" AND timestamp_to BETWEEN "+timestampFrom+" AND "+timestampTo;
		//Log.h(TAG, sql);
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		long durationMilli = c.getLong(0);
		c.close();
		return durationMilli;
	}

};
