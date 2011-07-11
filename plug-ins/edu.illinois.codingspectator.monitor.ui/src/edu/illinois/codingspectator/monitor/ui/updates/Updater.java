/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.updates;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import edu.illinois.bundleupdater.Activator;
import edu.illinois.bundleupdater.BundleUpdater;
import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;

/**
 * @author Mohsen Vakilian
 * 
 */
public class Updater implements SubmitterListener {

	@Override
	public void preLock() {
	}

	@Override
	public void preSubmit() {
	}

	@Override
	public void postSubmit(boolean succeeded) {
		if (isEclipseVersionIsSupported()) {
			new BundleUpdater(getUpdateSiteURL(), "edu.illinois.codingspectator.feature.group").checkForUpdates();
		} else {
			try {
				Assert.isTrue(false);
			} catch (AssertionFailedException e) {
				Activator.getDefault().logErrorStatus("Could not detect the Eclipse release name while trying to find the update site.", e);
			}
		}
	}

	private boolean isEclipseVersionIsSupported() {
		return isHelios() || isIndigo();
	}

	private String getUpdateSiteURL() {
		String baseURL= "http://codingspectator.cs.illinois.edu/updates/";
		if (isHelios()) {
			return baseURL + "helios";
		} else if (isIndigo()) {
			return baseURL + "indigo";
		} else {
			throw new RuntimeException("Unsupported Eclipse detected.");
		}
	}

	/**
	 * See org.eclipse.ui.internal.ProductInfo#getAppVersion()
	 * 
	 * @return
	 */
	private Version getEclipseVersion() {
		Bundle bundle= Platform.getBundle("org.eclipse.ui");
		if (bundle == null) {
			return Version.emptyVersion;
		} else {
			return bundle.getVersion();
		}
	}

	private boolean isHelios() {
		return getEclipseVersion().getMajor() == 3 && getEclipseVersion().getMinor() == 6;
	}

	private boolean isIndigo() {
		return getEclipseVersion().getMajor() == 3 && getEclipseVersion().getMinor() == 7;
	}

}
