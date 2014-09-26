package org.fieldstream.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CommonUtil {
	
	public static String getExceptionStackTrace(Exception e) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		return writer.toString();
	}
	
	public static int TwosComplement(int x, int nBits) {
		int msb = x>>(nBits-1);
		if(msb==1) {
			return -1*( (~x & ((1<<nBits)-1) ) +1);
			//return -1*( (~x & 0xFFF) +1);
		} else {
			return x;
		}
	}
}
