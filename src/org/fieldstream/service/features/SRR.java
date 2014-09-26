﻿//Copyright (c) 2010, Carnegie Mellon University
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
//    * Neither the name of Carnegie Mellon University nor the names of its contributors may be used to 
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
/**
 * 
 */
package org.fieldstream.service.features;

import org.fieldstream.service.sensors.api.AbstractFeature;

/**
 * @author Vinay Gunasekaran
 *
 */
public class SRR extends AbstractFeature{

	
	public SRR(int ID, boolean checkUpdate, float thresholdInput) {
		super(ID, checkUpdate, thresholdInput);
	}

	@Override
	public double calculate(int[] buffer, long[] timestamps, int sensor) {
		double thresh  = 0.003; //threshold
		double deltaX = 60; //framerate
		double deltaY = 2;
		int gapSize=15;
		double S = 0, S1 = buffer[0], S2;
		double SRR = 0;
		
		int i;
		double count = deltaY + 1;
		double vector, vector1 = 0;
		int begin=0;
		boolean set = false; //inside a startle
		
		for (i =0; i<buffer.length-1;i++){
			vector = buffer[i+1] - buffer[i];
			
			if (vector > thresh){
				if (count > deltaX){
					count = 0;
					set = true;
					S -= S1;
				}
			}
			if (vector > 0 && vector1 < 0){
				S1 = buffer[i];
			}
			if (vector < 0 && vector1 > 0 && set && count > gapSize){
				set =false;
				S2 = buffer[i];
				S += S2;
				if (S > deltaY){
					SRR++;
				}
				S = 0;
			}
						
			vector1 = vector;
			count ++;
		}
		
		return SRR;
	}

}
