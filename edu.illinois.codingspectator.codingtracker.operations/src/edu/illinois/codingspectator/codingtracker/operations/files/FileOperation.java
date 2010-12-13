/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class FileOperation extends UserOperation {

	private String filePath;

	public FileOperation() {
		super();
	}

	public FileOperation(IFile file) {
		super();
		this.filePath= FileHelper.getPortableFilePath(file);
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(filePath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		filePath= operationLexer.getNextLexeme();
	}

}
