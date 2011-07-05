/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

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
public class UploadingPreferencePage extends BundlePreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor lastUploadTextField;

	public UploadingPreferencePage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferenceStore= PrefsFacade.getInstance().getPreferenceStore();
		setPreferenceStore(preferenceStore);
		setDescription(Activator.populateMessageWithPluginName(Messages.UploadingPreferencePage_Description));
		preferenceStore.addPropertyChangeListener(new LastUploadChangeListener(Messages.PrefsFacade_LastUploadTimeKey));
	}

	@Override
	protected void createFieldEditors() {
		lastUploadTextField= addDisabledTextField(Messages.PrefsFacade_LastUploadTimeKey, Messages.UploadingPreferencePage_LastUploadTextField);
		createUploadNowButton();
	}

	@Override
	public boolean performOk() {
		// Do not store any values for the disabled text fields
		// All values will be stored manually through PrefsFacade
		return true;
	}

	private void createUploadNowButton() {
		Button uploadButton= new Button(getFieldEditorParent(), SWT.PUSH);
		uploadButton.setText(Activator.populateMessageWithPluginName(Messages.UploadingPreferencePage_UploadNowButtonText));

		uploadButton.addSelectionListener(new SelectionAdapter() {

			/**
			 * CodingSpectator lets the user manually upload the data if Eclipse is in development
			 * mode. But, it doesn't automatically upload the data at the start-up if Eclipse is run
			 * in development mode. See #{@link Activator#earlyStartup}.
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Submitter submitter= new Submitter();

				if (Uploader.promptUntilValidCredentialsOrCanceled(submitter)) {
					Uploader.submit(submitter);
				}
			}

		});

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

}
