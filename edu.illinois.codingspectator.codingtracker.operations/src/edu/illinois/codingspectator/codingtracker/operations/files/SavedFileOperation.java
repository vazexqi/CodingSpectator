/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.resources.BreakableResourceOperation;

/**
 * Note: This is an exception in the class hierarchy, because SavedFileOperation should extends
 * FileOperation. But, it is needed to avoid duplicating the code required for breakable operations
 * (as well as creating artificial multiple inheritance in Java).
 * 
 * @author Stas Negara
 * 
 */
public class SavedFileOperation extends BreakableResourceOperation {

	public SavedFileOperation() {
		super();
	}

	public SavedFileOperation(IFile savedFile, boolean success) {
		super(savedFile, success);
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
	public void replayBreakableResourceOperation() throws CoreException {
		EditorHelper.getExistingEditor(resourcePath).doSave(null);
		//FIXME: Instead of sleeping, should listen to IProgressMonitor.done()
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//do nothing
		}
	}

}
