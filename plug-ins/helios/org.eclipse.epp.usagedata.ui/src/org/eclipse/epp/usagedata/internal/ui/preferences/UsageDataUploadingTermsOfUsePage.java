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
package org.eclipse.epp.usagedata.internal.ui.preferences;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.epp.usagedata.internal.ui.Activator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UsageDataUploadingTermsOfUsePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button acceptTermsButton;

	public UsageDataUploadingTermsOfUsePage() {
		noDefaultAndApplyButton();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		Browser browser = new Browser(composite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browser.setLayoutData(layoutData);		
		browser.setUrl(getTermsOfUseUrl());
		
		acceptTermsButton = new Button(composite, SWT.CHECK);
		acceptTermsButton.setText(Messages.UsageDataUploadingTermsOfUsePage_0); 
		GridData gridData = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		acceptTermsButton.setLayoutData(gridData);		
		
		acceptTermsButton.setSelection(getCapturePreferences().getBoolean(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY));
		
		return composite;
	}
	
	@Override
	public boolean performOk() {
		getCapturePreferences().setValue(UsageDataCaptureSettings.USER_ACCEPTED_TERMS_OF_USE_KEY, acceptTermsButton.getSelection());
		
		return super.performOk();
	}
	
	private String getTermsOfUseUrl() {
		URL terms = FileLocator.find(Activator.getDefault().getBundle(), new Path("terms.html"), null); //$NON-NLS-1$
		try {
			return FileLocator.toFileURL(terms).toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private IPreferenceStore getCapturePreferences() {
		return org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator.getDefault().getPreferenceStore();
	}
}