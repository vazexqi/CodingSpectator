/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.branding;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Mohsen Vakilian
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.codingspectator.branding"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleStatusLineUpdater statusLineUpdater;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin= this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		statusLineUpdater.stop();
		plugin= null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public void initializeStatusLineUpdater() {
		statusLineUpdater= new BundleStatusLineUpdater();
		statusLineUpdater.start();
		getPreferenceStore().setDefault(PreferenceKeys.SHOW_IN_STATUS_LINE_KEY, true);
	}

	public BundleStatusLineUpdater getStatusLineUpdater() {
		return statusLineUpdater;
	}

	public void log(IStatus status) {
		getLog().log(status);
	}

	public void logErrorStatus(String message, Exception e) {
		log(new Status(Status.ERROR, PLUGIN_ID, message, e));
	}


}
