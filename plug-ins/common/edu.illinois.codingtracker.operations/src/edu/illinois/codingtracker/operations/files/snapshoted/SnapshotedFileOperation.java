/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.files.FileOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class SnapshotedFileOperation extends FileOperation {

	protected String fileContent;

	public SnapshotedFileOperation() {
		super();
	}

	public SnapshotedFileOperation(IFile snapshotedFile) {
		super(snapshotedFile);
		fileContent= ResourceHelper.readFileContent(snapshotedFile);
	}

	public SnapshotedFileOperation(IFile snapshotedFile, String charsetName) {
		super(snapshotedFile);
		fileContent= ResourceHelper.readFileContent(snapshotedFile, charsetName);
	}

	public String getFileContent() {
		return fileContent;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(fileContent);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		fileContent= operationLexer.readString();
	}

	@Override
	public void replay() throws CoreException {
		createCompilationUnit(fileContent);
		removeExternallyModifiedResource(resourcePath);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("File content: " + fileContent + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
