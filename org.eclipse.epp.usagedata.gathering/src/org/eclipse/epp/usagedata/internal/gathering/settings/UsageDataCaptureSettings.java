/*******************************************************************************
 * Copyright (c) 2007 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering.settings;

import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.jface.preference.IPreferenceStore;

public class UsageDataCaptureSettings {

	public static final String CAPTURE_ENABLED_KEY = UsageDataCaptureActivator.PLUGIN_ID + ".enabled"; //$NON-NLS-1$
	public static final String USER_ACCEPTED_TERMS_OF_USE_KEY = UsageDataCaptureActivator.PLUGIN_ID + ".terms_accepted"; //$NON-NLS-1$

	public boolean isEnabled() {
		if (System.getProperties().containsKey(CAPTURE_ENABLED_KEY)) {
			return "true".equals(System.getProperty(CAPTURE_ENABLED_KEY)); //$NON-NLS-1$
		} else if (getPreferencesStore().contains(CAPTURE_ENABLED_KEY)) {
			return getPreferencesStore().getBoolean(CAPTURE_ENABLED_KEY);
		} else {
			return true;
		}
	}

	public void setEnabled(boolean value) {
		// The preferences store actually does this for us. However, for
		// completeness, we're checking the value to potentially avoid 
		// messing with the service.
		if (getPreferencesStore().getBoolean(CAPTURE_ENABLED_KEY) == value) return;
		
		getPreferencesStore().setValue(CAPTURE_ENABLED_KEY, value);
		
		// The activator should be listening to changes in the preferences store
		// and will change the state of the service as a result of us setting
		// the value here.
	}
	
	private IPreferenceStore getPreferencesStore() {
		return UsageDataCaptureActivator.getDefault().getPreferenceStore();
	}

	public boolean hasUserAcceptedTermsOfUse() {
		return getPreferencesStore().getBoolean(USER_ACCEPTED_TERMS_OF_USE_KEY);
	}

	public void setUserAcceptedTermsOfUse(boolean value) {
		getPreferencesStore().setValue(USER_ACCEPTED_TERMS_OF_USE_KEY, value);
	}
}
