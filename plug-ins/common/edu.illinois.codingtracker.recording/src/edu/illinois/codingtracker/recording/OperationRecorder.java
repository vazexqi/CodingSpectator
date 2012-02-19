/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

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

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.FileRevision;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.conflicteditors.ClosedConflictEditorOperation;
import edu.illinois.codingtracker.operations.conflicteditors.OpenedConflictEditorOperation;
import edu.illinois.codingtracker.operations.conflicteditors.SavedConflictEditorOperation;
import edu.illinois.codingtracker.operations.files.ClosedFileOperation;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.files.EditedUnsychronizedFileOperation;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CVSCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CVSInitiallyCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.RefreshedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNInitiallyCommittedFileOperation;
import edu.illinois.codingtracker.operations.junit.TestCaseFinishedOperation;
import edu.illinois.codingtracker.operations.junit.TestCaseStartedOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionFinishedOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionLaunchedOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.options.ProjectOptionsChangedOperation;
import edu.illinois.codingtracker.operations.options.WorkspaceOptionsChangedOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.operations.references.ReferencingProjectsChangedOperation;
import edu.illinois.codingtracker.operations.resources.CopiedResourceOperation;
import edu.illinois.codingtracker.operations.resources.CreatedResourceOperation;
import edu.illinois.codingtracker.operations.resources.DeletedResourceOperation;
import edu.illinois.codingtracker.operations.resources.ExternallyModifiedResourceOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.operations.starts.LaunchedApplicationOperation;
import edu.illinois.codingtracker.operations.textchanges.ConflictEditorTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.PerformedConflictEditorTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.RedoneConflictEditorTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.RedoneTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.UndoneConflictEditorTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.UndoneTextChangeOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class OperationRecorder {

	private static volatile OperationRecorder recorderInstance= null;

	private static final KnownFilesRecorder knownFilesRecorder= KnownFilesRecorder.getInstance();

	private static final ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	private IFile lastEditedFile= null;


	public static OperationRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new OperationRecorder();
		}
		return recorderInstance;
	}

	private OperationRecorder() {
	}

	public void recordRefreshedFile(IFile refreshedFile, String replacedText) {
		boolean isFileKnown= knownFilesRecorder.isFileKnown(refreshedFile, true);
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

	/**
	 * Note that editedFile might be null.
	 * 
	 * @param editorID
	 * @param editedFile
	 * @param initialContent
	 */
	public void recordOpenedConflictEditor(String editorID, IFile editedFile, String initialContent) {
		String editedFilePath= "";
		if (editedFile != null) {
			ensureFileIsKnown(editedFile, true);
			editedFilePath= ResourceHelper.getPortableResourcePath(editedFile);
		}
		TextRecorder.record(new OpenedConflictEditorOperation(editorID, editedFilePath, initialContent));
	}

	public void recordCreatedResource(IResource createdResource, int updateFlags, boolean success) {
		if (success && createdResource instanceof IFile) {
			ensureFileIsKnown((IFile)createdResource, false);
		}
		TextRecorder.record(new CreatedResourceOperation(createdResource, updateFlags, success));
		astRecorder.recordASTOperationForCreatedResource(createdResource, success);
	}

	public void recordMovedResource(IResource movedResource, IPath destination, int updateFlags, boolean success) {
		invalidateLastEditedFile(movedResource);
		knownFilesRecorder.moveKnownFiles(movedResource, destination, success);
		TextRecorder.record(new MovedResourceOperation(movedResource, destination, updateFlags, success));
		astRecorder.recordASTOperationForMovedResource(movedResource, destination, success);
	}

	public void recordCopiedResource(IResource copiedResource, IPath destination, int updateFlags, boolean success) {
		knownFilesRecorder.copyKnownFiles(copiedResource, destination, success);
		TextRecorder.record(new CopiedResourceOperation(copiedResource, destination, updateFlags, success));
		astRecorder.recordASTOperationForCopiedResource(copiedResource, destination, success);
	}

	public void recordDeletedResource(IResource deletedResource, int updateFlags, boolean success) {
		invalidateLastEditedFile(deletedResource);
		knownFilesRecorder.removeKnownFilesForResource(deletedResource);
		TextRecorder.record(new DeletedResourceOperation(deletedResource, updateFlags, success));
		astRecorder.recordASTOperationForDeletedResource(deletedResource, success);
	}

	public void recordExternallyModifiedFiles(Set<IFile> externallyModifiedJavaFiles, boolean areDeleted) {
		for (IFile externallyModifiedJavaFile : externallyModifiedJavaFiles) {
			TextRecorder.record(new ExternallyModifiedResourceOperation(externallyModifiedJavaFile, areDeleted));
			if (areDeleted) {
				astRecorder.recordASTOperationForDeletedResource(externallyModifiedJavaFile, true);
			}
		}
	}

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

	public void recordUpdatedFiles(Set<FileRevision> updatedFileRevisions) {
		for (FileRevision fileRevision : updatedFileRevisions) {
			TextRecorder.record(new UpdatedFileOperation(fileRevision.getFile(), fileRevision.getRevision(), fileRevision.getCommittedRevision()));
		}
	}

	/**
	 * Records the committed files including their content.
	 * 
	 * @param committedFileRevisions
	 * @param isInitialCommit
	 * @param isSVNCommit
	 */
	public void recordCommittedFiles(Set<FileRevision> committedFileRevisions, boolean isInitialCommit, boolean isSVNCommit) {
		if (committedFileRevisions.size() > 0) {
			for (FileRevision fileRevision : committedFileRevisions) {
				IFile file= fileRevision.getFile();
				String revision= fileRevision.getRevision();
				String committedRevision= fileRevision.getCommittedRevision();
				if (isInitialCommit) {
					if (isSVNCommit) {
						TextRecorder.record(new SVNInitiallyCommittedFileOperation(file, revision, committedRevision));
					} else {
						TextRecorder.record(new CVSInitiallyCommittedFileOperation(file, revision, committedRevision));
					}
				} else {
					if (isSVNCommit) {
						TextRecorder.record(new SVNCommittedFileOperation(file, revision, committedRevision));
					} else {
						TextRecorder.record(new CVSCommittedFileOperation(file, revision, committedRevision));
					}
				}
				knownFilesRecorder.addKnownFile(file, ResourceHelper.getCharsetNameForFile(file));
			}
			knownFilesRecorder.recordKnownFiles();
		}
	}

	public void recordClosedFile(IFile file) {
		invalidateLastEditedFile(file);
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
		RefactoringMode mode= null;
		switch (eventType) {
			case RefactoringExecutionEvent.ABOUT_TO_PERFORM:
				mode= RefactoringMode.PERFORM;
				break;
			case RefactoringExecutionEvent.ABOUT_TO_REDO:
				mode= RefactoringMode.REDO;
				break;
			case RefactoringExecutionEvent.ABOUT_TO_UNDO:
				mode= RefactoringMode.UNDO;
				break;
		}
		TextRecorder.record(new NewStartedRefactoringOperation(mode, refactoringDescriptor));
	}

	public void recordFinishedRefactoring(boolean success) {
		TextRecorder.record(new FinishedRefactoringOperation(success));
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
			if (cvsEntriesFile != null && !knownFilesRecorder.isFileKnown(cvsEntriesFile, false)) {
				knownFilesRecorder.addCVSEntriesFile(cvsEntriesFile);
				hasChanged= true;
			}
			if (!knownFilesRecorder.isFileKnown(entry.getKey(), entry.getValue(), true)) {
				knownFilesRecorder.addKnownFile(entry.getKey(), entry.getValue());
				hasChanged= true;
				//save the content of a previously unknown file
				if (snapshotIfWasNotKnown && entry.getKey().exists()) { //TODO: Remove after ensured in ResourceHelper: Actually, should always exist here
					TextRecorder.record(new NewFileOperation(entry.getKey(), entry.getValue()));
				}
			}
		}
		if (hasChanged) {
			knownFilesRecorder.recordKnownFiles();
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
			if (!knownFilesRecorder.areProjectOptionsCurrent(projectName, projectOptions)) {
				knownFilesRecorder.recordProjectOptions(projectName, projectOptions);
				TextRecorder.record(new ProjectOptionsChangedOperation(projectName, projectOptions));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void ensureWorkspaceOptionsAreCurrent() {
		Map<String, String> workspaceOptions= JavaCore.getOptions();
		if (!knownFilesRecorder.areWorkspaceOptionsCurrent(workspaceOptions)) {
			knownFilesRecorder.recordWorkspaceOptions(workspaceOptions);
			TextRecorder.record(new WorkspaceOptionsChangedOperation(workspaceOptions));
		}
	}

	public void ensureReferencingProjectsAreCurrent(String projectName, Set<String> referencingProjectNames) {
		if (!knownFilesRecorder.areReferencingProjectsCurrent(projectName, referencingProjectNames)) {
			knownFilesRecorder.recordReferencingProjects(projectName, referencingProjectNames);
			TextRecorder.record(new ReferencingProjectsChangedOperation(projectName, referencingProjectNames));
		}
	}

	private void invalidateLastEditedFile(IResource resource) {
		if (lastEditedFile != null && resource.getFullPath().isPrefixOf(lastEditedFile.getFullPath())) {
			lastEditedFile= null;
		}
	}

}
