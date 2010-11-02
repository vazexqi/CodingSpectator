/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.SubmitterException;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Uploader {

	private static IStatus reportUploadFailure(SubmitterException exception) {
		Activator.populateMessageWithPluginName(Messages.WorkbenchPreferencePage_FailedToUploadMessage);
		Status errorStatus= Activator.getDefault().createErrorStatus(Messages.WorkbenchPreferencePage_FailedToUploadMessage, exception);
		Activator.getDefault().log(errorStatus);
		return errorStatus;
	}

	/**
	 * 
	 * @param submitter
	 * @return if the method completed successfully.
	 */
	public static boolean promptUntilValidCredentialsOrCanceled(final Submitter submitter) {
		try {
			return submitter.promptUntilValidCredentialsOrCanceled();
		} catch (SubmitterException subEx) {
			reportUploadFailure(subEx);
			return false;
		}
	}

	public static void submit(final Submitter submitter) {
		Job job= new Job(MessageFormat.format(Messages.WorkbenchPreferencePage_UploadingMessage, Messages.WorkbenchPreferencePage_PluginName)) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					submitter.submit();
					PrefsFacade.getInstance().updateLastUploadTime();
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
