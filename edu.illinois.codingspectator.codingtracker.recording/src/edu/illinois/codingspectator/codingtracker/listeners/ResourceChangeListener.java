/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
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

	private final Set<String> svnEntriesChangeSet= new HashSet<String>();


	//Calculated sets:

	private final Set<IFile> savedJavaFiles= new HashSet<IFile>();

	//Actually, should not be more than one per resourceChanged notification
	private final Set<String> savedConflictEditorIDs= new HashSet<String>();

	private final Set<IFile> externallyModifiedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> updatedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> initiallyCommittedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> committedJavaFiles= new HashSet<IFile>();


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
		savedJavaFiles.clear();
		savedConflictEditorIDs.clear();
		externallyModifiedJavaFiles.clear();
		updatedJavaFiles.clear();
		initiallyCommittedJavaFiles.clear();
		committedJavaFiles.clear();
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
		calculateSVNSets();
		calculateSavedConflictEditorIDs();
		calculateSavedAndExternallyModifiedJavaFiles();
	}

	private void calculateSVNSets() {
		for (IFile file : svnChangedJavaFiles) {
			if (changedJavaFiles.contains(file)) {
				updatedJavaFiles.add(file); //if both the java file and its svn storage have changed, then its an update
			} else {
				committedJavaFiles.add(file); //if only svn storage of a java file has changed, its a commit
			}
		}
		for (IFile file : svnAddedJavaFiles) {
			if (!addedJavaFiles.contains(file)) { //if only svn storage was added for a file, its an initial commit
				initiallyCommittedJavaFiles.add(file);
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
		boolean isSVNEntriesChanged= svnEntriesChangeSet.size() > 0;
		for (IFile file : changedJavaFiles) {
			if (!updatedJavaFiles.contains(file)) { //updated files are neither saved nor externally modified
				if (isRefactoring || dirtyFiles.contains(file) && !isSVNEntriesChanged) {
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
		operationRecorder.recordCommittedFiles(initiallyCommittedJavaFiles, true);
		operationRecorder.recordCommittedFiles(committedJavaFiles, false);
	}

	private void updateDirtyAndKnownFiles() {
		dirtyFiles.removeAll(removedJavaFiles);
		//TODO: Removing from dirty files when updated or changed externally may cause subsequent save to be treated as an
		//external modification. Is it ok (e.g. this can be detected and filtered out during the replay phase)?
		dirtyFiles.removeAll(changedJavaFiles);
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
				if (file.getName().equals("entries")) {
					svnEntriesChangeSet.add("yes");
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
