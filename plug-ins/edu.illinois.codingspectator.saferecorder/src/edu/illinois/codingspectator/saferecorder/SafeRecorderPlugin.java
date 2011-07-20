/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.saferecorder;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class SafeRecorderPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.codingspectator.saferecorder"; //$NON-NLS-1$

	// The shared instance
	private static SafeRecorderPlugin plugin;


	public SafeRecorderPlugin() {

	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin= this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		plugin= null;
		super.stop(bundleContext);
	}

	/**
	 * @return the shared instance
	 */
	public static SafeRecorderPlugin getDefault() {
		return plugin;
	}

	/**
	 * @return The default storage location
	 */
	public static IPath getStorageLocation() {
		return getDefault().getStateLocation();
	}

	public static IPath getVersionedStorageLocation() {
		return getStorageLocation().append(CodingSpectatorDataPlugin.getCodingSpectatorVersion().toString());
	}

}
