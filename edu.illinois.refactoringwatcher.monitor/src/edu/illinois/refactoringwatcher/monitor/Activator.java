package edu.illinois.refactoringwatcher.monitor;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.illinois.refactoringwatcher.monitor.submission.Submitter;
import edu.illinois.refactoringwatcher.monitor.ui.Uploader;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.refactoringwatcher.monitor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin= this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
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

	public Status createInfoStatus(String message) {
		return new Status(Status.INFO, PLUGIN_ID, message);
	}

	public Status createErrorStatus(String message, Exception e) {
		return new Status(Status.ERROR, PLUGIN_ID, message, e);
	}

	public void log(Status status) {
		getLog().log(status);
	}

	@Override
	public void earlyStartup() {
		if (System.getenv(Messages.Activator_Testing_Mode) == null) {
			final Submitter submitter= new Submitter();

			if (Uploader.initializeUntilValidCredentials(submitter)) {
				Uploader.submit(submitter);
			}
		}
	}

	public static String populateMessageWithPluginName(String formattedString) {
		return MessageFormat.format(formattedString, Messages.WorkbenchPreferencePage_PluginName);
	}

}
