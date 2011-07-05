/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.updates;

import edu.illinois.bundleupdater.BundleUpdater;
import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;

/**
 * @author Mohsen Vakilian
 * 
 */
public class Updater implements SubmitterListener {

	/* (non-Javadoc)
	 * @see edu.illinois.codingspectator.monitor.core.submission.SubmitterListener#preSubmit()
	 */
	@Override
	public void preSubmit() {
	}

	/* (non-Javadoc)
	 * @see edu.illinois.codingspectator.monitor.core.submission.SubmitterListener#postSubmit(boolean)
	 */
	@Override
	public void postSubmit(boolean succeeded) {
		new BundleUpdater("http://codingspectator.cs.illinois.edu/updates/", "edu.illinois.codingspectator.feature.group").checkForUpdates();
	}
}
