/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.illinois.codingspectator.monitor.ui;

import java.net.URL;

import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * core A dialog to prompt the user for login information such as user name and password.
 * 
 * @see org.eclipse.equinox.internal.p2.ui.dialogs.UserValidationDialog
 */
public class UserValidationDialog extends Dialog {

	private Text username;

	private Text password;

	private AuthenticationInfo result= null;

	private Button saveButton;

	private final String titleMessage;

	private final String message;

	private final int dialogImageType;

	public UserValidationDialog(Shell parentShell, String titleMessage, String message, String initialUsername, int dialogImageType) {
		super(parentShell);

		this.titleMessage= titleMessage;
		this.message= message;

		this.result= new AuthenticationInfo(initialUsername, "", false);

		this.dialogImageType= dialogImageType;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(titleMessage);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite)super.createDialogArea(parent);

		Composite container= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createImageSection(container);
		createFieldsSection(container);

		Dialog.applyDialogFont(composite);

		return composite;
	}

	private void createImageSection(Composite composite) {
		Image image= composite.getDisplay().getSystemImage(dialogImageType);
		if (image != null) {
			Label label= new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			label.setImage(image);
		}
	}

	private void createFieldsSection(Composite composite) {
		Composite fieldContainer= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		fieldContainer.setLayout(layout);
		GridData layoutData= new GridData();
		fieldContainer.setLayoutData(layoutData);

		createInstructionsSection(fieldContainer);
		createUsernameTextField(fieldContainer);
		createPasswordTextField(fieldContainer);
		createSavePasswordCheckbox(fieldContainer);
	}

	private void createSavePasswordCheckbox(Composite fieldContainer) {
		saveButton= new Button(fieldContainer, SWT.CHECK);
		saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		saveButton.setText(Messages.UserValidationDialog_SavePassword);
		saveButton.setSelection(saveResult());
	}

	private void createPasswordTextField(Composite fieldContainer) {
		GridData layoutData;
		Label label;
		label= new Label(fieldContainer, SWT.NONE);
		label.setText(Messages.UserValidationDialog_Password);
		password= new Text(fieldContainer, SWT.PASSWORD | SWT.BORDER);
		layoutData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		password.setLayoutData(layoutData);
		password.setText(getPassword());
	}

	private void createUsernameTextField(Composite fieldContainer) {
		GridData layoutData;
		Label label= new Label(fieldContainer, SWT.NONE);
		label.setText(Messages.UserValidationDialog_Username);
		username= new Text(fieldContainer, SWT.BORDER);
		username.setEnabled(false);
		username.setEditable(false);
		layoutData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		username.setLayoutData(layoutData);
		username.setText(getUserName());
		username.setFocus();

		if (isUsernameEmpty()) {
			username.setEditable(true);
			username.setEnabled(true);
		}
	}

	private void createInstructionsSection(Composite fieldContainer) {
		FormText text= new FormText(fieldContainer, SWT.WRAP);
		text.setText(message, true, true);
		GridData data= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1);
		data.grabExcessHorizontalSpace= true;
		data.horizontalAlignment= SWT.FILL;
		data.widthHint= convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		text.setLayoutData(data);
		text.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent event) {
				browseTo(event.getHref().toString());
			}
		});
	}

	private boolean isUsernameEmpty() {
		return "".equals(getUserName());
	}

	@Override
	protected void okPressed() {
		this.result= new AuthenticationInfo(username.getText(), password.getText(), saveButton.getSelection());
		super.okPressed();
	}

	/**
	 * Returns the authentication information given by the user, or null if the user cancelled
	 * 
	 * @return the authentication information given by the user, or null if the user cancelled
	 */
	public AuthenticationInfo getResult() {
		return result;
	}

	private String getUserName() {
		return result != null ? result.getUserName() : ""; //$NON-NLS-1$
	}

	private String getPassword() {
		return result != null ? result.getPassword() : ""; //$NON-NLS-1$
	}

	private boolean saveResult() {
		return result != null ? result.saveResult() : false;
	}

	/**
	 * See @{link org.eclipse.epp.usagedata.internal.ui.wizards.SelectActionWizardPage}
	 * 
	 * @param url The url to visit
	 */
	private void browseTo(String url) {
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Error opening browser", e)); //$NON-NLS-1$
		}
	}
}
