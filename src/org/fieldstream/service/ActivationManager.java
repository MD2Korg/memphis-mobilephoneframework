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
package org.fieldstream.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fieldstream.Constants;
import org.fieldstream.service.context.model.ModelCalculation;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;
import org.fieldstream.service.sensor.ContextSubscriber;
import org.fieldstream.service.sensors.api.AbstractFeature;
import org.fieldstream.service.sensors.api.AbstractMote;
import org.fieldstream.service.sensors.api.AbstractSensor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Environment;


/**
 * This Class is the currently combined Feature and Context Manager, managing all context models and their associated features
 *
 * Makes extensive use of the Constants class to identify sensor/feature pairs that are needed.
 * All Features needed by a model are split up in the corresponding feature calculation and the sensor.
 * Depending on this, all necessary features are than started and stopped as needed
 *
 * @author Patrick Blitz
 * @author Andrew Raij
- *
 */

public class ActivationManager implements ContextSubscriber {
/**
 * List that contains all available sensors
 */
	private DatabaseLogger db;

	public static HashMap<Integer,AbstractMote> motes;
	public static HashMap<Integer,AbstractSensor> sensors;
	public static HashMap<Integer,AbstractFeature> features;
	public static HashMap<Integer, ArrayList<Integer>> modelToSFMapping;
	/*
	 * all available and loaded context modules, aka models
	 */
	public static HashMap<Integer,ModelCalculation>  models;
	/**
	 * the map from sensor IDs to Feature Objects needed by the FeatureCalcualtion class to quickly lookup which features need to be calculated on which sensor data
	 */
	public static HashMap<Integer,ArrayList<Integer>> sensorFeature;
/**
 * list of Sensor/Feature Combinations currently in place
 */
	public static ArrayList<Integer> SFlist;
//	private FeatureCalculation featureCalculation;

	static private ActivationManager INSTANCE = null;

	//this was not present in the statemanager before.
	public static ActivationManager getInstance()
	{
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_getInstance()");

		if(INSTANCE == null)
		{
			INSTANCE = new ActivationManager();
		}
		return INSTANCE;
	}

	private ActivationManager() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_Constructor");

		models = new HashMap<Integer, ModelCalculation>();
		modelToSFMapping = new HashMap<Integer, ArrayList<Integer>>();
		SFlist = new ArrayList<Integer>();
		sensors = new HashMap<Integer, AbstractSensor>();
		motes = new HashMap<Integer, AbstractMote>();
		sensorFeature = new HashMap<Integer, ArrayList<Integer>>();
		features = new HashMap<Integer, AbstractFeature>();
//		if(Log.MONOWAR) Log.d("Monowar_ALL","OK    : ActivationManager_Object_Created Time: "+System.currentTimeMillis());
//		if(Log.ERRORDB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", "OK    : ActivationManager_Object_Created", System.currentTimeMillis());}

	}


	private void addFeatures(ArrayList<Integer> newFeatures) {
		if (newFeatures!= null && !newFeatures.isEmpty()) {
			int i = 0;
			HashMap<Integer, ArrayList<AbstractFeature>> newSensorFeature = FeatureCalculation.mapping;
			for (; i< newFeatures.size(); i++) {

				int sensor = Constants.parseSensorId(newFeatures.get(i));
				int feature = Constants.parseFeatureId(newFeatures.get(i));
				if (!features.keySet().contains(feature)) {
					features.put(feature,Factory.featureFactory(feature));
					features.get(feature).active=true;
				}
				if (!sensors.containsKey(sensor)) {
					if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_addfeaure()_sensor:"+sensor);

					sensors.put(sensor, Factory.sensorFactory(sensor));
					sensors.get(sensor).activate();
				}
				// now all needed features and sensors are there, just need to construct the "mapping" array
				if (sensorFeature.containsKey(sensor)) {
					sensorFeature.get(sensor).add(feature); // even if this thing was already there, it would just get added
				} else {
					ArrayList<Integer> tmp = new ArrayList<Integer>();
					tmp.add(feature);
					sensorFeature.put(sensor, tmp);
				}
				// add all new feature/sensor combinations to the mapping list for the feature Calculation
				if (newSensorFeature.containsKey(sensor)) {
					if (!newSensorFeature.get(sensor).contains(features.get(feature))) {
						newSensorFeature.get(sensor).add(features.get(feature));
					}
				} else {
					ArrayList<AbstractFeature> tmp = new ArrayList<AbstractFeature>();
					tmp.add(features.get(feature));
					newSensorFeature.put(sensor, tmp);
				}
			}
//			featureCalculation.setMap(newSensorFeature);
		}
	}
