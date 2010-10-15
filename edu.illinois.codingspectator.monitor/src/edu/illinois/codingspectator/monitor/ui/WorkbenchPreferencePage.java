/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.illinois.codingspectator.monitor.Activator;
import edu.illinois.codingspectator.monitor.Messages;
import edu.illinois.codingspectator.monitor.prefs.PrefsFacade;
import edu.illinois.codingspectator.monitor.submission.Submitter;

/**
 * This is the preference page for the plug-in. It displays the UUID(String) which will be used in
 * the URL to the repository to store the recorded data.
 * 
 * A single UUID is assigned to each workspace of Eclipse helping us identify which machine the user
 * is working on in the event that a user programs on multiple machines.
 * 
 * There is an option for the user to force the upload of the data. This is enabled through the
 * "Upload Now" button on preference page (similar to the interface of the UDC preference page).
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
public class WorkbenchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor lastUploadTextField;

	private StringFieldEditor uiudTextField;

	public WorkbenchPreferencePage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferenceStore= PrefsFacade.getInstance().getPreferenceStore();
		setPreferenceStore(preferenceStore);
		setDescription(Activator.populateMessageWithPluginName(Messages.WorkbenchPreferencePage_Title));
		preferenceStore.addPropertyChangeListener(new LastUploadChangeListener(Messages.PrefsFacade_LastUploadTimeKey));
		preferenceStore.addPropertyChangeListener(new UIUDChangeListener(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey));
	}

	@Override
	protected void createFieldEditors() {
		uiudTextField= addDisabledTextField(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, Messages.WorkbenchPreferencePage_UUIDTextField);
		lastUploadTextField= addDisabledTextField(Messages.PrefsFacade_LastUploadTimeKey, Messages.WorkbenchPreferencePage_LastUploadTextField);
		createUploadNowButton();
	}

	private StringFieldEditor addDisabledTextField(String textFieldValue, String textFieldLabel) {
		StringFieldEditor textfield= new StringFieldEditor(textFieldValue, textFieldLabel,
				getFieldEditorParent());
		textfield.setEnabled(false, getFieldEditorParent());
		addField(textfield);
		return textfield;
	}

	@Override
	public boolean performOk() {
		// Do not store any values for the disabled text fields
		// All values will be stored manually through PrefsFacade
		return true;
	}

	private void createUploadNowButton() {
		Button uploadButton= new Button(getFieldEditorParent(), SWT.PUSH);
		uploadButton.setText(Activator.populateMessageWithPluginName(Messages.WorkbenchPreferencePage_UploadNowButtonText));

		uploadButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Submitter submitter= new Submitter();

				if (Uploader.promptUntilValidCredentialsOrCanceled(submitter)) {
					Uploader.submit(submitter);
				}
			}

		});

	}

	/**
	 * This class implements a listener that updates a given field editor whenever its preference
	 * key changes.
	 * 
	 */
	public abstract class PreferenceChangeListener implements IPropertyChangeListener {

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

				;
			}
		}

	}

	public class LastUploadChangeListener extends PreferenceChangeListener {

		public LastUploadChangeListener(String preferenceKey) {
			super(preferenceKey);
		}

		@Override
		protected StringFieldEditor getFieldEditor() {
			return lastUploadTextField;
		}

	}

	public class UIUDChangeListener extends PreferenceChangeListener {

		public UIUDChangeListener(String preferenceKey) {
			super(preferenceKey);
		}

		@Override
		protected StringFieldEditor getFieldEditor() {
			return uiudTextField;
		}

	}

}
