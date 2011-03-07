/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class NewFileOperation extends SnapshotedFileOperation {

	public NewFileOperation() {
		super();
	}

	public NewFileOperation(IFile newFile) {
		super(newFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_NEW_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "New file";
	}

}
