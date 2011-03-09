/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SavedFileOperation extends FileOperation {

	public SavedFileOperation() {
		super();
	}

	public SavedFileOperation(IFile savedFile) {
		super(savedFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_SAVED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Saved file";
	}

	@Override
	public void replay() throws CoreException {
		getFileEditor(false).doSave(null);
	}

}
