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
package org.fieldstream.service.logger;

// Class for logging exceptions.
// Borrowed from http://jyro.blogspot.com/2009/09/crash-report-for-android-app.html

import android.app.Activity;
import android.content.Context;
import java.io.*;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler defaultUEH;

	private Activity app = null;

	public TopExceptionHandler(Activity app) {
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		this.app = app;
	}

	public void uncaughtException(Thread t, Throwable e)
	{
		StackTraceElement[] arr = e.getStackTrace();
		String report = e.toString()+"\n\n";
		report += "--------- Stack trace ---------\n\n";
		for (int i=0; i<arr.length; i++)
		{
			report += "    "+arr[i].toString()+"\n";
		}
		report += "-------------------------------\n\n";

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		report += "--------- Cause ---------\n\n";
		Throwable cause = e.getCause();
		if(cause != null) {
			report += cause.toString() + "\n\n";
			arr = cause.getStackTrace();
			for (int i=0; i<arr.length; i++)
			{
				report += "    "+arr[i].toString()+"\n";
			}
		}
		try {
			FileOutputStream trace = app.openFileOutput(
					"stack.trace", Context.MODE_APPEND);
			trace.write(report.getBytes());
			trace.close();
		} catch(IOException ioe) {
			// ...
		}

		defaultUEH.uncaughtException(t, e);
	}
}
