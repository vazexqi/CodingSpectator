/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.ClosedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.OpenedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.SavedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ClosedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.EditedUnsychronizedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ExternallyModifiedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.CVSCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.CVSInitiallyCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.RefreshedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.SVNCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.SVNInitiallyCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestCaseFinishedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestCaseStartedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestSessionFinishedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestSessionLaunchedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingspectator.codingtracker.operations.options.ProjectOptionsChangedOperation;
import edu.illinois.codingspectator.codingtracker.operations.options.WorkspaceOptionsChangedOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.NewStartedRefactoringOperation.Mode;
import edu.illinois.codingspectator.codingtracker.operations.references.ReferencingProjectsChangedOperation;
import edu.illinois.codingspectator.codingtracker.operations.resources.CopiedResourceOperation;
import edu.illinois.codingspectator.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.LaunchedApplicationOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.StartedEclipseOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.ConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.PerformedConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.RedoneConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.RedoneTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.UndoneConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.UndoneTextChangeOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class OperationRecorder {

	private static volatile OperationRecorder recorderInstance= null;

	private static final KnownfilesRecorder knownfilesRecorder= KnownfilesRecorder.getInstance();

	private IFile lastEditedFile= null;


	public static OperationRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new OperationRecorder();
		}
		return recorderInstance;
	}

	private OperationRecorder() {
		TextRecorder.record(new StartedEclipseOperation());
	}

	public void recordRefreshedFile(IFile refreshedFile, String replacedText) {
		boolean isFileKnown= knownfilesRecorder.isFileKnown(refreshedFile, true);
		if (!isFileKnown) {
			ensureFileIsKnown(refreshedFile, false);
		}
		TextRecorder.record(new RefreshedFileOperation(refreshedFile, replacedText, isFileKnown));
	}

	public void recordChangedText(DocumentEvent documentEvent, String replacedText, String oldDocumentText, IFile editedFile,
									boolean isUndoing, boolean isRedoing) {
		if (ResourceHelper.isFileBufferNotSynchronized(editedFile)) {
			if (!editedFile.equals(lastEditedFile)) {
				recordEditedUnsynchronizedFile(editedFile, oldDocumentText);
			}
		} else {
			ITextFileBuffer textFileBuffer= ResourceHelper.getTextFileBuffer(editedFile.getFullPath());
			if (textFileBuffer != null && textFileBuffer.getEncoding() != null) {
				ensureFileIsKnown(editedFile, true, textFileBuffer.getEncoding());
			} else {
				ensureFileIsKnown(editedFile, true);
			}
			if (!editedFile.equals(lastEditedFile)) {
				recordEditedFile(editedFile);
			}
		}
		lastEditedFile= editedFile;
		Debugger.debugDocumentEvent(documentEvent, replacedText);
		TextRecorder.record(getTextChangeOperation(documentEvent, replacedText, isUndoing, isRedoing));
	}

	private TextChangeOperation getTextChangeOperation(DocumentEvent documentEvent, String replacedText, boolean isUndoing, boolean isRedoing) {
		TextChangeOperation textChangeOperation= null;
		if (isUndoing) {
			textChangeOperation= new UndoneTextChangeOperation(documentEvent, replacedText);
		} else if (isRedoing) {
			textChangeOperation= new RedoneTextChangeOperation(documentEvent, replacedText);
		} else {
			textChangeOperation= new PerformedTextChangeOperation(documentEvent, replacedText);
		}
		return textChangeOperation;
	}

	public void recordConflictEditorChangedText(DocumentEvent documentEvent, String replacedText, String editorID, boolean isUndoing, boolean isRedoing) {
		ConflictEditorTextChangeOperation conflictEditorTextChangeOperation= null;
		if (isUndoing) {
			conflictEditorTextChangeOperation= new UndoneConflictEditorTextChangeOperation(editorID, documentEvent, replacedText);
		} else if (isRedoing) {
			conflictEditorTextChangeOperation= new RedoneConflictEditorTextChangeOperation(editorID, documentEvent, replacedText);
		} else {
			conflictEditorTextChangeOperation= new PerformedConflictEditorTextChangeOperation(editorID, documentEvent, replacedText);
		}
		TextRecorder.record(conflictEditorTextChangeOperation);
	}

	private void recordEditedUnsynchronizedFile(IFile editedFile, String editorContent) {
		TextRecorder.record(new EditedUnsychronizedFileOperation(editedFile, editorContent));
	}

	private void recordEditedFile(IFile editedFile) {
		TextRecorder.record(new EditedFileOperation(editedFile));
	}

	public void recordOpenedConflictEditor(String editorID, IFile editedFile, String initialContent) {
		ensureFileIsKnown(editedFile, true);
		TextRecorder.record(new OpenedConflictEditorOperation(editorID, editedFile, initialContent));
	}

	public void recordMovedResource(IResource movedResource, IPath destination, int updateFlags, boolean success) {
		TextRecorder.record(new MovedResourceOperation(movedResource, destination, updateFlags, success));
	}

	public void recordCopiedResource(IResource movedResource, IPath destination, int updateFlags, boolean success) {
		TextRecorder.record(new CopiedResourceOperation(movedResource, destination, updateFlags, success));
	}

	//	public void recordSavedFiles(Set<IFile> savedFiles, boolean isRefactoring) {
