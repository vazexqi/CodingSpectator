/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatePlugin;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.PreferenceConstants;
import org.eclipse.ltk.core.refactoring.codingspectator.RunningModes;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

/**
 * The activator class controls the plug-in life cycle. The "restriction" warning has to be
 * suppressed because we are calling methods in the AutoUpdatePlugin to set the preference for
 * checking for updates in the user's workspace.
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin implements IStartup {

	private static final int UPLOAD_PERIOD_MILLISECONDS= 1000 * 60 * 60 * 24 * 1;

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.codingspectator.monitor.core.ui"; //$NON-NLS-1$

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
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin= this;

		enableAutomaticCheckForUpdatesPreference();
	}

	private void enableAutomaticCheckForUpdatesPreference() {
		if (forcedAutoUpdatePrefHasNeverBeenSet()) {
			AutomaticUpdatePlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.PREF_AUTO_UPDATE_ENABLED, true);
			AutomaticUpdatePlugin.getDefault().savePreferences();
			setForcedAutoUpdatePref();
		}
	}

	private void setForcedAutoUpdatePref() {
		PrefsFacade prefsFacade= PrefsFacade.getInstance();
		prefsFacade.setForcedAutoUpdatePref(true);
	}

	private boolean forcedAutoUpdatePrefHasNeverBeenSet() {
		PrefsFacade prefsFacade= PrefsFacade.getInstance();
		return prefsFacade.getForcedAutoUpdatePref() == false;
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
		return !Platform.inDevelopmentMode() && !RunningModes.isInTestMode() && enoughTimeHasElapsedSinceLastUpload();
	}

	private boolean enoughTimeHasElapsedSinceLastUpload() {
		try {
			return new Date().getTime() - PrefsFacade.getInstance().getLastUploadTime() > UPLOAD_PERIOD_MILLISECONDS;
		} catch (ParseException e) {
			createErrorStatus("Cannot parse the date that we stored", e);
			return true;
		}
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

	public static String populateMessageWithPluginName(String formattedString) {
		return MessageFormat.format(formattedString, Messages.WorkbenchPreferencePage_PluginName);
	}


}
