/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.operations.OperationSymbols;

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
			//If not in test mode, explicitly close the editors of the files that are contained in the deleted resource such that the replayer 
			//does not complain about the wrong editor, and do it before the resource is deleted such that the affected files still exist
			if (!isInTestMode) {
				for (ITextEditor fileEditor : EditorHelper.getExistingEditors(resourcePath)) {
					//Don't use getFileEditor().close(false), because it is executed asynchronously 
					fileEditor.getSite().getPage().closeEditor(fileEditor, false);
				}
			}
			resource.delete(updateFlags, null);
		}
	}

}