//		for (IFile file : savedFiles) {
//			FileOperation fileOperation= null;
//			if (isRefactoring) {
//				fileOperation= new RefactoredSavedFileOperation(file);
//			} else {
//				fileOperation= new SavedFileOperation(file);
//			}
//			TextRecorder.record(fileOperation);
//		}
//		if (!isRefactoring) {
//			//TODO: Saving does not mean the file is known if its encoding differs from the saved editor encoding
//			//Could look for the cases when the encoding is the same
//			//ensureFilesAreKnown(savedFiles, false);
//		}
//	}

	public void recordSavedFile(IFile savedFile, boolean success) {
		TextRecorder.record(new SavedFileOperation(savedFile, success));
		//TODO: Saving does not mean the file is known if its encoding differs from the saved editor encoding
		//But, could look for the cases when the encoding is the same
		//ensureFileIsKnown(savedFile, false);
	}

	public void recordSavedCompareEditor(CompareEditor compareEditor, boolean success) {
		TextRecorder.record(new SavedConflictEditorOperation(EditorHelper.getConflictEditorID(compareEditor), success));
		//TODO: Saving does not mean the file is known if its encoding differs from the saved conflict editor encoding
		//But, could look for the cases when the encoding is the same
		//ensureFileIsKnown(EditorHelper.getEditedJavaFile(compareEditor), false);
	}

//	public void recordSavedConflictEditors(Set<String> savedConflictEditorIDs, Set<IFile> savedFiles) {
//		for (String editorID : savedConflictEditorIDs) {
//			TextRecorder.record(new SavedConflictEditorOperation(editorID));
//		}
//		//TODO: Saving does not mean the file is known if its encoding differs from the saved conflict editor encoding
//		//Could look for the cases when the encoding is the same
//		//ensureFilesAreKnown(savedFiles, false);
//	}

	public void recordExternallyModifiedFiles(Set<IFile> externallyModifiedFiles) {
		for (IFile file : externallyModifiedFiles) {
			TextRecorder.record(new ExternallyModifiedFileOperation(file));
		}
	}

	public void recordUpdatedFiles(Set<IFile> updatedFiles) {
		for (IFile file : updatedFiles) {
			TextRecorder.record(new UpdatedFileOperation(file));
		}
	}

	/**
	 * Records the committed files including their content.
	 * 
	 * @param committedFiles
	 * @param isInitialCommit
	 * @param isSVNCommit
	 */
	public void recordCommittedFiles(Set<IFile> committedFiles, boolean isInitialCommit, boolean isSVNCommit) {
		if (committedFiles.size() > 0) {
			for (IFile file : committedFiles) {
				if (isInitialCommit) {
					if (isSVNCommit) {
						TextRecorder.record(new SVNInitiallyCommittedFileOperation(file));
					} else {
						TextRecorder.record(new CVSInitiallyCommittedFileOperation(file));
					}
				} else {
					if (isSVNCommit) {
						TextRecorder.record(new SVNCommittedFileOperation(file));
					} else {
						TextRecorder.record(new CVSCommittedFileOperation(file));
					}
				}
				knownfilesRecorder.addKnownfile(file, ResourceHelper.getCharsetNameForFile(file));
			}
			knownfilesRecorder.recordKnownfiles();
		}
	}

	public void recordClosedFile(IFile file) {
		if (file.equals(lastEditedFile)) {
			lastEditedFile= null;
		}
		TextRecorder.record(new ClosedFileOperation(file));
	}

	public void recordClosedConflictEditor(String editorID) {
		TextRecorder.record(new ClosedConflictEditorOperation(editorID));
	}

	public void recordLaunchedTestSession(String testRunName, String launchedProjectName) {
		TextRecorder.record(new TestSessionLaunchedOperation(testRunName, launchedProjectName));
	}

	public void recordStartedTestSession(String testRunName) {
		TextRecorder.record(new TestSessionStartedOperation(testRunName));
	}

	public void recordFinishedTestSession(String testRunName) {
		TextRecorder.record(new TestSessionFinishedOperation(testRunName));
	}

	public void recordStartedTestCase(String testRunName, String testClassName, String testMethodName) {
		TextRecorder.record(new TestCaseStartedOperation(testRunName, testClassName, testMethodName));
	}

	public void recordFinishedTestCase(String testRunName, String result) {
		TextRecorder.record(new TestCaseFinishedOperation(testRunName, result));
	}

	public void recordLaunchedApplication(String launchMode, String launchName, String application, String product, boolean useProduct) {
		TextRecorder.record(new LaunchedApplicationOperation(launchMode, launchName, application, product, useProduct));
	}

	public void recordStartedRefactoring(RefactoringDescriptor refactoringDescriptor, int eventType) {
		Mode mode= null;
		switch (eventType) {
			case RefactoringExecutionEvent.ABOUT_TO_PERFORM:
				mode= Mode.PERFORM;
				break;
			case RefactoringExecutionEvent.ABOUT_TO_REDO:
				mode= Mode.REDO;
				break;
			case RefactoringExecutionEvent.ABOUT_TO_UNDO:
				mode= Mode.UNDO;
				break;
		}
		TextRecorder.record(new NewStartedRefactoringOperation(mode, refactoringDescriptor));
	}

	public void recordFinishedRefactoring(boolean success) {
		TextRecorder.record(new FinishedRefactoringOperation(success));
	}

