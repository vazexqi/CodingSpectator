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

import java.net.URL;

import org.eclipse.core.runtime.Status;
import org.eclipse.epp.usagedata.internal.ui.Activator;
import org.eclipse.epp.usagedata.internal.ui.uploaders.AskUserUploader;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

public class SelectActionWizardPage extends WizardPage {

	// TODO Replace with proper values
	private static final String UDC_URL = "http://www.eclipse.org/org/usagedata/index.php"; //$NON-NLS-1$
	private static final String FAQ_URL = "http://www.eclipse.org/org/usagedata/faq.php"; //$NON-NLS-1$

	private static final int WIDTH_HINT = 500;
	
	private final AskUserUploader uploader;
	private Button neverUploadRadio;
	private Button dontUploadRadio;
	private Button uploadAlwaysRadio;
	private Button uploadNowRadio;

	public SelectActionWizardPage(AskUserUploader uploader) {
		super("wizardPage"); //$NON-NLS-1$
		this.uploader = uploader;
		setTitle(Messages.SelectActionWizardPage_3); 
		setDescription(Messages.SelectActionWizardPage_4); 
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		FormText greeting = createFormText(composite, Messages.SelectActionWizardPage_5); 
		greeting.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				if ("udc".equals(event.getHref())) { //$NON-NLS-1$
					browseTo(UDC_URL); 
				} else if ("preview".equals(event.getHref())) { //$NON-NLS-1$
					((AskUserUploaderWizard)getWizard()).showPreviewPage();
				} else if ("faq".equals(event.getHref())) { //$NON-NLS-1$
					browseTo(FAQ_URL);
				}
			}
		});
		createSpacer(composite);		
						
		createUploadNowRadio(composite);		
		createSpacer(composite);
		
		createUploadAlwaysRadio(composite);
		createSpacer(composite);
		
		createDontUploadRadio(composite);
		createSpacer(composite);
		
		createNeverUploadRadio(composite);
		createSpacer(composite);

		FormText text = createFormText(composite, getTermsText());
		text.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				((AskUserUploaderWizard)getWizard()).showTermsPage();
			}
		});
		
		setControl(composite);
	}

	private String getTermsText() {
		return Messages.SelectActionWizardPage_9; 
	}

	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData layoutData = new GridData();
		layoutData.heightHint = 5;
		spacer.setLayoutData(layoutData);
	}

	private void createUploadNowRadio(Composite parent) {
		uploadNowRadio = createRadio(parent, Messages.SelectActionWizardPage_10, AskUserUploader.UPLOAD_NOW); 
		createDescriptionText(parent, Messages.SelectActionWizardPage_11); 
	}

	private void createUploadAlwaysRadio(Composite parent) {
		uploadAlwaysRadio = createRadio(parent, Messages.SelectActionWizardPage_12, AskUserUploader.UPLOAD_ALWAYS); 
		createDescriptionText(parent, Messages.SelectActionWizardPage_13); 
	}

	private void createDontUploadRadio(Composite parent) {
		dontUploadRadio = createRadio(parent, Messages.SelectActionWizardPage_14, AskUserUploader.DONT_UPLOAD);		 
		createDescriptionText(parent, Messages.SelectActionWizardPage_15); 
	}

	private void createNeverUploadRadio(Composite parent) {
		neverUploadRadio = createRadio(parent, Messages.SelectActionWizardPage_16,AskUserUploader.NEVER_UPLOAD);	 
		createDescriptionText(parent, Messages.SelectActionWizardPage_17);		 
	}

	private Button createRadio(Composite parent, String label, final int action) {
		Button radio = new Button(parent, SWT.RADIO);
		radio.setText(label);
		radio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uploader.setAction(action);
				getContainer().updateButtons();
			}
		});
		radio.setSelection(uploader.getAction() == action);
		
		return radio;
	}
	
	private void createDescriptionText(Composite parent, String string) {
		createText(parent, string, 25);
	}
		
	private void createText(Composite parent, String string, int indent) {
		Label text = new Label(parent, SWT.WRAP);
		text.setText(string);
		
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = indent;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.widthHint = WIDTH_HINT;
		text.setLayoutData(layoutData);
	}

	private FormText createFormText(Composite parent, String string) {
		FormText text = new FormText(parent, SWT.WRAP);
		text.setText(string, true, true);
		
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.widthHint = WIDTH_HINT;
		text.setLayoutData(layoutData);
		
		return text;
	}

	@Override
	public boolean isPageComplete() {
		if (uploadAlwaysRadio.getSelection()) return true;
		if (uploadNowRadio.getSelection()) return true;
		if (neverUploadRadio.getSelection()) return true;
		if (dontUploadRadio.getSelection()) return true;
		
		return false;
	}

	private void browseTo(String url) {
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Error opening browser", e)); //$NON-NLS-1$
		}
	}
}