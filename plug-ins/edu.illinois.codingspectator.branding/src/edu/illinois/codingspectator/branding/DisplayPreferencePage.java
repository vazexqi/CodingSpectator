/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.branding;

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

/**
 * @author Mohsen Vakilian
 * 
 */
public class DisplayPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button showInStatusLineCheckbox;

	private IPropertyChangeListener propertyChangeListener= new IPropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
			if (PreferenceKeys.SHOW_IN_STATUS_LINE_KEY.equals(event.getProperty())) {
				// Updates the status of the check box using a syncExec so that the check box is updated before the preference page closes.
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						showInStatusLineCheckbox.setSelection((Boolean)event.getNewValue());
					};
				});
			}
		}
	};

	public DisplayPreferencePage() {
		setDescription(Messages.WorkbenchPreferencePage_preference_page_description);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}


	@Override
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		composite.setLayout(new GridLayout());

		showInStatusLineCheckbox= new Button(composite, SWT.CHECK | SWT.LEFT);
		showInStatusLineCheckbox.setText(Messages.WorkbenchPreferencePage_show_bundle_in_status_line);
		showInStatusLineCheckbox.setToolTipText(Messages.WorkbenchPreferencePage_show_bundle_in_status_line_tool_tip);

		Label filler= new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

		initialize();

		return composite;
	}

	private void initialize() {
		showInStatusLineCheckbox.setSelection(getPreferenceStore().getBoolean(PreferenceKeys.SHOW_IN_STATUS_LINE_KEY));
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(PreferenceKeys.SHOW_IN_STATUS_LINE_KEY, showInStatusLineCheckbox.getSelection());

		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		showInStatusLineCheckbox.setSelection(getPreferenceStore().getDefaultBoolean(PreferenceKeys.SHOW_IN_STATUS_LINE_KEY));

		super.performDefaults();
	}
}
