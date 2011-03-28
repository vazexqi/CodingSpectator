/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ResourceChangeListener extends BasicListener implements IResourceChangeListener {

	private final IResourceDeltaVisitor resourceDeltaVisitor= new ResourceDeltaVisitor();

	//Populated sets:

	private final Set<IFile> addedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> changedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> removedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnAddedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnChangedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnEntriesChangeSet= new HashSet<IFile>();

	private final Set<IFile> cvsEntriesAddedSet= new HashSet<IFile>();

	private final Set<IFile> cvsEntriesChangedOrRemovedSet= new HashSet<IFile>();

	//Calculated sets:

	//TODO: Consider changing from HashSet to TreeSet to make tests deterministic (interestingly, they all are passing so far).

	private final Set<IFile> savedJavaFiles= new HashSet<IFile>();

	//Actually, should not be more than one per resourceChanged notification
	private final Set<String> savedConflictEditorIDs= new HashSet<String>();

	private final Set<IFile> externallyModifiedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> updatedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnInitiallyCommittedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> cvsInitiallyCommittedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnCommittedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> cvsCommittedJavaFiles= new HashSet<IFile>();


	public static void register() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(), IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) { //why could it be null?
			initializeSets();
			populateSets(delta);
			calculateSets();
			recordSets();
			updateDirtyAndKnownFiles();
		}
	}

	private void initializeSets() {
		addedJavaFiles.clear();
		changedJavaFiles.clear();
		removedJavaFiles.clear();
		svnAddedJavaFiles.clear();
		svnChangedJavaFiles.clear();
		svnEntriesChangeSet.clear();
		cvsEntriesAddedSet.clear();
		cvsEntriesChangedOrRemovedSet.clear();
		savedJavaFiles.clear();
		savedConflictEditorIDs.clear();
		externallyModifiedJavaFiles.clear();
		updatedJavaFiles.clear();
		svnInitiallyCommittedJavaFiles.clear();
		cvsInitiallyCommittedJavaFiles.clear();
		svnCommittedJavaFiles.clear();
		cvsCommittedJavaFiles.clear();
	}

	private void populateSets(IResourceDelta delta) {
		try {
			//used IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS to see changes in .svn folder
			delta.accept(resourceDeltaVisitor, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		} catch (CoreException e) {
			Debugger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToVisitResourceDelta);
		}
	}

	private void calculateSets() {
		//should be done only in this order
		calculateCVSSets();
		calculateSVNSets();
		calculateSavedConflictEditorIDs();
		calculateSavedAndExternallyModifiedJavaFiles();
	}

	private void calculateCVSSets() {
		processAddedCVSEntriesFiles();
		processChangedOrRemovedCVSEntriesFiles();
	}

	private void processAddedCVSEntriesFiles() {
		boolean hasChangedKnownFiles= false;
		for (IFile cvsEntriesFile : cvsEntriesAddedSet) {
			IPath relativePath= cvsEntriesFile.getFullPath().removeLastSegments(2);
			Map<IFile, String> newVersions= FileHelper.getEntriesVersions(cvsEntriesFile, relativePath);
			boolean isInitialCommit= false;
			for (Entry<IFile, String> newEntry : newVersions.entrySet()) {
				IFile entryFile= newEntry.getKey();
				if (changedJavaFiles.contains(entryFile)) {
					updatedJavaFiles.add(entryFile);
				} else if (!addedJavaFiles.contains(entryFile)) {
					cvsInitiallyCommittedJavaFiles.add(entryFile);
					isInitialCommit= true;
				}
			}
			if (isInitialCommit || doesContainKnownFiles(relativePath)) {
				knownfilesRecorder.addCVSEntriesFile(cvsEntriesFile);
				hasChangedKnownFiles= true;
			}
		}
		if (hasChangedKnownFiles) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	private boolean doesContainKnownFiles(IPath path) {
		IResource resource= FileHelper.findWorkspaceMember(path);
		if (resource instanceof Folder) {
			Folder containerFolder= (Folder)resource;
			try {
				IResource[] members= containerFolder.members();
				for (IResource member : members) {
					if (member instanceof IFile && knownfilesRecorder.isFileKnown((IFile)member)) {
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
				Map<IFile, String> newVersions= FileHelper.getEntriesVersions(cvsEntriesFile, relativePath);
				File trackedCVSEntriesFile= knownfilesRecorder.getTrackedCVSEntriesFile(cvsEntriesFile);
				if (trackedCVSEntriesFile.exists()) {
					Map<IFile, String> previousVersions= FileHelper.getEntriesVersions(trackedCVSEntriesFile, relativePath);
					processCVSVersionsDifference(newVersions, previousVersions);
					knownfilesRecorder.addCVSEntriesFile(cvsEntriesFile); //overwrite the existing tracked entries file with the new one
					hasChangedKnownFiles= true;
				} else {
					for (Entry<IFile, String> newEntry : newVersions.entrySet()) {
						IFile entryFile= newEntry.getKey();
						if (changedJavaFiles.contains(entryFile)) {
							updatedJavaFiles.add(entryFile);
						}
					}
				}
			} else {
				// CVS entries file was deleted, so stop tracking it
				knownfilesRecorder.removeKnownfile(cvsEntriesFile);
				hasChangedKnownFiles= true;
			}
		}
		if (hasChangedKnownFiles) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	private void processCVSVersionsDifference(Map<IFile, String> newVersions, Map<IFile, String> previousVersions) {
		for (Entry<IFile, String> newEntry : newVersions.entrySet()) {
			IFile entryFile= newEntry.getKey();
			String previousVersion= previousVersions.get(entryFile);
			if (previousVersion == null) {
				if (!addedJavaFiles.contains(entryFile)) {
					cvsInitiallyCommittedJavaFiles.add(entryFile);
				}
			} else if (!previousVersion.equals(newEntry.getValue())) {
				if (changedJavaFiles.contains(entryFile)) {
					updatedJavaFiles.add(entryFile);
				} else {
					cvsCommittedJavaFiles.add(entryFile);
				}
			}
		}
	}

	private void calculateSVNSets() {
		for (IFile file : svnChangedJavaFiles) {
			if (changedJavaFiles.contains(file)) {
				updatedJavaFiles.add(file); //if both the java file and its svn storage have changed, then its an update
			} else {
				svnCommittedJavaFiles.add(file); //if only svn storage of a java file has changed, its a commit
			}
		}
		for (IFile file : svnAddedJavaFiles) {
			if (!addedJavaFiles.contains(file)) { //if only svn storage was added for a file, its an initial commit
				svnInitiallyCommittedJavaFiles.add(file);
			}
		}
	}

	private void calculateSavedConflictEditorIDs() {
		//Detect files saved from a conflict editor and remove them from changedJavaFiles
		//such that they are not considered for a regular save
		IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
		if (activePage != null) {
			IEditorReference[] editorReferences= activePage.getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				IEditorPart editor= editorReference.getEditor(false);
				if (editor != null && EditorHelper.isConflictEditor(editor) && !editor.isDirty()) {
					CompareEditor compareEditor= (CompareEditor)editor;
					if (dirtyConflictEditors.contains(compareEditor)) {
						dirtyConflictEditors.remove(compareEditor);
						savedConflictEditorIDs.add(EditorHelper.getConflictEditorID(compareEditor));
						changedJavaFiles.remove(EditorHelper.getEditedJavaFile(compareEditor));
					}
				}
			}
		}
	}

	private void calculateSavedAndExternallyModifiedJavaFiles() {
		for (IFile file : changedJavaFiles) {
			if (!updatedJavaFiles.contains(file)) { //updated files are neither saved nor externally modified
				if (isRefactoring || FileHelper.isFileBufferSynchronized(file)) {
					savedJavaFiles.add(file);
				} else {
					externallyModifiedJavaFiles.add(file);
				}
			}
		}
	}

	private void recordSets() {
		operationRecorder.recordSavedFiles(savedJavaFiles, isRefactoring);
		operationRecorder.recordSavedConflictEditors(savedConflictEditorIDs);
		operationRecorder.recordExternallyModifiedFiles(externallyModifiedJavaFiles);
		operationRecorder.recordUpdatedFiles(updatedJavaFiles);
		operationRecorder.recordCommittedFiles(svnInitiallyCommittedJavaFiles, true, true);
		operationRecorder.recordCommittedFiles(cvsInitiallyCommittedJavaFiles, true, false);
		operationRecorder.recordCommittedFiles(svnCommittedJavaFiles, false, true);
		operationRecorder.recordCommittedFiles(cvsCommittedJavaFiles, false, false);
	}

	private void updateDirtyAndKnownFiles() {
		removedJavaFiles.addAll(updatedJavaFiles); //updated files become unknown (like removed)
		removedJavaFiles.addAll(externallyModifiedJavaFiles); //externally modified files become unknown
		operationRecorder.removeKnownFiles(removedJavaFiles);
	}

	/**
	 * Returns null if there is no corresponding Java source file (e.g. when the SVN file is not
	 * from text-base folder).
	 * 
	 * @param svnFile
	 * @return
	 */
	private IFile getJavaSourceFileForSVNFile(IFile svnFile) {
		IFile javaSourceFile= null;
		String fileName= svnFile.getName();
		if (fileName.endsWith(".java.svn-base")) {
			IPath fileFullPath= svnFile.getFullPath();
			String parentDir= fileFullPath.segment(fileFullPath.segmentCount() - 2);
			if (parentDir.equals("text-base")) {
				String javaSourceFileName= fileName.substring(0, fileName.lastIndexOf("."));
				IPath javaSourceFilePath= fileFullPath.removeLastSegments(3).append(javaSourceFileName);
				javaSourceFile= ResourcesPlugin.getWorkspace().getRoot().getFile(javaSourceFilePath);
			}
		}
		return javaSourceFile;
	}

	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource= delta.getResource();
			if (resource.getType() == IResource.FILE) {
				IFile file= (IFile)resource;
				if (file.getName().equals("entries") && file.getParent().getName().equals(".svn")) {
					svnEntriesChangeSet.add(file);
				} else if (file.getName().equals("Entries") && file.getParent().getName().equals("CVS")) {
					if (delta.getKind() == IResourceDelta.ADDED) {
						cvsEntriesAddedSet.add(file);
					} else {
						cvsEntriesChangedOrRemovedSet.add(file);
					}
				} else {
					String fileExtension= file.getFileExtension(); //may be null
					if ("java".equals(fileExtension)) {
						visitJavaFile(delta, file);
					} else if ("svn-base".equals(fileExtension)) {
						visitSVNBaseFile(delta, file);
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

		private void visitSVNBaseFile(IResourceDelta delta, IFile file) {
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					IFile javaSourceFile= getJavaSourceFileForSVNFile(file);
					if (javaSourceFile != null) {
						svnAddedJavaFiles.add(javaSourceFile);
					}
					break;
				case IResourceDelta.CHANGED:
					if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
						javaSourceFile= getJavaSourceFileForSVNFile(file);
						if (javaSourceFile != null) {
							svnChangedJavaFiles.add(javaSourceFile);
						}
					}
					break;
			}
		}

	}

}
