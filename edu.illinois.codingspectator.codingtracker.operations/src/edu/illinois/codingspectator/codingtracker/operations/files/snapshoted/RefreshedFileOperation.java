/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import java.io.IOException;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class RefreshedFileOperation extends SnapshotedFileOperation {

	private String replacedText;

	public RefreshedFileOperation() {
		super();
	}

	public RefreshedFileOperation(IFile refreshedFile, String replacedText) {
		super(refreshedFile);
		this.replacedText= replacedText;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_REFRESHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Refreshed file";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(replacedText);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		replacedText= operationLexer.getNextLexeme();
	}

	@Override
	public void replay() throws CoreException {
		ITextEditor fileEditor= getExistingEditor();
		if (fileEditor != null) {//If file editor exists, check the presence of the replaced text
			IDocument editedDocument= getEditedDocument(fileEditor);
			if (!replacedText.equals(editedDocument.get())) {
				throw new RuntimeException("Replaced text of a refreshed file is not present in the document: " + this);
			}
		} else {//If file editor does not exist, create a file with the replaced text and open an editor for it
			createCompilationUnit(replacedText);
			createEditor();
			activateEditor(currentEditor);
		}
		refresh();
	}

	private void refresh() throws CoreException {
		IPath fullFilePath= new Path(filePath);
		IResource fileResource= FileHelper.findWorkspaceMember(fullFilePath);
		if (fileResource == null || !fileResource.exists()) {
			throw new RuntimeException("Unsupported replay. Refreshed file does not exist: " + this);
		}
		try {
			FileHelper.writeFileContent(FileHelper.getFileForResource(fileResource), fileContent, false);
		} catch (IOException e) {
			throw new RuntimeException("Could not write content to the refreshed file: " + this, e);
		}
		IFileBuffer fileBuffer= FileHelper.getFileBuffer(fullFilePath);
		if (fileBuffer == null) {
			throw new RuntimeException("Could not find file buffer for the refreshed file: " + this);
		}
		fileBuffer.revert(new NullProgressMonitor());
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Replaced text: " + replacedText + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
