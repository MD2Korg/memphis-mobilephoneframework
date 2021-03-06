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
package org.fieldstream.service.sensor.virtual;
import java.util.Arrays;

import org.fieldstream.service.logger.Log;

public class AccelCommutingCalculation{
	// ===============================================
	private static final String TAG="AccelCommutingCalculation";
	private static final double PERCENTILE=0.8;

	private static final int STATIC=0;
	private static final int MOVING=1;

	private static double currentvar;
	private static double prctilevar=0;
	private static double maxvar=0;

	private static double currentprctile=0;
	private static double prctileprctile=0;
	private static double maxprctile=0;

	private static double[] varBuff;
	private static int varHead=0;

	private static double[] prctileBuff;
	private static int prctileHead;

	private static double mean=0;
	private static double meancrossings=0;

	private static double[] meanBuff;
	private static int meanHead=0;

	private static int valuecount=0;
	// ===============================================
	public AccelCommutingCalculation(){
		varBuff=new double[4];
		prctileBuff=new double[4];
		meanBuff=new double[4];
		varHead=0;
		prctileHead=0;
		meanHead=0;
	}

	public void print(){
		if(Log.DEBUG){
//			Log.d(TAG,"currentvar="+currentvar+"|prctilevar="+prctilevar+"|maxprctile="+maxvar);
			Log.d(TAG,"==============================================");
			Log.d(TAG,"currentprctile="+currentprctile+"|maxprctile="+maxprctile+"|mean="+mean+"|meancrossings="+meancrossings);
			Log.d(TAG,"ratio="+currentprctile/maxprctile);
			Log.d(TAG,"==============================================");
		}
	}

	// ============================================
	// VARIANCE
	// ============================================
/*	private void computeVar(int[] buffer){
		if(buffer.length==0) currentvar=-1;
		double mean=0;
		for(int i=0;i<buffer.length;i++) mean+=buffer[i];
		mean=mean/buffer.length;
		currentvar=0;
		for(int i=0;i<buffer.length;i++) currentvar+=(buffer[i]-mean)*(buffer[i]-mean);
		currentvar=currentvar/buffer.length;
	}

	private void computePrctileVar(){
		double[] tmpBuff=varBuff;
		Arrays.sort(tmpBuff);
		int P=(int)(PERCENTILE*varBuff.length);
		prctilevar=varBuff[P];
	}

	private void computeMaxVar(){
		maxvar=(prctilevar>maxvar)?prctilevar:maxvar;
	}

	private void pushVarToBuffer(){
		varBuff[varHead++]=currentvar;
	}
*/

	// ============================================
	// PERCENTILE
	// ============================================
	private void computeValueCount(int[] buffer){
		int[] s=buffer;
		Arrays.sort(s);
		if(buffer.length<2){
			valuecount=buffer.length;
		}else{
			valuecount=1;
			for(int i=1;i<buffer.length;i++) if(buffer[i]>buffer[i-1]) valuecount++;
		}
	}

	private void computePrctile(int[] buffer){
		int[] s=buffer;
		Arrays.sort(s);
		int P1=(int) Math.floor(0.05*buffer.length);
		int P2=(int) Math.floor(0.95*buffer.length);
		currentprctile=buffer[P2]-buffer[P1];
	}

	private void computePrctilePrctile(){
		double[] tmpBuff=prctileBuff;
		Arrays.sort(prctileBuff);
		int P=(int) (PERCENTILE*prctileBuff.length);
		prctileprctile=prctileBuff[P];
	}

	private void computeMaxPrctile(){
//		maxprctile=(prctileprctile>maxprctile)?prctileprctile:maxprctile;
		maxprctile=(currentprctile>maxprctile)?currentprctile:maxprctile;
	}

	private void pushPrctileToBuffer(){
		prctileBuff[prctileHead++]=currentprctile;
	}

	// ============================================
	// MEANCROSSINGS
	// ============================================
	private void computeMean(int[] buffer){
		mean=0;
		for(int i=0;i<buffer.length;i++) mean+=buffer[i];
		mean=mean/buffer.length;
	}

	private void computeMeanCrossings(int[] buffer){
		meancrossings=0;
		for(int i=0;i<buffer.length-1;i++){
			double b0=(double)buffer[i];
			double b1=(double)buffer[i+1];
			if((b0>=mean & b1<=mean)|(b0<=mean & b1>=mean)) meancrossings++;
		}
	}

	// ============================================
	// CALCULATE
	// ============================================
	public int calculate(int[] buffer){
		if(buffer.length==0) return 0;
		computeMean(buffer);
		computeMeanCrossings(buffer);
		computePrctile(buffer);
		computeValueCount(buffer);
		if(prctileHead==prctileBuff.length-1){
			computePrctilePrctile();
			computeMaxPrctile();
			prctileHead=0;
		}else{
			pushPrctileToBuffer();
		}
//		if(Log.DEBUG) Log.d(TAG,"currentprctile="+currentprctile+"|maxprctile"+maxprctile+"|ratio="+currentprctile/maxprctile);
		print();
		boolean obviouslystatic=(valuecount<10);
		if(obviouslystatic) return STATIC;
		boolean strongmovement=(currentprctile>0.02*maxprctile);
		boolean highfrequency=(meancrossings>10);
		if(strongmovement && highfrequency) return MOVING; else return STATIC;
	}
}
