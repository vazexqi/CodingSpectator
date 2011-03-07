/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.data;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class CodingSpectatorDataPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.codingspectator.data"; //$NON-NLS-1$

	// The shared instance
	private static CodingSpectatorDataPlugin plugin;

	/**
	 * The constructor
	 */
	public CodingSpectatorDataPlugin() {
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
		plugin= null;
		super.stop(context);
	}

	/**
	 * @return the shared instance
	 */
	public static CodingSpectatorDataPlugin getDefault() {
		return plugin;
	}

	/**
	 * @return The default storage location
	 */
	public static IPath getStorageLocation() {
		return getDefault().getStateLocation();
	}

	public static Version getCodingSpectatorVersion() {
		return Platform.getBundle(PLUGIN_ID).getVersion();
	}

	public static IPath getVersionedStorageLocation() {
		return getStorageLocation().append(getCodingSpectatorVersion().toString());
	}

}
