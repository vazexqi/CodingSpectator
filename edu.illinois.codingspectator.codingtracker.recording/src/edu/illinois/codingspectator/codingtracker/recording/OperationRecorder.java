/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.ClosedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.OpenedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.SavedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ClosedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ExternallyModifiedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.FileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.RefactoredSavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.CVSCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.CVSInitiallyCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.SVNCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.snapshoted.SVNInitiallyCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestCaseFinishedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestCaseStartedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestSessionFinishedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestSessionLaunchedOperation;
import edu.illinois.codingspectator.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingspectator.codingtracker.operations.options.ProjectOptionsChangedOperation;
import edu.illinois.codingspectator.codingtracker.operations.options.WorkspaceOptionsChangedOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.PerformedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.RefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.UndoneRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.LaunchedApplicationOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.StartedEclipseOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.StartedRefactoringOperation;
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

	public void recordChangedText(DocumentEvent documentEvent, String replacedText, IFile editedFile, boolean isUndoing, boolean isRedoing) {
		if (!editedFile.equals(lastEditedFile) || !knownfilesRecorder.isFileKnown(editedFile)) {
			lastEditedFile= editedFile;
			ensureIsKnownFile(lastEditedFile);
			recordEditedFile();
		}
		Debugger.debugDocumentEvent(documentEvent, replacedText);
		TextChangeOperation textChangeOperation= null;
		if (isUndoing) {
			textChangeOperation= new UndoneTextChangeOperation(documentEvent, replacedText);
		} else if (isRedoing) {
			textChangeOperation= new RedoneTextChangeOperation(documentEvent, replacedText);
		} else {
			textChangeOperation= new PerformedTextChangeOperation(documentEvent, replacedText);
		}
		TextRecorder.record(textChangeOperation);
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

	private void recordEditedFile() {
		TextRecorder.record(new EditedFileOperation(lastEditedFile));
	}

	public void recordOpenedConflictEditor(String editorID, IFile editedFile, String initialContent) {
		TextRecorder.record(new OpenedConflictEditorOperation(editorID, editedFile, initialContent));
	}

	public void recordSavedFiles(Set<IFile> savedFiles, boolean isRefactoring) {
		for (IFile file : savedFiles) {
			FileOperation fileOperation= null;
			if (isRefactoring) {
				fileOperation= new RefactoredSavedFileOperation(file);
			} else {
				fileOperation= new SavedFileOperation(file);
			}
			TextRecorder.record(fileOperation);
		}
	}

	public void recordSavedConflictEditors(Set<String> savedConflictEditorIDs) {
		for (String editorID : savedConflictEditorIDs) {
			TextRecorder.record(new SavedConflictEditorOperation(editorID));
		}
	}

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
				knownfilesRecorder.addKnownfile(file);
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

	public void recordStartedRefactoring() {
		TextRecorder.record(new StartedRefactoringOperation());
	}

	public void recordExecutedRefactoring(RefactoringDescriptor refactoringDescriptor, int eventType) {
		Debugger.debugRefactoringDescriptor(refactoringDescriptor);
		RefactoringOperation refactoringOperation= null;
		switch (eventType) {
			case RefactoringExecutionEvent.PERFORMED:
				refactoringOperation= new PerformedRefactoringOperation(refactoringDescriptor);
				break;
			case RefactoringExecutionEvent.REDONE:
				refactoringOperation= new RedoneRefactoringOperation(refactoringDescriptor);
				break;
			case RefactoringExecutionEvent.UNDONE:
				refactoringOperation= new UndoneRefactoringOperation(refactoringDescriptor);
				break;
		}
		TextRecorder.record(refactoringOperation);
	}

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

	private void ensureIsKnownFile(IFile file) {
		//TODO: Is creating a new HashSet for a single file too expensive?
		Set<IFile> files= new HashSet<IFile>(1);
		files.add(file);
		ensureAreKnownFiles(files);
	}

	public void ensureAreKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			//TODO: Is it possible to have a known file, whose CVS/Entries is not known? If not, merge the following two if statements.
			IFile cvsEntriesFile= getCVSEntriesForFile(file);
			if (cvsEntriesFile != null && !knownfilesRecorder.isFileKnown(cvsEntriesFile)) {
				knownfilesRecorder.addCVSEntriesFile(cvsEntriesFile);
				hasChanged= true;
			}
			if (!knownfilesRecorder.isFileKnown(file)) {
				knownfilesRecorder.addKnownfile(file);
				hasChanged= true;
				//save the content of a previously unknown file
				if (file.getLocation().toFile().exists()) { //Actually, should always exist here
					TextRecorder.record(new NewFileOperation(file));
				}
			}
		}
		if (hasChanged) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	private IFile getCVSEntriesForFile(IFile file) {
		IPath cvsEntriesPath= file.getFullPath().removeLastSegments(1).append("CVS").append("Entries");
		IResource cvsEntriesResource= FileHelper.findWorkspaceMemeber(cvsEntriesPath);
		if (cvsEntriesResource != null) {
			return (IFile)cvsEntriesResource;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void ensureOptionsAreCurrent(IJavaProject javaProject) {
		Map<String, String> workspaceOptions= JavaCore.getOptions();
		if (!knownfilesRecorder.areWorkspaceOptionsCurrent(workspaceOptions)) {
			knownfilesRecorder.recordWorkspaceOptions(workspaceOptions);
			TextRecorder.record(new WorkspaceOptionsChangedOperation(workspaceOptions));
		}
		Map<String, String> projectOptions= javaProject.getOptions(false);
		String projectName= javaProject.getElementName();
		if (!knownfilesRecorder.areProjectOptionsCurrent(projectName, projectOptions)) {
			knownfilesRecorder.recordProjectOptions(projectName, projectOptions);
			TextRecorder.record(new ProjectOptionsChangedOperation(projectName, projectOptions));
		}
	}

}
