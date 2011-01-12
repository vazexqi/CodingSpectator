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
public class UpdatedFileOperation extends FileOperation {

	public UpdatedFileOperation() {
		super();
	}

	public UpdatedFileOperation(IFile updatedFile) {
		super(updatedFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_UPDATED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Updated file";
	}

}
