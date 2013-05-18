/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.codeskimmer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Connor Simmons
 * 
 */
public class UsernameDialog extends Dialog {

	private static final String DEFAULT_USERNAME = "default";

	private static String username = DEFAULT_USERNAME;

	private Text text;

	protected UsernameDialog(Shell parentShell) {
		super(parentShell);
	}

	public static String getUsername() {
		return username;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Username Input");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 5;
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(285, 15));
		label.setText("Provide the username for this loaded set of operations:");
		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(285, 15));
		text.setText(username);
		return composite;

	}

	@Override
	protected void okPressed() {
		username = text.getText().trim();
		super.okPressed();
	}

}
