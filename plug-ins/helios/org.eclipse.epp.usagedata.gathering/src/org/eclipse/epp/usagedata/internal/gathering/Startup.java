/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * @author Mohsen Vakilian, nchen - always set capturing to true
 * 
 */
public class Startup implements IStartup {

	// CODINGSPECTATOR
	public void earlyStartup() {
		getCaptureSettings().setEnabled(true);
		IPreferenceStore preferenceStore= UsageDataCaptureActivator.getDefault().getPreferenceStore();
		if (preferenceStore.needsSaving()) {
			try {
				new InstanceScope().getNode(UsageDataCaptureActivator.PLUGIN_ID).flush();
			} catch (BackingStoreException e) {
				UsageDataCaptureActivator.getDefault().logException("Unable to flush preferences for " + UsageDataCaptureActivator.PLUGIN_ID, e);
			}
		}
	}

	private UsageDataCaptureSettings getCaptureSettings() {
		return UsageDataCaptureActivator.getDefault().getSettings();
	}

}
