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

import org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator;
import org.eclipse.epp.usagedata.internal.gathering.settings.UsageDataCaptureSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UsageDataCapturePreferencesPage extends PreferencePage
	implements IWorkbenchPreferencePage {
	
	Button captureEnabledCheckbox;

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
			if (UsageDataCaptureSettings.CAPTURE_ENABLED_KEY.equals(event.getProperty())) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						captureEnabledCheckbox.setSelection((Boolean)event.getNewValue());
					};
				});				
			}
		}			
	};
	
	public UsageDataCapturePreferencesPage() {
		setDescription(Messages.UsageDataCapturePreferencesPage_0); 
		setPreferenceStore(UsageDataCaptureActivator.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}
	
	@Override
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		composite.setLayout(new GridLayout());
		
		createGeneralInformationArea(composite);
		
		Label filler = new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		
		initialize();
		
		return composite;
	}

	private void initialize() {
		captureEnabledCheckbox.setSelection(getCapturePreferences().getBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));
	}

	@Override
	public boolean performOk() {
		getCapturePreferences().setValue(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY, captureEnabledCheckbox.getSelection());
	
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		captureEnabledCheckbox.setSelection(getCapturePreferences().getDefaultBoolean(UsageDataCaptureSettings.CAPTURE_ENABLED_KEY));

		super.performDefaults();
	}

	private void createGeneralInformationArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		
		composite.setLayout(new GridLayout());
		
		captureEnabledCheckbox = new Button(composite, SWT.CHECK | SWT.LEFT);
		captureEnabledCheckbox.setText(Messages.UsageDataCapturePreferencesPage_1);  
	}


	private IPreferenceStore getCapturePreferences() {
		return org.eclipse.epp.usagedata.internal.gathering.UsageDataCaptureActivator.getDefault().getPreferenceStore();
	}
}
