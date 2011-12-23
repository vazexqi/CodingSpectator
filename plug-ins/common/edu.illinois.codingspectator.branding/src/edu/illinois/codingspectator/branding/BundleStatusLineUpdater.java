/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.branding;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * @author Mohsen Vakilian
 * 
 */
public class BundleStatusLineUpdater {

	private final BundleStatusLineManager bundleStatusLineManager= new BundleStatusLineManager();

	private final IPropertyChangeListener propertyChangeListener= new IPropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
			if (PreferenceKeys.SHOW_IN_STATUS_LINE_KEY.equals(event.getProperty())) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						updateStatusLine((Boolean)event.getNewValue());
					};
				});
			}
		}
	};

	public void start() {
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	public void stop() {
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
	}

	private void updateStatusLine(boolean showInStatusLine) {
		if (showInStatusLine) {
			bundleStatusLineManager.addLogoToStatusLineIfNecessary();
		} else {
			bundleStatusLineManager.removeLogoFromStatusLine();
		}
	}

	public void updateStatusLine() {
		boolean showInStatusLine= Activator.getDefault().getPreferenceStore().getBoolean(PreferenceKeys.SHOW_IN_STATUS_LINE_KEY);
		updateStatusLine(showInStatusLine);
	}
}