//	/**
//	 * adds a single new feature to the necessary arrays
//	 * @param newFeature a constants feature sensor id
//	 */
//	private void addFeatures(Integer newFeature) {
//		if (newFeature!= null) {
//			HashMap<Integer, ArrayList<AbstractFeature>> newSensorFeature = FeatureCalculation.mapping;
//
//
//				int sensor = Constants.parseSensorId(newFeature);
//				int feature = Constants.parseFeatureId(newFeature);
//				if (!features.keySet().contains(feature)) {
//					features.put(feature,Factory.featureFactory(feature));
//					features.get(feature).active=true;
//				}
//				if (!sensors.containsKey(sensor)) {
//					sensors.put(sensor, Factory.sensorFactory(sensor));
//					sensors.get(sensor).activate();
//				}
//				// now all needed features and sensors are there, just need to construct the "mapping" array
//				if (sensorFeature.containsKey(sensor)) {
//					sensorFeature.get(sensor).add(feature); // even if this thing was already there, it would just get added
//				} else {
//					ArrayList<Integer> tmp = new ArrayList<Integer>();
//					tmp.add(feature);
//					sensorFeature.put(sensor, tmp);
//				}
//				// add all new feature/sensor combinations to the mapping list for the feature Calculation
//				if (newSensorFeature.containsKey(sensor)) {
//					if (!newSensorFeature.get(sensor).contains(features.get(feature))) {
//						newSensorFeature.get(sensor).add(features.get(feature));
//					}
//				} else {
//					ArrayList<AbstractFeature> tmp = new ArrayList<AbstractFeature>();
//					tmp.add(features.get(feature));
//					newSensorFeature.put(sensor, tmp);
//				}
//
//			featureCalculation.setMap(newSensorFeature);
//		}
//	}

	/**
	 *
	 * @param model
	 * @return
	 */
	public ArrayList<Integer> updateFeatureList(int model, ArrayList<Integer> newFeatures) {
		ArrayList<Integer> newSF = new ArrayList<Integer>();
		modelToSFMapping.put(model, newFeatures);
		for (int i = 0;i<newFeatures.size();i++) {
			if (!SFlist.contains(newFeatures.get(i))){
				SFlist.add(newFeatures.get(i));
				newSF.add(newFeatures.get(i));
			}
		}
		if (!newSF.isEmpty()) {
			addFeatures(newSF);
			return newSF;
		}
		return null;
	}

	/**
	 * activate a specific model. Automatically starts/activates all needed sensors and features
	 * @param modelID the IntegerID of a Model as defined in {@link Constants}
	 */
	public void activate(int modelID) {
		try{
		if (!models.containsKey(modelID)) {
			db.logAnything("activation", "activate model"+modelID, System.currentTimeMillis());
			models.put(modelID, Factory.modelFactory(modelID));

			// load sensors for this model
			ArrayList<Integer> newFeatures = updateFeatureList(modelID, models.get(modelID).getUsedFeatures());

			Log.i("StateManager","loading Model "+((Integer)modelID).toString());
		}
		}
		catch(Exception e){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","EXCEPTION\t: (ActivationManager)_activate_model="+modelID+e.getLocalizedMessage());
		}
	}

	public void activateModel(String modelName) {
		try {
			Field field = Constants.class.getField(modelName);
			int modelID = field.getInt(null);
			this.activate(modelID);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void activateMoteStartup(String moteName) {
		try {
			Field field = Constants.class.getField(moteName);
			int moteID = field.getInt(null);
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_activateMote motename="+moteName+" mote ID="+moteID);

			Constants.moteActive[moteID]=true;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * deactivate a specific model. All Features and sensors that are only needed by this model will be turned off/deactivated
	 * @param modelID the IntegerID of a Model as defined in {@link Constants}
	 */
	public void deactivate(int modelID) {
//		ArrayList<Integer> modelsToRemove = new ArrayList<Integer>();

		if (models.containsKey(modelID)) {
			db.logAnything("activation", "deactivate model"+modelID, System.currentTimeMillis());
			// get list of SF to deactivte
			ArrayList<Integer> otherSF  = new ArrayList<Integer>();
			ArrayList<Integer> tosStop = new ArrayList<Integer>();
			for (Integer model : modelToSFMapping.keySet()) {
				if (model!=modelID) {
					otherSF.addAll(modelToSFMapping.get(model));
				}
			}
			for (Integer model : modelToSFMapping.get(modelID)) {
				if (!otherSF.contains(model)) {
					//deactivate this SF
					tosStop.add(model);
				}
			}
			if (!tosStop.isEmpty()) {
				unloadFeatures(tosStop);
			}
			models.remove(modelID);
		}
		if (Log.DEBUG) Log.d("StateManager","unloading Model "+((Integer)modelID).toString());
	}

	public void activateMote(int moteType) {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_activateMote motetype="+moteType+" mote size="+motes.size());
		if(!motes.containsKey(moteType)) {
			motes.put(moteType, Factory.moteFactory(moteType));
			motes.get(moteType).activate();

		}
	}

	public void deactivateMote(int moteType) {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_deactivateMote motetype="+moteType+" mote size="+motes.size());

		if(motes.containsKey(moteType))
		{
			motes.get(moteType).deactivate();
			motes.remove(moteType);
		}
	}

	boolean initialized = false;

	public void init() {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_init()");

		if (initialized)
			return;

//		featureCalculation = FeatureCalculation.getInstance();

		ContextBus.getInstance().subscribe(this);
		db = DatabaseLogger.getInstance(this);

		loadConfigFromFile();

		initialized = true;
	}

	public void destroy() {
		if (!initialized)
			return;

		ContextBus.getInstance().unsubscribe(this);
		DatabaseLogger.releaseInstance(this);
		HashMap<Integer, ModelCalculation> tempModels = new HashMap<Integer, ModelCalculation>();
		tempModels.putAll(models);
		for (int model : tempModels.keySet()) {
			deactivate(model);
		}
	    Iterator k =sensors.keySet().iterator();

	    while(k.hasNext()){
	    	int sensorid=(Integer) k.next();
//	    for (int sensorid:ab){
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_destroy()_sensorid:"+sensorid);
			this.deactivateSensor(sensorid);
		    k =sensors.keySet().iterator();

		}

	    k =motes.keySet().iterator();

	    while(k.hasNext()){
	    	int moteid=(Integer) k.next();
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_destroy()_moteid="+moteid+"size="+motes.size());

//		for (int moteid:motes.keySet()){
			this.deactivateMote(moteid);
		    k =motes.keySet().iterator();

		}

//		featureCalculation.finalize();
//		featureCalculation=null;
		db = null;
		initialized = false;
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_destroy()_end");

	}
	/**
	 * dynamicly add/delete a sensor / feature combination (from a model),<br /> the model has to be loaded already
	 * for this to work.
	 * @param modelID
	 * @param featureSensorID
	 */
	public void addFeature(int modelID, int featureSensorID) {
		if (models.containsKey(modelID)) {
			if (!modelToSFMapping.get(modelID).contains(featureSensorID) ) {
				modelToSFMapping.get(modelID).add(featureSensorID);
				if (!SFlist.contains(featureSensorID) ) {
					SFlist.add(featureSensorID);
					addFeature(modelID, featureSensorID);
				}
			}

		}

	}
//TODO: this functions seems wrong, it unloads all sensors even if they are still needed by other features!
	private void unloadFeatures(ArrayList<Integer> tosStop) {
		if (tosStop!= null && !tosStop.isEmpty()) {
			int i = 0;
			HashMap<Integer, ArrayList<AbstractFeature>> newSensorFeature = FeatureCalculation.mapping;
			for (; i< tosStop.size(); i++) {

				int sensor = Constants.parseSensorId(tosStop.get(i));
				int feature = Constants.parseFeatureId(tosStop.get(i));
				if (features.keySet().contains(feature)) {
					features.remove(feature);
				}

				ArrayList<Integer> sf = sensorFeature.get(sensor);
				if (sf != null) {
					if (sf.size()<2) {
						sensorFeature.remove(sensor);
					} else {
	//					sensorFeature.get(sensor).removeAll(Arrays.asList(new Integer[]{feature}));
						sensorFeature.get(sensor).remove(sensorFeature.get(sensor).indexOf(feature));
					}
				}
				// add all new feature/sensor combinations to the mapping list for the feature Calculation

				if (newSensorFeature.containsKey(sensor)) {
					if (newSensorFeature.get(sensor).size()<2) {
						newSensorFeature.remove(sensor);
					} else {
						newSensorFeature.get(sensor).removeAll(Arrays.asList(new Integer[]{feature}));
					}
				}

				if (sensors.containsKey(sensor) && !sensorFeature.containsKey(sensor)) {
					sensors.get(sensor).deactivate();
					sensors.remove(sensor);
				}

			}
//			featureCalculation.setMap(newSensorFeature);

		}

	}

	/**
	 * used to dynamically activate sensors that are needed, but not direclty by a classifier. Necessary for some Virtual Sensors
	 * @param sensorid
	 */
	public void activateSensor(int sensorid) {
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL","OK\t: (ActivationManager)_activateSensor()_sensorID="+sensorid);
		if (!sensors.containsKey(sensorid)) {
			if (sensorFeature.get(sensorid) == null) {
				sensorFeature.put(sensorid, new ArrayList<Integer>());
			}
			AbstractSensor sensor=Factory.sensorFactory(sensorid);
			if(sensor!=null){
				sensors.put(sensorid, sensor);
				sensors.get(sensorid).activate();
			}
			Log.d("ActivationManager", "activated sensor " + sensorid);
		}
		else {
			Log.d("ActivationManager", "tried to activate sensor " + sensorid + "but already activcated");
		}
	}

    public void deactivateSensor(int sensorid) {
            if (sensors.containsKey(sensorid)) {
                    if (sensorFeature.get(sensorid).isEmpty()) {
                            sensors.get(sensorid).deactivate();
                            sensors.remove(sensorid);
                    }
            }
    }

	public void activateSensor(String sensorName) {
		try {
			Field field = Constants.class.getField(sensorName);
			int sensorID = field.getInt(null);
			this.activateSensor(sensorID);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * publish a new ground truth label (from EMA for example) to the model
	 * @param modelStress
	 * @param newLabel
	 */

	public void publishNewGroundTruth(int modelStress, float newLabel) {
		if (models.containsKey(modelStress)) {
			models.get(modelStress).setLabel(newLabel);
		}
	}


	// code for

	private HashMap<Integer, HashMap<Integer, ArrayList<Integer> > > activationTriggers = new HashMap<Integer, HashMap<Integer, ArrayList<Integer> > >();
	private HashMap<Integer, HashMap<Integer, ArrayList<Integer> > > deactivationTriggers = new HashMap<Integer, HashMap<Integer, ArrayList<Integer> > >();
	private HashMap<Integer, int[]> contextBuffers = new HashMap<Integer, int[]>();
	private HashMap<Integer, Integer> contextBuffersIndex = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> lastContext = new HashMap<Integer, Integer>();



	// MJRTY linear time majority function, from Boyer and Moore
	private int majority(int[] contexts) {
		int maj = -1, cnt = 0;

		for (int i=0; i<contexts.length; i++) {
			// buffer hasn't been filled yet
			if (contexts[i] == -1) {
				return -1;
			}
			if (cnt == 0) {
				maj = contexts[i];
				cnt = 1;
			} else {
				if (contexts[i] == maj) {
					cnt ++;
				} else {
					cnt --;
				}
			}
		}
		return maj;
	}


	public void receiveContext(int modelID, int label, long startTime,
			long endTime) {

		// add the latest label to the context buffer;
		int[] buffer = contextBuffers.get(modelID);
		if (buffer != null) {
			Integer index = contextBuffersIndex.get(modelID);
			buffer[index] = label;
			index++;
			if (index == buffer.length) {
				index = 0;
			}
			contextBuffersIndex.put(modelID, index);

			// compute the majority from the buffer
			int context = majority(buffer);
			if (context != -1 && context != lastContext.get(modelID)) {
				checkActivationRules(modelID, context);
				checkDeactivationRules(modelID, context);
				lastContext.put(modelID, context);
			}
		}
	}

	/**
	 * @param modelID
	 * @param context
	 */
	private void checkDeactivationRules(int modelID, int context) {
		if (deactivationTriggers.containsKey(modelID)) {
			HashMap<Integer, ArrayList<Integer> > modelRules = deactivationTriggers.get(modelID);
			if (modelRules.containsKey(context)) {
				ArrayList<Integer> rules = modelRules.get(context);

				for (Integer modelToDeactivate : rules) {
					Log.d("ActivationManager", "Deactivating model " + modelToDeactivate);
					this.deactivate(modelToDeactivate);
				}
			}
		}
	}

	/**
	 * @param modelID
	 * @param context
	 */
	private void checkActivationRules(int modelID, int context) {
		if (activationTriggers.containsKey(modelID)) {
			HashMap<Integer, ArrayList<Integer> > modelRules = activationTriggers.get(modelID);
			if (modelRules.containsKey(context)) {
				ArrayList<Integer> rules = modelRules.get(context);

				for (Integer modelToActivate : rules) {
					Log.d("ActivationManager", "Activating model " + modelToActivate);
					this.activate(modelToActivate);
				}
			}
		}
	}


	void loadConfigFromFile() {
		File root = Environment.getExternalStorageDirectory();

		File dir = new File(root+"/"+Constants.CONFIG_DIR);
		dir.mkdirs();

		File setupFile = new File(dir, Constants.ACTIVATION_CONFIG_FILENAME);
		if (!setupFile.exists())
			return;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Document dom = null;
		try {
			dom = builder.parse(setupFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Element xmlroot = dom.getDocumentElement();

        NodeList nodeList = xmlroot.getElementsByTagName("startup");
        if (nodeList.getLength() > 0) {
        	Element element = (Element)nodeList.item(0);
        	loadStartupConfig(element);
        }

        nodeList = xmlroot.getElementsByTagName("runtime");
        if (nodeList.getLength() > 0) {
        	Element element = (Element)nodeList.item(0);
        	loadRuntimeRules(element);
        }

        printActiveModels();
        printActiveSensors();
        printRuntimeRules();
	}

	void loadStartupConfig(Element startupElement) {
		// load models
		for(int i=0;i<Constants.MOTE_NO;i++)
			Constants.moteActive[i]=false;
        NodeList nodeList = startupElement.getElementsByTagName("mote");
        for (int i=0; i < nodeList.getLength(); i++) {
        	Element element = (Element)nodeList.item(i);
        	String moteToActivate = element.getFirstChild().getNodeValue();
        	this.activateMoteStartup(moteToActivate);
        }

		nodeList = startupElement.getElementsByTagName("model");
        for (int i=0; i < nodeList.getLength(); i++) {
        	Element element = (Element)nodeList.item(i);
        	String modelToActivate = element.getFirstChild().getNodeValue();
        	this.activateModel(modelToActivate);
        }

		// load sensors
        nodeList = startupElement.getElementsByTagName("sensor");
        for (int i=0; i < nodeList.getLength(); i++) {
        	Element element = (Element)nodeList.item(i);
        	String sensorToActivate = element.getFirstChild().getNodeValue();
        	this.activateSensor(sensorToActivate);
        }
	}


	void loadRuntimeRules(Element runtimeRules) {

        NodeList ruleList = runtimeRules.getElementsByTagName("trigger");
        for (int i=0; i < ruleList.getLength(); i++) {
        	Element rule = (Element)ruleList.item(i);

        	// extract the model and output that triggers an action
        	String modelTrigger = rule.getAttribute("model");
        	String outputTrigger = rule.getAttribute("output");
        	String majorityBufferSize = rule.getAttribute("majority_buffer_size");

        	// extract the actions triggered by model and output
        	NodeList actions = rule.getChildNodes();
        	for (int j=0; j < actions.getLength(); j++) {
        		Node action = actions.item(j);

        		if (action.getNodeName().equals("activate")) {
        			String modelToActivate = action.getFirstChild().getNodeValue();
        			addActivationRule(modelTrigger, outputTrigger, modelToActivate, majorityBufferSize, true);
        		}
        		else if (action.getNodeName().equals("deactivate")) {
        			String modelToDeactivate = action.getFirstChild().getNodeValue();
        			addActivationRule(modelTrigger, outputTrigger, modelToDeactivate, majorityBufferSize, false);
        		}
        		else {
        			//ignore #text
        		}
        	}
        }
	}

	private void addActivationRule(int modelTrigger, int outputTrigger,
			int modelToActivate, boolean activate) {

		HashMap<Integer, HashMap<Integer, ArrayList<Integer> > > triggers = null;

		if (activate) {
			triggers = activationTriggers;
		}
		else {
			triggers = deactivationTriggers;
		}

		HashMap<Integer, ArrayList<Integer> > outputTriggers = triggers.get(modelTrigger);
		if (outputTriggers == null) {
			outputTriggers = new HashMap<Integer, ArrayList<Integer> >();
			triggers.put(modelTrigger, outputTriggers);
		}

		ArrayList<Integer> modelsToActivate = outputTriggers.get(outputTrigger);
		if (modelsToActivate == null) {
			modelsToActivate = new ArrayList<Integer>();
			outputTriggers.put(outputTrigger, modelsToActivate);
		}

		if (!modelsToActivate.contains(modelToActivate)) {
			modelsToActivate.add(modelToActivate);
		}


	}

	private void addActivationRule(String mt, String ot,
			String mta, String mbs, boolean activate) {

		int modelTrigger=-1,modelToActivate=-1, outputTrigger=-1, majBufferSize=5; //default size is 5
		try {
			modelTrigger = Constants.class.getField(mt).getInt(null);
			modelToActivate = Constants.class.getField(mta).getInt(null);
			outputTrigger = Integer.parseInt(ot);
			majBufferSize = Integer.parseInt(mbs);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if (modelTrigger == -1 || outputTrigger == -1 || modelToActivate == -1)
			return;

		// if this is the first time we've seen this model as a trigger
		if (!lastContext.containsKey(modelTrigger)) {
			lastContext.put(modelTrigger, -1);
			int[] buffer = new int[majBufferSize];
			Arrays.fill(buffer, -1);
			contextBuffers.put(modelTrigger, buffer);
			contextBuffersIndex.put(modelTrigger, 0);
		}

		addActivationRule(modelTrigger, outputTrigger, modelToActivate, activate);

	}


	void printActiveModels() {
		for (int m : models.keySet()) {
			Log.d("ActivationManager", "model " + m + " active");
		}
	}

	void printActiveSensors() {
		for (int s : sensors.keySet()) {
			Log.d("ActivationManager", "sensor " + s + " active");
		}
	}

	void printRuntimeRules() {
		for (int triggerModel : activationTriggers.keySet()) {
			for (int triggerOutput : activationTriggers.get(triggerModel).keySet()) {
				String str = "Activate [ ";
				for (int action : activationTriggers.get(triggerModel).get(triggerOutput)) {
					str += action + " ";
				}
				str += "] when " + triggerModel + " produces " + triggerOutput;

				Log.d("ActivationManager", str);
			}
		}

		for (int triggerModel : deactivationTriggers.keySet()) {
			for (int triggerOutput : deactivationTriggers.get(triggerModel).keySet()) {
				String str = "Deactivate [";
				for (int action : deactivationTriggers.get(triggerModel).get(triggerOutput)) {
					str += action + " ";
				}
				str += "] when " + triggerModel + " produces " + triggerOutput;
				Log.d("ActivationManager", str);
			}
		}
	}
}
