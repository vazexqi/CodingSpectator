package edu.illinois.codingspectator.monitor;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.illinois.codingspectator.monitor.prefs.PrefsFacade;
import edu.illinois.codingspectator.monitor.submission.Submitter;
import edu.illinois.codingspectator.monitor.ui.Uploader;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	private static final int UPLOAD_PERIOD_MILLISECONDS= 1000 * 60 * 60 * 24 * 1;

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
		if (shouldUpload()) {
			final Submitter submitter= new Submitter();

			if (Uploader.promptUntilValidCredentialsOrCanceled(submitter)) {
				Uploader.submit(submitter);
			}
		}
	}

	private boolean shouldUpload() {
		return isInTestMode() && enoughTimeHasElapsedSinceLastUpload();
	}

	private boolean isInTestMode() {
		return System.getenv(Messages.Activator_Testing_Mode) == null;
	}

	private boolean enoughTimeHasElapsedSinceLastUpload() {
		try {
			return new Date().getTime() - PrefsFacade.getInstance().getLastUploadTime() > UPLOAD_PERIOD_MILLISECONDS;
		} catch (ParseException e) {
			createErrorStatus("Cannot parse the date that we stored", e);
			return true;
		}
	}

	public static String populateMessageWithPluginName(String formattedString) {
		return MessageFormat.format(formattedString, Messages.WorkbenchPreferencePage_PluginName);
	}

}
