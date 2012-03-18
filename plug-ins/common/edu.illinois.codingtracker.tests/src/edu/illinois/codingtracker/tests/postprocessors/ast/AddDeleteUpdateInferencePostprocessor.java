/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast;

import java.util.LinkedList;
import java.util.List;

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

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
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
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.helpers.SnapshotDifferenceCalculator;


/**
 * This class infers three basic AST operations (add, delete, and update) and records them along
 * with the original user operations.
 * 
 * @author Stas Negara
 * 
 */
public class AddDeleteUpdateInferencePostprocessor extends ASTPostprocessor {

	private List<TextChangeOperation> bufferedTextChanges= new LinkedList<TextChangeOperation>();

	private List<UserOperation> refactoringPredecessors= new LinkedList<UserOperation>();


	@Override
	protected String getResultFilePostfix() {
		return ".inferred_ast_operations";
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
		record(snapshotedFileOperation);
		IResource workspaceResource= ResourceHelper.findWorkspaceMember(snapshotedFileOperation.getResourcePath());
		if (workspaceResource instanceof IFile && willReplaceFile) {
			IFile editedFile= (IFile)workspaceResource;
			String currentContent= ResourceHelper.readFileContent(editedFile);
			String newContent= snapshotedFileOperation.getFileContent();
			replaySnapshotsAsEdits(snapshotedFileOperation, editedFile, new String[] { currentContent, newContent }, shouldRestoreOriginalEditor);
		} else { //Resource does not exist or is not a file.
			replay(snapshotedFileOperation);
		}
	}

	private void handleRefreshedFileOperation(RefreshedFileOperation refreshedFileOperation) {
		record(refreshedFileOperation);
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
		long timestamp= snapshotedFileOperation.getTime() - 1; //Subtracting 1ms we mark the added operations.
		List<PerformedTextChangeOperation> snapshotDifference= new LinkedList<PerformedTextChangeOperation>();
		for (int i= 0; i < snapshots.length - 1; i++) {
			snapshotDifference.addAll(SnapshotDifferenceCalculator.getSnapshotDifference(snapshots[i], snapshots[i + 1], timestamp));
		}
		replaySnapshotDifference(snapshotDifference, editedFile, timestamp, shouldRestoreOriginalEditor);
	}

	private void replaySnapshotDifference(List<PerformedTextChangeOperation> snapshotDifference, IFile editedFile, long timestamp, boolean shouldRestoreOriginalEditor) {
		if (snapshotDifference.size() > 0) {
			ASTOperationRecorder.isReplayingSnapshotDifference= true;
			IEditorPart originalEditor= null;
			if (shouldRestoreOriginalEditor) {
				originalEditor= UserOperation.getCurrentEditor();
			}
			EditedFileOperation editedFileOperation= new EditedFileOperation(editedFile, timestamp);
			replayAndRecord(editedFileOperation);
			for (PerformedTextChangeOperation editDifferenceOperation : snapshotDifference) {
				replayAndRecord(editDifferenceOperation);
			}
			SavedFileOperation savedFileOperation= new SavedFileOperation(editedFile, true, timestamp);
			replayAndRecord(savedFileOperation);
			if (shouldRestoreOriginalEditor) {
				restoreOriginalEditor(originalEditor, timestamp);
			}
			ASTOperationRecorder.isReplayingSnapshotDifference= false;
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
			if (originalEditor instanceof AbstractDecoratedTextEditor) {
				originalFile= EditorHelper.getEditedJavaFile((AbstractDecoratedTextEditor)originalEditor);
			} else {
				throw new RuntimeException("The original editor is of the wrong type: " + originalEditor);
			}
			if (originalFile != null) {
				EditedFileOperation editedOriginalFileOperation= new EditedFileOperation(originalFile, timestamp);
				replayAndRecord(editedOriginalFileOperation);
			} else {
				throw new RuntimeException("Could not retrieve the edited file from the original editor: " + originalEditor);
			}
		}
	}

}
