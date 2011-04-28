/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.breakable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SavedFileOperation extends BreakableFileOperation {

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
	public void replayBreakableFileOperation() throws CoreException {
		EditorHelper.getExistingEditor(filePath).doSave(null);
		//FIXME: Instead of sleeping, should listen to IProgressMonitor.done()
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//do nothing
		}
	}

}
