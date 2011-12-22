/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ExceptionUtil {

	public static String getStackTrace(Throwable throwable) {
		final Writer result= new StringWriter();
		final PrintWriter printWriter= new PrintWriter(result);
		throwable.printStackTrace(printWriter);
		return result.toString();
	}

}
