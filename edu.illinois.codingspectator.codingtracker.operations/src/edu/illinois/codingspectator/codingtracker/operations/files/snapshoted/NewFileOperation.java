/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

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

	@Override
	public void replay() throws CoreException {
		//Do not replay redundant new file operations (when a file's editor is open, the new file operation is considered redundant, 
		//because it is a side effect of refreshing, e.g. saving an unknown file, or keeping editing a file without refreshing it). 
		if (getExistingEditor() == null) {
			super.replay();
		}
	}


}
