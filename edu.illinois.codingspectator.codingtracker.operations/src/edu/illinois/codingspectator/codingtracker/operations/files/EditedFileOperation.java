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
public class EditedFileOperation extends FileOperation {

	public EditedFileOperation() {
		super();
	}

	public EditedFileOperation(IFile editedFile) {
		super(editedFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_EDITED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "File edited: ";
	}

}
