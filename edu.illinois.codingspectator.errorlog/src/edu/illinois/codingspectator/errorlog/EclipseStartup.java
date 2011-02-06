/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.errorlog;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class EclipseStartup implements IStartup {

	@Override
	public void earlyStartup() {
		ILog log = ResourcesPlugin.getPlugin().getLog();
		log.addLogListener(new ErrorLogListener());
		
	}

}
