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
package org.fieldstream.service.sensors.api;

import org.fieldstream.Constants;
import org.fieldstream.service.logger.Log;

public abstract class AbstractMote {

	private static final String TAG = "AbstractMote";

	/*This is the mote type
	 * AutoSense Versions 1 ECG and so on
	 * mote types come from the constant file
	 */
	public int MoteType;

	/*
	 * This is the mote id
	 * that is reeived from the actual
	 * physical mote
	 */
	public int MoteID;



	protected AbstractMote INSTANCE;

	/*
	 * Every mote either has a device address
	 * or a bridge address
	 */

	public String DeviceAddress;


	/*
	 * A standard constructor based on the moteype
	 * and the device Address
	 */
	public AbstractMote(int moteType)
	{
		MoteType = moteType;
		INSTANCE = this;

		Log.d(TAG + Constants.getMoteDescription(moteType), "Created");

	}

	/*
	 * This constructor should be used if
	 * device Address
	 */

	public AbstractMote(int moteType, String deviceAddress)
	{
		MoteType = moteType;
		DeviceAddress = deviceAddress;

		INSTANCE = this;

		Log.d("AbstractMote - " + Constants.getMoteDescription(moteType), "Created");
	}


	public abstract void initialize();

	public abstract void finalize();

	public abstract void activate();

	public abstract void deactivate();

	public abstract void sendCommand(int command);

	public abstract void sendCommand(String command);




}
