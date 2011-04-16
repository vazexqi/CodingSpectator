/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class EditedUnsychronizedFileOperation extends FileOperation {

	private String editorContent;

	public EditedUnsychronizedFileOperation() {
		super();
	}

	public EditedUnsychronizedFileOperation(IFile editedFile, String editorContent) {
		super(editedFile);
		this.editorContent= editorContent;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_EDITED_UNSYNCHRONIZED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Edited unsynchronized file";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(editorContent);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		editorContent= operationLexer.getNextLexeme();
	}

	@Override
	public void replay() throws CoreException {
		ITextEditor fileEditor= EditorHelper.getExistingEditor(filePath);
		if (fileEditor != null) { //File editor exists
			EditorHelper.activateEditor(fileEditor);
			IDocument editedDocument= EditorHelper.getEditedDocument(fileEditor);
			if (!editorContent.equals(editedDocument.get())) {
				throw new RuntimeException("The text of the unsychronized editor is wrong: " + this);
			}
		} else {
			IResource editedFile= FileHelper.findWorkspaceMember(filePath);
			if (editedFile == null || !editedFile.exists()) {
				createCompilationUnit(editorContent);
			}
			fileEditor= EditorHelper.openEditor(filePath);
			EditorHelper.getEditedDocument(fileEditor).set(editorContent);
		}
		currentEditor= fileEditor;
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Editor content: " + editorContent + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
