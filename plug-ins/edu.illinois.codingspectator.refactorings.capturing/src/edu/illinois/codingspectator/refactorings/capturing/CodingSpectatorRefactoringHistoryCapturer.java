/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactorings.capturing;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistorySerializer;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
@SuppressWarnings("restriction")
public class CodingSpectatorRefactoringHistoryCapturer implements SubmitterListener {

	private void transferCodingSpectatorRefactoringHistory() {
		try {
			IPath codingspectatorRefactoringHistoryPath= RefactoringHistorySerializer.getCodingSpectatorRefactoringHistoryFolder();
			EFSFile codingspectatorRefactoringHistory= new EFSFile(codingspectatorRefactoringHistoryPath);
			EFSFile destinationInWatchedFolder= new EFSFile(CodingSpectatorDataPlugin.getStorageLocation());
			if (codingspectatorRefactoringHistory.exists()) {
				codingspectatorRefactoringHistory.copyTo(destinationInWatchedFolder);
			}
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Failed to transfer CodingSpectator data.", e));
		}
	}

	@Override
	public void preSubmit() {
		transferCodingSpectatorRefactoringHistory();
	}

	@Override
	public void postSubmit(boolean succeeded) {
	}

}
