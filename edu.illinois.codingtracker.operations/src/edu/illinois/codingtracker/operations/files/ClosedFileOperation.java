/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.operations.OperationSymbols;

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
		return "Closed file";
	}

	@Override
	public void replay() throws CoreException {
		ITextEditor fileEditor= EditorHelper.getExistingEditor(resourcePath);
		if (fileEditor != null) {
			//Don't use getFileEditor().close(false), because it is executed asynchronously 
			fileEditor.getSite().getPage().closeEditor(fileEditor, false);
		}
	}

}
