/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Added the logging methods.
 * 
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.codingtracker.recording";

	private static Activator plugin;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		plugin= this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		plugin= null;
	}

	public static Activator getDefault() {
		return plugin;
	}

	private ILog getLog() {
		return Platform.getLog(Platform.getBundle(PLUGIN_ID));
	}

	public static Status createInfoStatus(String message) {
		return new Status(Status.INFO, PLUGIN_ID, message);
	}

	public static Status createErrorStatus(String message, Exception e) {
		return new Status(Status.ERROR, PLUGIN_ID, message, e);
	}

	public void log(Status status) {
		getLog().log(status);
	}

}
