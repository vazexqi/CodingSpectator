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
public class ClosedFileOperation extends FileOperation {

	public ClosedFileOperation() {
		super();
	}

	public ClosedFileOperation(IFile closedFile) {
		super(closedFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_CLOSED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "File closed";
	}

}
