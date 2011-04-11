/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.files.FileOperation;

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
		fileContent= FileHelper.readFileContent(snapshotedFile);
	}

	public SnapshotedFileOperation(IFile snapshotedFile, String charsetName) {
		super(snapshotedFile);
		fileContent= FileHelper.readFileContent(snapshotedFile, charsetName);
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(fileContent);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		fileContent= operationLexer.getNextLexeme();
	}

	@Override
	public void replay() throws CoreException {
		createCompilationUnit(fileContent);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("File content: " + fileContent + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

	protected void checkSnapshotMatchesTheExistingFile() {
		IResource workspaceResource= FileHelper.findWorkspaceMember(filePath);
		if (workspaceResource != null) {
			if (!fileContent.equals(FileHelper.readFileContent((IFile)workspaceResource))) {
				throw new RuntimeException("The snapshot file does not match the existing file: " + filePath);
			}
		}
	}

}
