package edu.illinois.refactorbehavior.monitor;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.refactorbehavior.monitor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ScopedPreferenceStore preferenceStore;

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

	public IPreferenceStore getPreferenceStore() {
		// Create the preference store lazily.
		if (preferenceStore == null) {
			preferenceStore= new ScopedPreferenceStore(new ConfigurationScope(), getBundle().getSymbolicName());

		}
		return preferenceStore;
	}

}
