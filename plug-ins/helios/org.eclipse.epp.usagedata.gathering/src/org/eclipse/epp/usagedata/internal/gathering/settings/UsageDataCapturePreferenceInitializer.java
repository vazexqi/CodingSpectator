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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.jface.preference.IPreferenceStore;

public class UsageDataCapturePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = UsageDataCaptureActivator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, true);
		preferenceStore.setDefault(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY, false);
	}

}
