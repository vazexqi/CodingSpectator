/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.replaying;

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
 * @author Stas Negara
 * 
 */
public class CustomDelayDialog extends Dialog {

	private static final int DEFAULT_DELAY= 500;

	private static int delay= DEFAULT_DELAY;

	private Text text;

	protected CustomDelayDialog(Shell parentShell) {
		super(parentShell);
	}

	public static int getDelay() {
		return delay;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Custom delay");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.horizontalSpacing= 5;
		composite.setLayout(layout);
		Label label= new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(285, 15));
		label.setText("Provide a custom delay between operations in milliseconds:");
		text= new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(30, 15));
		text.setText(String.valueOf(delay));
		return composite;

	}

	@Override
	protected void okPressed() {
		try {
			delay= Integer.valueOf(text.getText().trim());
		} catch (NumberFormatException ex) {
			delay= DEFAULT_DELAY;
		}
		super.okPressed();
	}

}
