/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */

package edu.illinois.codingspectator.errorlog;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

/**
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
