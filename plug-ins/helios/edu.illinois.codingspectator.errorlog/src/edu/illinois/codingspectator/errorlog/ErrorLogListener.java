/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.errorlog;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.internal.runtime.PlatformLogWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.adaptor.EclipseLog;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;

/**
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ErrorLogListener extends PlatformLogWriter {

	private static final SafeRecorder safeRecorder= new SafeRecorder("error.log");

	public ErrorLogListener() {
		super(null);
	}

	public synchronized void logging(IStatus status, String plugin) {
		Writer writer= new StringWriter();
		new EclipseLog(writer).log(getLog(status));
		safeRecorder.record(writer.toString());
	}

}
