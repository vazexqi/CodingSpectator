/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.errorlog;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

/**
 * TODO: We need to write the reported error into a file.
 * 
 * Depending on how we resolve issue #149, we might be able to reuse classes
 * EclipseLog and PlatformLogWriter to output the errors into our own file.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class ErrorLogListener implements ILogListener {

	@Override
	public void logging(IStatus status, String plugin) {
		System.err.println("status = " + status);
		System.err.println("stack trace = ");
		status.getException().printStackTrace();
		System.err.println("plugin = " + plugin);
		System.err.println("timestamp = " + System.currentTimeMillis());
	}

}
