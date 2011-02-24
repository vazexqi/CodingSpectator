/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.errorlog;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;

/**
 * TODO: We need to write the reported error into a file.
 * 
 * Depending on how we resolve issue #149, we might be able to reuse classes EclipseLog and
 * PlatformLogWriter to output the errors into our own file.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * @author Stas Negara
 * 
 */
public class ErrorLogListener implements ILogListener {

	private static final SafeRecorder safeRecorder= new SafeRecorder("error-log.txt");

	@Override
	public void logging(IStatus status, String plugin) {
		safeRecorder.record("status = " + status + "\n");
//		safeRecorder.record("stack trace = " + "\n");
//		status.getException().printStackTrace();
		safeRecorder.record("plugin = " + plugin + "\n");
		safeRecorder.record("timestamp = " + System.currentTimeMillis() + "\n");
	}
}
