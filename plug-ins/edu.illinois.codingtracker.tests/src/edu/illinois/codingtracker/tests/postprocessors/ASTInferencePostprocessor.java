/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.RefreshedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;
import edu.illinois.codingtracker.operations.options.OptionsChangedOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.references.ReferencingProjectsChangedOperation;
import edu.illinois.codingtracker.operations.resources.ResourceOperation;
import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ASTInferenceTextRecorder;


/**
 * This class infers AST operations and records them along with the original user operations.
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ASTInferencePostprocessor extends CodingTrackerPostprocessor {

	private List<TextChangeOperation> bufferedTextChanges= new LinkedList<TextChangeOperation>();

	private List<UserOperation> refactoringPredecessors= new LinkedList<UserOperation>();


	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		for (int i= 0; i < userOperations.size(); i++) {
			UserOperation userOperation= userOperations.get(i);
			if (userOperation instanceof TextChangeOperation) {
				handleTextChangeOperation((TextChangeOperation)userOperation);
			} else if (userOperation instanceof OptionsChangedOperation || userOperation instanceof ReferencingProjectsChangedOperation) {
				refactoringPredecessors.add(userOperation);
			} else if (userOperation instanceof NewStartedRefactoringOperation) {
				if (((NewStartedRefactoringOperation)userOperation).isRename() && doBufferedTextChangesFormCycle()) {
					//Do not replay buffered changes since they are assisting a rename refactoring.
					applyAccumulatedOperations(false);
				} else {
					applyAccumulatedOperations(true);
				}
				replayAndRecord(userOperation);
			} else {
				applyAccumulatedOperations(true);
				if (userOperation instanceof NewFileOperation) {
					handleNewFileOperation((NewFileOperation)userOperation);
				} else if (userOperation instanceof CommittedFileOperation) {
					handleCommittedFileOperation((CommittedFileOperation)userOperation);
				} else if (userOperation instanceof RefreshedFileOperation) {
					handleRefreshedFileOperation((RefreshedFileOperation)userOperation);
				} else {
					//TODO: Also, consider that some code change operations replace the whole file content with a new content.
					//Some of these operations are accompanying refresh file operations, some are performed manually, but in
					//both scenarios it could be beneficial to represent the edit on a finer grained scale using 
					//SnapshotDifferenceCalculator the same way as for the snapshot-based operations.
					replayAndRecord(userOperation);
				}
			}
		}
		applyAccumulatedOperations(true); //Just in case
	}

	private void handleTextChangeOperation(TextChangeOperation textChangeOperation) {
		if (bufferedTextChanges.isEmpty()) {
			bufferedTextChanges.add(textChangeOperation);
		} else if (bufferedTextChanges.size() == 1) {
			TextChangeOperation lastTextChangeOperation= bufferedTextChanges.get(0);
			if (lastTextChangeOperation.isPossiblyCorrelatedWith(textChangeOperation)) {
				bufferedTextChanges.add(textChangeOperation);
			} else {
				replayAndRecord(lastTextChangeOperation);
				bufferedTextChanges.set(0, textChangeOperation);
			}
		} else {
			bufferedTextChanges.add(textChangeOperation);
		}
	}

	private void applyAccumulatedOperations(boolean shouldReplayBufferedTextChanges) {
		for (TextChangeOperation textChangeOperation : bufferedTextChanges) {
			if (shouldReplayBufferedTextChanges) {
				replayAndRecord(textChangeOperation);
			} else {
				record(textChangeOperation);
			}
		}
		for (UserOperation refactoringPredecessor : refactoringPredecessors) {
			replayAndRecord(refactoringPredecessor);
		}
		bufferedTextChanges.clear();
		refactoringPredecessors.clear();
	}

	private boolean doBufferedTextChangesFormCycle() {
		if (bufferedTextChanges.isEmpty()) {
			return true;
		}
		TextChangeOperation firstChange= bufferedTextChanges.get(0);
		TextChangeOperation lastChange= bufferedTextChanges.get(bufferedTextChanges.size() - 1);
		if (!firstChange.isUndoneBy(lastChange)) {
			return false;
		}
		String initialContent= firstChange.getEditedText();
		IDocument editedDocument= new Document(initialContent);
		for (TextChangeOperation changeOperation : bufferedTextChanges) {
			try {
				editedDocument.replace(changeOperation.getOffset(), changeOperation.getLength(), changeOperation.getNewText());
			} catch (BadLocationException e) {
				return false;
			}
		}
		return initialContent.equals(editedDocument.get());
	}

	private void handleNewFileOperation(NewFileOperation newFileOperation) {
		//Note that there is no need to return to the previously edited file, since NewFileOperations are always
		//succeeded by edit operations of the same file.
		handleOneStepSnapshotedFileOperation(newFileOperation, true, false);
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		boolean willReplaceFile= ResourceOperation.isExternallyModifiedResource(committedFileOperation.getResourcePath());
		handleOneStepSnapshotedFileOperation(committedFileOperation, willReplaceFile, true);
	}

	private void handleOneStepSnapshotedFileOperation(SnapshotedFileOperation snapshotedFileOperation, boolean willReplaceFile, boolean shouldRestoreOriginalEditor) {
		IResource workspaceResource= ResourceHelper.findWorkspaceMember(snapshotedFileOperation.getResourcePath());
		if (workspaceResource instanceof IFile && willReplaceFile) {
			IFile editedFile= (IFile)workspaceResource;
			String currentContent= ResourceHelper.readFileContent(editedFile);
			String newContent= snapshotedFileOperation.getFileContent();
			replaySnapshotsAsEdits(snapshotedFileOperation, editedFile, new String[] { currentContent, newContent }, shouldRestoreOriginalEditor);
		} else { //Resource does not exist or is not a file.
			replayAndRecord(snapshotedFileOperation);
		}
	}

	private void handleRefreshedFileOperation(RefreshedFileOperation refreshedFileOperation) {
		String replacedText= refreshedFileOperation.getReplacedText();
		String newContent= refreshedFileOperation.getFileContent();
		IFile refreshedFile= findOrCreateRefreshedCompilationUnit(refreshedFileOperation);
		String currentContent= getRefreshedFileCurrentContent(refreshedFile, replacedText);
		if (currentContent.equals(newContent)) {
			return; //Nothing to do, the resulting content is already there.
		}
		replaySnapshotsAsEdits(refreshedFileOperation, refreshedFile, new String[] { currentContent, replacedText, newContent }, true);
	}

	private void replaySnapshotsAsEdits(SnapshotedFileOperation snapshotedFileOperation, IFile editedFile, String[] snapshots, boolean shouldRestoreOriginalEditor) {
		long timestamp= snapshotedFileOperation.getTime();
		List<PerformedTextChangeOperation> editDifference= SnapshotDifferenceCalculator.getEditDifference(snapshots[0], snapshots[1], timestamp);
		for (int i= 1; i < snapshots.length - 1; i++) {
			editDifference.addAll(SnapshotDifferenceCalculator.getEditDifference(snapshots[i], snapshots[i + 1], timestamp));
		}
		replayEditDifference(editDifference, editedFile, timestamp, shouldRestoreOriginalEditor);
		record(snapshotedFileOperation);
	}

	private void replayEditDifference(List<PerformedTextChangeOperation> editDifference, IFile editedFile, long timestamp, boolean shouldRestoreOriginalEditor) {
		if (editDifference.size() > 0) {
			IEditorPart originalEditor= null;
			if (shouldRestoreOriginalEditor) {
				originalEditor= EditorHelper.getActiveEditor();
			}
			EditedFileOperation editedFileOperation= new EditedFileOperation(editedFile, timestamp);
			replayAndRecord(editedFileOperation);
			for (PerformedTextChangeOperation editDifferenceOperation : editDifference) {
				replayAndRecord(editDifferenceOperation);
			}
			SavedFileOperation savedFileOperation= new SavedFileOperation(editedFile, true, timestamp);
			replayAndRecord(savedFileOperation);
			if (shouldRestoreOriginalEditor) {
				restoreOriginalEditor(originalEditor, timestamp);
			}
		}
	}

	private IFile findOrCreateRefreshedCompilationUnit(RefreshedFileOperation refreshedFileOperation) {
		String resourcePath= refreshedFileOperation.getResourcePath();
		IResource workspaceResource= ResourceHelper.findWorkspaceMember(resourcePath);
		if (workspaceResource == null) {
			try {
				refreshedFileOperation.createCompilationUnit(refreshedFileOperation.getReplacedText());
			} catch (CoreException e) {
				throw new RuntimeException("Could not create compilation unit: " + refreshedFileOperation, e);
			}
			workspaceResource= ResourceHelper.findWorkspaceMember(resourcePath);
		}
		return (IFile)workspaceResource;
	}

	private String getRefreshedFileCurrentContent(IFile refreshedFile, String replacedText) {
		String currentContent= ResourceHelper.readFileContent(refreshedFile);
		String resourcePath= ResourceHelper.getPortableResourcePath(refreshedFile);
		try {
			ITextEditor fileEditor= EditorHelper.getExistingEditor(resourcePath);
			if (fileEditor != null && fileEditor.isDirty()) {
				currentContent= EditorHelper.getEditedDocument(fileEditor).get();
				if (!currentContent.equals(replacedText)) {
					throw new RuntimeException("Replaced text of a refreshed file is not present in the document: " + replacedText);
				}
			}
		} catch (PartInitException e) {
			throw new RuntimeException("Could not get the existing editor for resource: " + resourcePath, e);
		}
		return currentContent;
	}

	private void restoreOriginalEditor(IEditorPart originalEditor, long timestamp) {
		if (originalEditor != null) {
			IFile originalFile= null;
			if (originalEditor instanceof CompareEditor) {
				originalFile= EditorHelper.getEditedJavaFile((CompareEditor)originalEditor);
			} else if (originalEditor instanceof AbstractDecoratedTextEditor) {
				originalFile= EditorHelper.getEditedJavaFile((AbstractDecoratedTextEditor)originalEditor);
			}
			if (originalFile != null) {
				EditedFileOperation editedOriginalFileOperation= new EditedFileOperation(originalFile, timestamp);
				replayAndRecord(editedOriginalFileOperation);
			} else {
				EditorHelper.activateEditor(originalEditor);
			}
		}
	}

	private void replayAndRecord(UserOperation userOperation) {
		//First replay and then record TextChangeOperations and vice versa for all other operations in order to preserve
		//the right ordering (i.e. ASTOperation follow operation(s) that caused it) and the right timestamp (i.e. ASTOperation
		//has the timestamp of the last operation that caused it).
		if (userOperation instanceof TextChangeOperation) {
			replay(userOperation);
			record(userOperation);
		} else {
			record(userOperation);
			replay(userOperation);
		}
	}

	private void replay(UserOperation userOperation) {
		System.out.println("Replaying operation: " + userOperation.generateSerializationText());
		try {
			userOperation.replay();
		} catch (Exception e) {
			throw new RuntimeException("Could not replay user operation: " + userOperation, e);
		}
	}

	private void record(UserOperation userOperation) {
		ASTInferenceTextRecorder.record(userOperation);
	}

	@Override
	protected File getResultRecordFile() {
		return astMainRecordFile;
	}

}
