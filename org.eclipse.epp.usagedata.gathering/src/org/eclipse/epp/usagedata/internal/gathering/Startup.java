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

import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;

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
			UsageDataCaptureActivator.getDefault().savePluginPreferences();
		}
	}

	private UsageDataCaptureSettings getCaptureSettings() {
		return UsageDataCaptureActivator.getDefault().getSettings();
	}

}
