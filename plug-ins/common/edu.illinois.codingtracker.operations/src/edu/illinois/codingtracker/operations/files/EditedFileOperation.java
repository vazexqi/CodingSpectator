/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.operations.OperationSymbols;

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

	public EditedFileOperation(IFile editedFile, long timestamp) {
		super(editedFile, timestamp);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_EDITED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Edited file";
	}

	@Override
	public void replay() throws CoreException {
		currentEditor= EditorHelper.openEditor(resourcePath);
	}

}
