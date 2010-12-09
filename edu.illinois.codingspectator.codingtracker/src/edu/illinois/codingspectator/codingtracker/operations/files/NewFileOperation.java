/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class NewFileOperation extends SnapshotedFileOperation {

	public NewFileOperation(IFile newFile) {
		super(newFile);
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.FILE_NEW_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "New file: ";
	}

}
