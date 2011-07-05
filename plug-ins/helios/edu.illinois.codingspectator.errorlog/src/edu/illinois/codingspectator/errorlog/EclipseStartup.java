/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.errorlog;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class EclipseStartup implements IStartup {

	/**
	 * @see org.eclipse.ui.internal.views.log.LogView.createPartControl(Composite)
	 */
	@Override
	public void earlyStartup() {
		Platform.addLogListener(new ErrorLogListener());
	}

}
