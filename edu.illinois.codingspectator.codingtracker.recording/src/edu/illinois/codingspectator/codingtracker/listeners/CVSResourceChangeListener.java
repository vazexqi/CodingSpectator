/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.FileRevision;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;
import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class CVSResourceChangeListener extends BasicListener implements IResourceChangeListener {

	private final IResourceDeltaVisitor resourceDeltaVisitor= new ResourceDeltaVisitor();

	//Populated sets:

	private final Set<IFile> addedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> changedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> removedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> cvsEntriesAddedSet= new HashSet<IFile>();

	private final Set<IFile> cvsEntriesChangedOrRemovedSet= new HashSet<IFile>();

	//Calculated sets:

	private final Set<FileRevision> updatedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> cvsInitiallyCommittedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> cvsCommittedJavaFileRevisions= new HashSet<FileRevision>();


	public static void register() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new CVSResourceChangeListener(), IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (isRefactoring) {
			//No CVS operations are part of a refactoring. 
			//Note that this check is unreliable because this notification usually arrives in a thread that is different from the thread,
			//which performs the refactoring (setting isRefactoring accordingly).
			return;
		}
		IResourceDelta delta= event.getDelta();
		if (delta != null) { //why could it be null?
			initializeSets();
			populateSets(delta);
			//proceed only if this is a CVS operation (i.e. at least one CVS/Entries file is affected)
			if (!cvsEntriesAddedSet.isEmpty() || !cvsEntriesChangedOrRemovedSet.isEmpty()) {
				System.out.println("CVS operation recorded!!!");
				calculateCVSSets();
				recordSets();
				updateKnownFiles();
			}
		}
	}

	private void initializeSets() {
		addedJavaFiles.clear();
		changedJavaFiles.clear();
		removedJavaFiles.clear();
		cvsEntriesAddedSet.clear();
		cvsEntriesChangedOrRemovedSet.clear();
		updatedJavaFileRevisions.clear();
		cvsInitiallyCommittedJavaFileRevisions.clear();
		cvsCommittedJavaFileRevisions.clear();
	}

	private void populateSets(IResourceDelta delta) {
		try {
			//used IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS to see changes in version control admin area
			delta.accept(resourceDeltaVisitor, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		} catch (CoreException e) {
			Debugger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToVisitResourceDelta);
		}
	}

	private void calculateCVSSets() {
		processAddedCVSEntriesFiles();
		processChangedOrRemovedCVSEntriesFiles();
	}

	private void processAddedCVSEntriesFiles() {
		boolean hasChangedKnownFiles= false;
		for (IFile cvsEntriesFile : cvsEntriesAddedSet) {
			IPath relativePath= cvsEntriesFile.getFullPath().removeLastSegments(2);
			Map<IFile, String> newRevisions= ResourceHelper.getEntriesRevisions(cvsEntriesFile, relativePath);
			boolean isInitialCommit= false;
			for (Entry<IFile, String> newEntry : newRevisions.entrySet()) {
				IFile entryFile= newEntry.getKey();
				FileRevision fileRevision= getCVSFileRevision(entryFile, newEntry.getValue());
				if (changedJavaFiles.contains(entryFile)) {
					updatedJavaFileRevisions.add(fileRevision);
				} else if (!addedJavaFiles.contains(entryFile)) {
					cvsInitiallyCommittedJavaFileRevisions.add(fileRevision);
					isInitialCommit= true;
				}
			}
			if (isInitialCommit || doesContainKnownFiles(relativePath)) {
				knownFilesRecorder.addCVSEntriesFile(cvsEntriesFile);
				hasChangedKnownFiles= true;
			}
		}
		if (hasChangedKnownFiles) {
			knownFilesRecorder.recordKnownFiles();
		}
	}

	private FileRevision getCVSFileRevision(IFile file, String revision) {
		//CVS does not have a separate committed revision, so use a placeholder "0" instead
		return new FileRevision(file, revision, "0");
	}

	private boolean doesContainKnownFiles(IPath path) {
		IResource resource= ResourceHelper.findWorkspaceMember(path);
		if (resource instanceof Folder) {
			Folder containerFolder= (Folder)resource;
			try {
				IResource[] members= containerFolder.members();
				for (IResource member : members) {
					if (member instanceof IFile && knownFilesRecorder.isFileKnown((IFile)member, false)) {
						return true;
					}
				}
			} catch (CoreException e) {
				Debugger.logExceptionToErrorLog(e, Messages.Recorder_CVSFolderMembersFailure);
			}
		}
		return false;
	}

	private void processChangedOrRemovedCVSEntriesFiles() {
		boolean hasChangedKnownFiles= false;
		for (IFile cvsEntriesFile : cvsEntriesChangedOrRemovedSet) {
			if (cvsEntriesFile.exists()) {
				IPath relativePath= cvsEntriesFile.getFullPath().removeLastSegments(2);
				Map<IFile, String> newRevisions= ResourceHelper.getEntriesRevisions(cvsEntriesFile, relativePath);
				File trackedCVSEntriesFile= knownFilesRecorder.getTrackedCVSEntriesFile(cvsEntriesFile);
				if (trackedCVSEntriesFile.exists()) {
					Map<IFile, String> previousRevisions= ResourceHelper.getEntriesRevisions(trackedCVSEntriesFile, relativePath);
					processCVSRevisionsDifference(newRevisions, previousRevisions);
					knownFilesRecorder.addCVSEntriesFile(cvsEntriesFile); //overwrite the existing tracked entries file with the new one
					hasChangedKnownFiles= true;
				} else {
					for (Entry<IFile, String> newEntry : newRevisions.entrySet()) {
						IFile entryFile= newEntry.getKey();
						if (changedJavaFiles.contains(entryFile)) {
							updatedJavaFileRevisions.add(getCVSFileRevision(entryFile, newEntry.getValue()));
						}
					}
				}
			} else {
				// CVS entries file was deleted, so stop tracking it
				knownFilesRecorder.removeKnownFile(cvsEntriesFile);
				hasChangedKnownFiles= true;
			}
		}
		if (hasChangedKnownFiles) {
			knownFilesRecorder.recordKnownFiles();
		}
	}

	private void processCVSRevisionsDifference(Map<IFile, String> newRevisions, Map<IFile, String> previousRevisions) {
		for (Entry<IFile, String> newEntry : newRevisions.entrySet()) {
			IFile entryFile= newEntry.getKey();
			String newRevision= newEntry.getValue();
			FileRevision newFileRevision= getCVSFileRevision(entryFile, newRevision);
			String previousRevision= previousRevisions.get(entryFile);
			if (previousRevision == null) {
				if (!addedJavaFiles.contains(entryFile)) {
					cvsInitiallyCommittedJavaFileRevisions.add(newFileRevision);
				}
			} else if (!previousRevision.equals(newRevision)) {
				if (changedJavaFiles.contains(entryFile)) {
					updatedJavaFileRevisions.add(newFileRevision);
				} else {
					cvsCommittedJavaFileRevisions.add(newFileRevision);
				}
			}
		}
	}

	private void recordSets() {
		operationRecorder.recordUpdatedFiles(updatedJavaFileRevisions);
		operationRecorder.recordCommittedFiles(cvsInitiallyCommittedJavaFileRevisions, true, false);
		operationRecorder.recordCommittedFiles(cvsCommittedJavaFileRevisions, false, false);
	}

	private void updateKnownFiles() {
		removedJavaFiles.addAll(ResourceHelper.getFilesFromRevisions(updatedJavaFileRevisions)); //updated files become unknown (like removed)
		knownFilesRecorder.removeKnownFiles(removedJavaFiles);
	}

	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource= delta.getResource();
			if (resource instanceof IFile) {
				IFile file= (IFile)resource;
				if (file.getName().equals("Entries") && file.getParent().getName().equals("CVS")) {
					if (delta.getKind() == IResourceDelta.ADDED) {
						cvsEntriesAddedSet.add(file);
					} else {
						cvsEntriesChangedOrRemovedSet.add(file);
					}
				} else {
					if (ResourceHelper.isJavaFile(file)) {
						visitJavaFile(delta, file);
					}
				}
			}
			return true;
		}

		private void visitJavaFile(IResourceDelta delta, IFile file) {
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					addedJavaFiles.add(file);
					break;
				case IResourceDelta.REMOVED:
					removedJavaFiles.add(file);
					break;
				case IResourceDelta.CHANGED:
					if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
						changedJavaFiles.add(file);
					}
					break;
			}
		}

	}

}
