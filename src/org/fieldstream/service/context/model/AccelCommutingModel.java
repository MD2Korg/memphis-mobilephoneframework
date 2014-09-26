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
package org.fieldstream.service.context.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;
import org.fieldstream.service.sensor.ContextBus;

public class AccelCommutingModel extends ModelCalculation{
	private static final String TAG="AccelCommutingModel";
	private static double currentMotion=0;
	private ArrayList<Integer> featureLabels;

	public AccelCommutingModel(){
		if(Log.DEBUG) Log.d(TAG,"Created");
		featureLabels=new ArrayList<Integer>();
		featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_ACCELCOMMUTING));
		//featureLabels.add(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_ACCELPHONEZ));
	}

	protected void finalize(){}
	public int getID(){return Constants.MODEL_ACCELCOMMUTING;}
	public int getCurrentClassifier(){return (int) currentMotion;}
	public ArrayList<Integer> getUsedFeatures(){return featureLabels;}
	@Override
	public void computeContext(FeatureSet fs) {
		if(Log.DEBUG) Log.d(TAG,"Sent one decision");
		currentMotion=fs.getFeature(Constants.getId(Constants.FEATURE_MAX, Constants.SENSOR_VIRTUAL_ACCELCOMMUTING));
		ContextBus.getInstance().pushNewContext(getID(), (int) currentMotion, fs.getBeginTime(), fs.getEndTime());
	}
//	@Override
	public HashMap<Integer, String> outputdescription = new HashMap<Integer, String>() {
		{
			put(0,"Not moving");
			put(1,"Moving");
		}
	};

	public HashMap<Integer, String> getOutputDescription() {
		return outputdescription;
	}
}
