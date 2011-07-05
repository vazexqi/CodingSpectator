/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * @author Mohsen Vakilian
 * 
 */
public abstract class BundlePreferencePage extends FieldEditorPreferencePage {

	public BundlePreferencePage(int style) {
		super(style);
	}

	protected StringFieldEditor addDisabledTextField(String textFieldValue, String textFieldLabel) {
		StringFieldEditor textfield= new StringFieldEditor(textFieldValue, textFieldLabel, getFieldEditorParent());
		textfield.setEnabled(false, getFieldEditorParent());
		addField(textfield);
		return textfield;
	}

	/**
	 * This class implements a listener that updates a given field editor whenever its preference
	 * key changes.
	 * 
	 */
	public static abstract class PreferenceChangeListener implements IPropertyChangeListener {

		final String preferenceKey;

		public PreferenceChangeListener(String preferenceKey) {
			super();
			this.preferenceKey= preferenceKey;
		}

		protected abstract StringFieldEditor getFieldEditor();

		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event.getProperty().equals(preferenceKey)) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						getFieldEditor().setStringValue((String)event.getNewValue());

					}
				});

			}
		}

	}

}
