/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class DeletedResourceOperation extends UpdatedResourceOperation {

	public DeletedResourceOperation() {
		super();
	}

	public DeletedResourceOperation(IResource resource, int updateFlags, boolean success) {
		super(resource, updateFlags, success);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.RESOURCE_DELETED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Deleted resource";
	}

	@Override
	public void replayBreakableResourceOperation() throws CoreException {
		IResource resource= findResource();
		if (resource != null) {
			resource.delete(updateFlags, null);

			//Explicitly close the editor of the deleted file such that the replayer does not complain about the wrong editor
			//Note: this code is duplicated (with minor change) from ClosedFileOperation.replay()
			ITextEditor fileEditor= EditorHelper.getExistingEditor(resourcePath);
			if (fileEditor != null && fileEditor == currentEditor) {
				//Don't use getFileEditor().close(false), because it is executed asynchronously 
				fileEditor.getSite().getPage().closeEditor(fileEditor, false);
			}
		}
	}

}