//	public void recordExecutedRefactoring(RefactoringDescriptor refactoringDescriptor, int eventType) {
//		Debugger.debugRefactoringDescriptor(refactoringDescriptor);
//		RefactoringOperation refactoringOperation= null;
//		switch (eventType) {
//			case RefactoringExecutionEvent.PERFORMED:
//				refactoringOperation= new PerformedRefactoringOperation(refactoringDescriptor);
//				break;
//			case RefactoringExecutionEvent.REDONE:
//				refactoringOperation= new RedoneRefactoringOperation(refactoringDescriptor);
//				break;
//			case RefactoringExecutionEvent.UNDONE:
//				refactoringOperation= new UndoneRefactoringOperation(refactoringDescriptor);
//				break;
//		}
//		TextRecorder.record(refactoringOperation);
//	}

	public void removeKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			Object removed= knownfilesRecorder.removeKnownfile(file);
			if (removed != null) {
				hasChanged= true;
			}
		}
		if (hasChanged) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	private void ensureFileIsKnown(IFile file, boolean snapshotIfWasNotKnown) {
		ensureFileIsKnown(file, snapshotIfWasNotKnown, ResourceHelper.getCharsetNameForFile(file));
	}

	private void ensureFileIsKnown(IFile file, boolean snapshotIfWasNotKnown, String charsetName) {
		//TODO: Is creating a new HashMap for a single file too expensive?
		Map<IFile, String> fileMap= new HashMap<IFile, String>(1);
		fileMap.put(file, charsetName);
		ensureFilesAreKnown(fileMap, snapshotIfWasNotKnown);
	}

	public void ensureFilesAreKnown(Map<IFile, String> fileMap, boolean snapshotIfWasNotKnown) {
		boolean hasChanged= false;
		for (Entry<IFile, String> entry : fileMap.entrySet()) {
			//TODO: Is it possible to have a known file, whose CVS/Entries is not known? If not, merge the following two if statements.
			IFile cvsEntriesFile= getCVSEntriesForFile(entry.getKey());
			if (cvsEntriesFile != null && !knownfilesRecorder.isFileKnown(cvsEntriesFile, false)) {
				knownfilesRecorder.addCVSEntriesFile(cvsEntriesFile);
				hasChanged= true;
			}
			if (!knownfilesRecorder.isFileKnown(entry.getKey(), entry.getValue(), true)) {
				knownfilesRecorder.addKnownfile(entry.getKey(), entry.getValue());
				hasChanged= true;
				//save the content of a previously unknown file
				if (snapshotIfWasNotKnown && entry.getKey().exists()) { //Actually, should always exist here
					TextRecorder.record(new NewFileOperation(entry.getKey(), entry.getValue()));
				}
			}
		}
		if (hasChanged) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	private IFile getCVSEntriesForFile(IFile file) {
		IPath cvsEntriesPath= file.getFullPath().removeLastSegments(1).append("CVS").append("Entries");
		IResource cvsEntriesResource= ResourceHelper.findWorkspaceMember(cvsEntriesPath);
		if (cvsEntriesResource != null) {
			return (IFile)cvsEntriesResource;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void ensureOptionsAreCurrent(Set<IJavaProject> javaProjects) {
		ensureWorkspaceOptionsAreCurrent();
		for (IJavaProject javaProject : javaProjects) {
			Map<String, String> projectOptions= javaProject.getOptions(false);
			String projectName= javaProject.getElementName();
			if (!knownfilesRecorder.areProjectOptionsCurrent(projectName, projectOptions)) {
				knownfilesRecorder.recordProjectOptions(projectName, projectOptions);
				TextRecorder.record(new ProjectOptionsChangedOperation(projectName, projectOptions));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void ensureWorkspaceOptionsAreCurrent() {
		Map<String, String> workspaceOptions= JavaCore.getOptions();
		if (!knownfilesRecorder.areWorkspaceOptionsCurrent(workspaceOptions)) {
			knownfilesRecorder.recordWorkspaceOptions(workspaceOptions);
			TextRecorder.record(new WorkspaceOptionsChangedOperation(workspaceOptions));
		}
	}

	public void ensureReferencingProjectsAreCurrent(String projectName, Set<String> referencingProjectNames) {
		if (!knownfilesRecorder.areReferencingProjectsCurrent(projectName, referencingProjectNames)) {
			knownfilesRecorder.recordReferencingProjects(projectName, referencingProjectNames);
			TextRecorder.record(new ReferencingProjectsChangedOperation(projectName, referencingProjectNames));
		}
	}

}
