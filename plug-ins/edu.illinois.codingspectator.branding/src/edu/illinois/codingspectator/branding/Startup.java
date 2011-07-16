/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.branding;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Startup implements IStartup {


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	@Override
	public void earlyStartup() {
		Activator.getDefault().initializeStatusLineUpdater();
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Activator.getDefault().getStatusLineUpdater().updateStatusLine();
			}
		});
	}

}
