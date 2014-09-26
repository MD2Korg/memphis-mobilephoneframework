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
// @author Andrew Raij


package org.fieldstream.gui.ema;

import java.util.HashMap;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.DatabaseLogger;
import org.fieldstream.service.logger.Log;

public class EMABudgeter {

	private static String TAG = "EMABudgeter";


	// overall budgeting
	int totalBudget;
	int remainingBudget;
	long minTimeBeforeNext;
	HashMap<String, Integer> totalItemBudget;
	HashMap<String, Integer> remainingItemBudget;

	DatabaseLogger dataLogger;

	EMABudgeter() {
		remainingItemBudget = new HashMap<String, Integer>();
		totalItemBudget = new HashMap<String, Integer>();
		totalBudget = 0;
		remainingBudget = 0;
		minTimeBeforeNext = 0;
	}

	void setTotalBudget(int totalBudget) {
		Log.d(TAG, "Total EMA budget set to " + totalBudget);
		this.totalBudget = totalBudget;
	}
	void setRemainingBudget(int remainingbudget) {
		Log.d(TAG, "Total EMA budget set to " + totalBudget);
		remainingBudget = remainingbudget;
	}
	void setMinTimeBeforeNext(long minTimeBeforeNext) {
		Log.d(TAG, "At least " + minTimeBeforeNext + "ms before next EMA");
		this.minTimeBeforeNext = minTimeBeforeNext;
	}
	void setTotalItemBudget(String itemDesc, int budget){
		Log.d(TAG, "Adding " + itemDesc + " with budget of " + budget);
		this.totalItemBudget.put(itemDesc, budget);
	}
	void setRemainingItemBudget(String itemDesc, int budget){
		Log.d(TAG, "Adding " + itemDesc + " with budget of " + budget);
		this.remainingItemBudget.put(itemDesc, budget);
	}

	void removeItem(String itemDesc) {
		Log.d(TAG, "Removing " + itemDesc);

		remainingItemBudget.remove(itemDesc);
		totalItemBudget.remove(itemDesc);
	}

	void updateBudget(String itemDesc) {
		if(itemDesc!=null){
			Log.ema_alarm("", "update budget: model="+itemDesc);
			remainingItemBudget.put(itemDesc, remainingItemBudget.get(itemDesc).intValue() - 1);
			if(Integer.valueOf(itemDesc)==Constants.MODEL_COLLECT_SALIVA)
				remainingBudget++;

		}
		remainingBudget--;

		Log.d(TAG, "Updating budget for " + itemDesc + " to " + remainingItemBudget.get(itemDesc));
		Log.d(TAG, "Updating total remaining budget to " + remainingBudget);

	}

	int getremainingBudget(){
		return this.remainingBudget;
	}

	int getremainingItemBudget(String itemDesc){
		int itemBudget = 0;
		if(remainingItemBudget.containsKey(itemDesc))
			itemBudget = remainingItemBudget.get(itemDesc).intValue();

		return itemBudget;
	}

	void removeAllItems() {
		Log.d(TAG, "removing all items from budget");

		remainingItemBudget.clear();
		totalItemBudget.clear();
	}
}
