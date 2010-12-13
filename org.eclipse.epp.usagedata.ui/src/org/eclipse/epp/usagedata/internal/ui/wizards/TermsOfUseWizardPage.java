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
package org.eclipse.epp.usagedata.internal.ui.wizards;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.epp.usagedata.internal.ui.Activator;
import org.eclipse.epp.usagedata.internal.ui.uploaders.AskUserUploader;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class TermsOfUseWizardPage extends WizardPage {

	private final AskUserUploader uploader;

	public TermsOfUseWizardPage(AskUserUploader uploader) {
		super("wizardPage"); //$NON-NLS-1$
		this.uploader = uploader;
		setTitle(Messages.TermsOfUseWizardPage_1); 
		//setDescription("This wizard uploads captured usage data. Clearly a better description is required.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		Browser browser = new Browser(container, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browser.setLayoutData(layoutData);		
		browser.setUrl(getTermsOfUseUrl());
		
		final Button acceptTermsButton = new Button(container, SWT.CHECK);
		acceptTermsButton.setText(Messages.TermsOfUseWizardPage_2); 
		GridData gridData = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		acceptTermsButton.setLayoutData(gridData);
		acceptTermsButton.setSelection(uploader.hasUserAcceptedTermsOfUse());
		acceptTermsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				uploader.setUserAcceptedTermsOfUse(acceptTermsButton.getSelection());
				getContainer().updateButtons();
			}
		});
		
		setControl(container);
		
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
}