/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.replaying;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

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
public class TransformationIDsDialog extends Dialog {

	private static Set<Long> transformationIDs= new TreeSet<Long>();

	private static String lastInput= "";

	private final String message;

	private Text text;

	protected TransformationIDsDialog(Shell parentShell, String message) {
		super(parentShell);
		this.message= message;
	}

	public Set<Long> getTransformationIDs() {
		return transformationIDs;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.horizontalSpacing= 5;
		composite.setLayout(layout);
		Label label= new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(170, 15));
		label.setText("Provide transformation IDs:");
		text= new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(550, 15));
		text.setText(lastInput);
		return composite;

	}

	@Override
	protected void okPressed() {
		lastInput= text.getText().trim();
		StringTokenizer st= new StringTokenizer(lastInput, ", ");
		while (st.hasMoreTokens()) {
			transformationIDs.add(Long.valueOf(st.nextToken()));
		}
		super.okPressed();
	}

}
