/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class NewFileOperation extends SnapshotedFileOperation {

	public NewFileOperation() {
		super();
	}

	public NewFileOperation(IFile newFile, String charsetName) {
		super(newFile, charsetName);
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
