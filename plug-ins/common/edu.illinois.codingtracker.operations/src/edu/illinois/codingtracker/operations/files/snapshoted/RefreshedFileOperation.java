/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files.snapshoted;

import java.io.IOException;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class RefreshedFileOperation extends SnapshotedFileOperation {

	public static boolean isReplaying= false;

	private String replacedText;

	private boolean isCausedByConflictEditorSave;

	public RefreshedFileOperation() {
		super();
	}

	public RefreshedFileOperation(IFile refreshedFile, String replacedText, boolean isCausedByConflictEditorSave) {
		super(refreshedFile);
		this.replacedText= replacedText;
		this.isCausedByConflictEditorSave= isCausedByConflictEditorSave;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_REFRESHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Refreshed file";
	}

	public String getReplacedText() {
		return replacedText;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(replacedText);
		textChunk.append(isCausedByConflictEditorSave);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		replacedText= operationLexer.readString();
		isCausedByConflictEditorSave= operationLexer.readBoolean();
	}

	@Override
	public void replay() throws CoreException {
		isReplaying= true;
		performReplaying();
		isReplaying= false;
	}

	private void performReplaying() throws CoreException {
		ITextEditor fileEditor= EditorHelper.getExistingEditor(resourcePath);
		if (fileEditor != null) { //File editor exists
			IDocument editedDocument= EditorHelper.getEditedDocument(fileEditor);
			if (isCausedByConflictEditorSave && !fileEditor.isDirty()) {
				//Check the presence of the new text 
				if (!fileContent.equals(editedDocument.get())) {
					throw new RuntimeException("New text of a refreshed file is not present in the document: " + this);
				}
				return; //Nothing else to do, the file was already refreshed by Eclipse
			} else { //Check the presence of the replaced text
				if (!replacedText.equals(editedDocument.get())) {
					throw new RuntimeException("Replaced text of a refreshed file is not present in the document: " + this);
				}
			}
		} else {//If file editor does not exist, create a file with the replaced text and open an editor for it
			createCompilationUnit(replacedText);
			EditorHelper.createEditor(resourcePath);
			//If there is a current editor, restore it.
			if (currentEditor != null) {
				EditorHelper.activateEditor(currentEditor);
			}
		}
		refresh();
	}

	private void refresh() throws CoreException {
		IPath fullFilePath= new Path(resourcePath);
		IResource fileResource= ResourceHelper.findWorkspaceMember(fullFilePath);
		ResourceHelper.checkResourceExists(fileResource, "Unsupported replay. Refreshed file does not exist: " + this);
		try {
			ResourceHelper.writeFileContent(ResourceHelper.getFileForResource(fileResource), fileContent, false);
		} catch (IOException e) {
			throw new RuntimeException("Could not write content to the refreshed file: " + this, e);
		}
		ITextFileBuffer textFileBuffer= ResourceHelper.getTextFileBuffer(fullFilePath);
		if (textFileBuffer == null) {
			throw new RuntimeException("Could not find file buffer for the refreshed file: " + this);
		}
		textFileBuffer.revert(new NullProgressMonitor());
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Replaced text: " + replacedText + "\n");
		sb.append("Is caused by conflict editor save: " + isCausedByConflictEditorSave + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
