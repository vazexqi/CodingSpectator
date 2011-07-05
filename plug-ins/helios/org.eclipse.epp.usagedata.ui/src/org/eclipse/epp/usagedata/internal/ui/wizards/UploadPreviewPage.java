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

import org.eclipse.epp.usagedata.internal.ui.preview.UploadPreview;
import org.eclipse.epp.usagedata.internal.ui.uploaders.AskUserUploader;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class UploadPreviewPage extends WizardPage {

	private final AskUserUploader uploader;

	public UploadPreviewPage(AskUserUploader uploader) {
		super("wizardPage"); //$NON-NLS-1$
		this.uploader = uploader;
		setTitle(Messages.UploadPreviewPage_1); 
		setDescription(Messages.UploadPreviewPage_2); 
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		UploadPreview preview = new UploadPreview(uploader.getUploadParameters());
		Control control = preview.createControl(container);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(container);
	}
}