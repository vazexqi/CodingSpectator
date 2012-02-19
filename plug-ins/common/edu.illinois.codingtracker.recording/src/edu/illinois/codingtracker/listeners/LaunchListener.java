/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.Messages;

/**
 * 
 * @author Stas Negara
 * 
 */
public class LaunchListener extends BasicListener implements ILaunchListener {

	public static void register() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new LaunchListener());
	}

	@Override
	public void launchAdded(ILaunch launch) {
		try {
			String launchMode= launch.getLaunchMode();
			ILaunchConfiguration launchConfiguration= launch.getLaunchConfiguration();
			String launchName= launchConfiguration.getName();
			String application= launchConfiguration.getAttribute("application", "");
			String product= launchConfiguration.getAttribute("product", "");
			boolean useProduct= launchConfiguration.getAttribute("useProduct", false);
			operationRecorder.recordLaunchedApplication(launchMode, launchName, application, product, useProduct);
		} catch (CoreException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_LaunchConfigurationException);
		}
	}

	@Override
	public void launchRemoved(ILaunch launch) {
		//do nothing
	}

	@Override
	public void launchChanged(ILaunch launch) {
		//do nothing
	}

}
