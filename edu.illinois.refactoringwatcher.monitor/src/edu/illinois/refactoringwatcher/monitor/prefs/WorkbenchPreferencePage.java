package edu.illinois.refactoringwatcher.monitor.prefs;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter.NoAuthenticationInformationFoundException;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter.SubmitterException;

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
 * 
 */
public class WorkbenchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public WorkbenchPreferencePage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(populateMessageWithPluginName(Messages.WorkbenchPreferencePage_title));
		PrefsFacade.generateUUIDIfDoesNotExist();
	}

	private String populateMessageWithPluginName(String formattedString) {
		return MessageFormat.format(formattedString, Messages.WorkbenchPreferencePage_PluginName);
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor textfield= new StringFieldEditor(Messages.WorkbenchPreferencePage_UUIDFieldPreferenceKey, Messages.WorkbenchPreferencePage_UUIDTextField,
				getFieldEditorParent());
		textfield.setEnabled(false, getFieldEditorParent());
		addField(textfield);

		createUploadNowButton();
	}

	private void createUploadNowButton() {
		Button uploadButton= new Button(getFieldEditorParent(), SWT.PUSH);
		uploadButton.setText(populateMessageWithPluginName(Messages.WorkbenchPreferencePage_UploadNowButtonText));

		uploadButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Submitter submitter= new Submitter();

				if (authenticateAndInitialize(submitter)) {
					submit(submitter);
				}
			}

		});

	}

	private IStatus reportUploadFailure(SubmitterException exception) {
		populateMessageWithPluginName(Messages.WorkbenchPreferencePage_FailedToUploadMessage);
		Status errorStatus= Activator.getDefault().createErrorStatus(Messages.WorkbenchPreferencePage_FailedToUploadMessage, exception);
		Activator.getDefault().log(errorStatus);
		return errorStatus;
	}

	/**
	 * 
	 * @param submitter
	 * @return if the method completed successfully.
	 */
	private boolean authenticateAndInitialize(final Submitter submitter) {
		try {
			submitter.authenticateAndInitialize();
		} catch (NoAuthenticationInformationFoundException noAuthEx) {
			return false;
		} catch (SubmitterException subEx) {
			reportUploadFailure(subEx);
			return false;
		}
		return true;
	}

	private void submit(final Submitter submitter) {
		Job job= new Job(MessageFormat.format(Messages.WorkbenchPreferencePage_UploadingMessage, Messages.WorkbenchPreferencePage_PluginName)) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					submitter.submit();
				} catch (SubmitterException exception) {
					return reportUploadFailure(exception);
				}
				return Status.OK_STATUS;
			}

		};
		job.setPriority(Job.LONG);
		job.schedule();
	}

}
