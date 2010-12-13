/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.ClosedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.OpenedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.SavedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ClosedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.CommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ExternallyModifiedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.FileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.InitiallyCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.NewFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.RefactoredSavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.PerformedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.RefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.UndoneRefactoringOperation;
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

	private static final TextRecorder textRecorder= TextRecorder.getInstance();

	private IFile lastEditedFile= null;


	public static OperationRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new OperationRecorder();
		}
		return recorderInstance;
	}

	private OperationRecorder() {
		textRecorder.record(new StartedEclipseOperation());
	}

	public void recordChangedText(TextEvent textEvent, IFile editedFile, boolean isUndoing, boolean isRedoing) {
		if (!editedFile.equals(lastEditedFile) || !knownfilesRecorder.isFileKnown(editedFile)) {
			lastEditedFile= editedFile;
			ensureIsKnownFile(lastEditedFile);
			recordEditedFile();
		}
		Debugger.debugTextEvent(textEvent);
		TextChangeOperation textChangeOperation= null;
		if (isUndoing) {
			textChangeOperation= new UndoneTextChangeOperation(textEvent);
		} else if (isRedoing) {
			textChangeOperation= new RedoneTextChangeOperation(textEvent);
		} else {
			textChangeOperation= new PerformedTextChangeOperation(textEvent);
		}
		textRecorder.record(textChangeOperation);
	}

	public void recordConflictEditorChangedText(TextEvent textEvent, String editorID, boolean isUndoing, boolean isRedoing) {
		ConflictEditorTextChangeOperation conflictEditorTextChangeOperation= null;
		if (isUndoing) {
			conflictEditorTextChangeOperation= new UndoneConflictEditorTextChangeOperation(editorID, textEvent);
		} else if (isRedoing) {
			conflictEditorTextChangeOperation= new RedoneConflictEditorTextChangeOperation(editorID, textEvent);
		} else {
			conflictEditorTextChangeOperation= new PerformedConflictEditorTextChangeOperation(editorID, textEvent);
		}
		textRecorder.record(conflictEditorTextChangeOperation);
	}

	private void recordEditedFile() {
		textRecorder.record(new EditedFileOperation(lastEditedFile));
	}

	public void recordOpenedConflictEditor(String editorID, IFile editedFile, String initialContent) {
		textRecorder.record(new OpenedConflictEditorOperation(editorID, editedFile, initialContent));
	}

	public void recordSavedFiles(Set<IFile> savedFiles, boolean isRefactoring) {
		for (IFile file : savedFiles) {
			FileOperation fileOperation= null;
			if (isRefactoring) {
				fileOperation= new RefactoredSavedFileOperation(file);
			} else {
				fileOperation= new SavedFileOperation(file);
			}
			textRecorder.record(fileOperation);
		}
	}

	public void recordSavedConflictEditors(Set<String> savedConflictEditorIDs) {
		for (String editorID : savedConflictEditorIDs) {
			textRecorder.record(new SavedConflictEditorOperation(editorID));
		}
	}

	public void recordExternallyModifiedFiles(Set<IFile> externallyModifiedFiles) {
		for (IFile file : externallyModifiedFiles) {
			textRecorder.record(new ExternallyModifiedFileOperation(file));
		}
	}

	public void recordUpdatedFiles(Set<IFile> updatedFiles) {
		for (IFile file : updatedFiles) {
			textRecorder.record(new UpdatedFileOperation(file));
		}
	}

	/**
	 * Records the committed files including their content.
	 * 
	 * @param committedFiles
	 * @param isInitialCommit
	 */
	public void recordCommittedFiles(Set<IFile> committedFiles, boolean isInitialCommit) {
		if (committedFiles.size() > 0) {
			for (IFile file : committedFiles) {
				if (isInitialCommit) {
					textRecorder.record(new InitiallyCommittedFileOperation(file));
				} else {
					textRecorder.record(new CommittedFileOperation(file));
				}
				knownfilesRecorder.addKnownfile(file);
			}
			knownfilesRecorder.recordKnownfiles();
		}
	}

	public void recordClosedFile(IFile file) {
		textRecorder.record(new ClosedFileOperation(file));
	}

	public void recordClosedConflictEditor(String editorID) {
		textRecorder.record(new ClosedConflictEditorOperation(editorID));
	}

	public void recordStartedRefactoring() {
		textRecorder.record(new StartedRefactoringOperation());
	}

	public void recordExecutedRefactoring(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
		Debugger.debugRefactoringDescriptor(refactoringDescriptor);
		RefactoringOperation refactoringOperation= null;
		switch (event.getEventType()) {
			case RefactoringExecutionEvent.PERFORMED:
				refactoringOperation= new PerformedRefactoringOperation(refactoringDescriptor);
				break;
			case RefactoringExecutionEvent.REDONE:
				refactoringOperation= new RedoneRefactoringOperation(refactoringDescriptor);
				break;
			case RefactoringExecutionEvent.UNDONE:
				refactoringOperation= new UndoneRefactoringOperation(refactoringDescriptor);
				break;
			default:
				Exception e= new RuntimeException();
				Debugger.logExceptionToErrorLog(e, Messages.Recorder_UnrecognizedRefactoringType + event.getEventType());
		}
		textRecorder.record(refactoringOperation);
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
			if (!knownfilesRecorder.isFileKnown(file)) {
				knownfilesRecorder.addKnownfile(file);
				hasChanged= true;
				//save the content of a previously unknown file
				if (new File(file.getLocation().toOSString()).exists()) { //Actually, should always exist here
					textRecorder.record(new NewFileOperation(file));
				}
			}
		}
		if (hasChanged) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	public void commitStarted() {
		textRecorder.commitStarted();
	}

	public void commitCompleted() {
		textRecorder.commitCompleted();
	}

}
