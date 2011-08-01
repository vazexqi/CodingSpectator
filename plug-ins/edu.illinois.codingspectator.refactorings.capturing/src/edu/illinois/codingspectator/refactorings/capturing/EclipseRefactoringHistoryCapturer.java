/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactorings.capturing;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistorySerializer;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;

/**
 * 
 * This class copies the refactorings logged by Eclipse into the watched directory of
 * CodingSpectator when CodingSpectator is about to submit its data and before it locks the watched
 * folder.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@SuppressWarnings("restriction")
public class EclipseRefactoringHistoryCapturer implements SubmitterListener {

	private IFileStore getFileStore(IPath path) {
		return EFS.getLocalFileSystem().getStore(path);
	}

	private void transferEclipseRefactoringHistory() {
		try {
			IFileStore eclipseRefactoringHistoryFileStore= RefactoringHistorySerializer.getEclipseRefactoringHistoryFileStore();
			if (eclipseRefactoringHistoryFileStore.fetchInfo().exists()) {
				eclipseRefactoringHistoryFileStore.copy(getFileStore(CodingSpectatorDataPlugin.getVersionedStorageLocation().append("eclipse-refactorings")),
								EFS.OVERWRITE, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Failed to transfer Eclipse LTK data to CodingSpectator...", e));
		}
	}

	@Override
	public void preLock() {
		transferEclipseRefactoringHistory();
	}

	@Override
	public void preSubmit() {
	}

	@Override
	public void postSubmit(boolean succeeded) {
	}

}
