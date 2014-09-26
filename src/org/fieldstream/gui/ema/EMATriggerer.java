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

// @author Mishfaq Ahmed


package org.fieldstream.gui.ema;

import java.util.HashMap;

import org.fieldstream.gui.ema.InterviewScheduler.Model;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;

import android.database.Cursor;


public class EMATriggerer {
	EMABudgeter budgeter=null;
	HashMap<String, Integer> estimates=null;
	HashMap<String, Integer> numberobservedtoday = null;
	DatabaseLogger db;
	
	EMATriggerer(EMABudgeter budgeter,Model []models,long dayStartTime,long dayEndTime){
		this.budgeter = budgeter;
		int events;
		///////////// pop from database
		estimates = new HashMap<String, Integer>();
		numberobservedtoday = new HashMap<String, Integer>();
		db=DatabaseLogger.getInstance(this);
		for(int i=0;i<models.length;i++){
			estimates.put(Integer.toString(models[i].getModelID()), models[i].getExpectedNumber());
			events=db.getNumReportObserved(models[i].getModelID(),dayStartTime,dayEndTime);
			numberobservedtoday.put(Integer.toString(models[i].getModelID()),events);
		}
	}

	public boolean trigger(int modelID, long currentTimeMillis) {
		Log.d("Triggering","Entering the EMATrigger::trigger for model"+modelID);

		if (!numberobservedtoday.containsKey(""+modelID)) {
			numberobservedtoday.put(""+modelID, 0);
		}
		if (!estimates.containsKey(""+modelID)) {
			estimates.put(""+modelID, 0);
		}
		
		numberobservedtoday.put(""+modelID, numberobservedtoday.get(""+modelID)+1);			
		int estimation = estimates.get(""+modelID);

		
		String itemDesc = ""+modelID;
		
		int budgetleft = budgeter.getremainingItemBudget(itemDesc);

		String tt="OK\t: (EMATrigger)_trigger() modelID="+modelID+", numberobservedtoday="+numberobservedtoday.get(""+modelID)+
				" estimation="+estimation+" budgetleft="+budgetleft;
		if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL",tt);
		if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", tt,System.currentTimeMillis());}	
		
		
		
		if(budgetleft<=0) return  false;
		int observedtoday=numberobservedtoday.get(""+modelID);
		
		if(estimation<=observedtoday) return true;
		float probability = (float)budgetleft/((float)estimation-(float)observedtoday);
		if(probability > 1)
			probability = 1;

		
		
		if((float)Math.random()<probability){
			 tt="OK\t: (EMATrigger)_trigger() modelID="+modelID+", numberobservedtoday="+numberobservedtoday.get(""+modelID)+
					" estimation="+estimation+" budgetleft="+budgetleft+" probability="+probability+" YES";
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL",tt);
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", tt,System.currentTimeMillis());}	
			
		Log.d("Triggering with probability",""+probability);
		return true;
		}
//		
		
		
		// check what is the estimated number of events for this type in the current day.
		// check what is the remaining budget for this model.
		// check remaining time of the day.
		// check what is the budget of current day.
		// based on this values calculate the probabilities of returning true.
		
		else
		{
			 tt="OK\t: (EMATrigger)_trigger() modelID="+modelID+", numberobservedtoday="+numberobservedtoday.get(""+modelID)+
					" estimation="+estimation+" budgetleft="+budgetleft+" probability="+probability+" NO";
			if(Log.DEBUG_MONOWAR) Log.m("Monowar_ALL",tt);
			if(Log.LOG_DB){DatabaseLogger db=DatabaseLogger.getInstance(this);db.logAnything("DEBUG", tt,System.currentTimeMillis());}	

			Log.d("not triggered although the probability was",""+probability);
			return false;
		}
	}
	
}
