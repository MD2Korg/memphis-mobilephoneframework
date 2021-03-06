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
package org.fieldstream.service.logger;

/**
 * logging helper, to be called instead of android.util.log so we can disable
 * logging
 *
 * @author Patrick Blitz
 *
 */

public class Log {

	public static final boolean DEBUG = false;
	public static final boolean I=false;
	public static final boolean DEBUG_MONOWAR=false;
	public static final boolean DEBUG_MONOWAR_NEW=false;
	public static final boolean DEBUG_MONOWAR_EMA_ALARM=false;

	public static final boolean DEBUG_HILLOL=false;
	public static final boolean VERBOSE = false;
//	public static final boolean Cda = true;
	public static final boolean LOG_DB = false;			// will be deleted when framework is stable

	public static void d(String TAG, String logmessage) {
		if (DEBUG) {
			android.util.Log.d(TAG, logmessage);
		}
	}
	public static void m(String TAG, String logmessage) {
		if (DEBUG_MONOWAR) {
			android.util.Log.d(TAG, logmessage);
		}
	}
	public static void mm(String TAG, String logmessage) {
		if (DEBUG_MONOWAR_NEW) {
			android.util.Log.d("mm", "Monowar: "+logmessage);
		}
	}
	public static void ema_alarm(String TAG, String logmessage) {
		if (DEBUG_MONOWAR_EMA_ALARM) {
			android.util.Log.d("EMA_ALARM", "Monowar: "+logmessage);
		}
	}

	public static void h(String TAG, String logmessage) {
		if (DEBUG_HILLOL) {
			android.util.Log.d(TAG, "Hillol: " + logmessage);
		}
	}

	public static void v(String TAG, String logmessage) {
		if (VERBOSE) {
			android.util.Log.v(TAG, logmessage);
		}
	}

	public static void w(String TAG, String logmessage) {
		android.util.Log.w(TAG,logmessage);
	}

	public static void e(String TAG, String logmessage) {
		android.util.Log.e(TAG,logmessage);
	}

	public static void i(String TAG, String logmessage) {
		if(I)
		android.util.Log.i(TAG, logmessage);
	}
}
