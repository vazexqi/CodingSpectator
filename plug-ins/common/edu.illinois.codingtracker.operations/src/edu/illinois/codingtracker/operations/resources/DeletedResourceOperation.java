/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.compare.helpers.EditorHelper;
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
			//does not complain about the wrong editor, and do it before the resource is deleted such that the affected files still exist.
			if (!Configuration.isInTestMode) {
				EditorHelper.closeAllEditorsForResource(resourcePath);
			}
			//Do not use updateFlags, since sometimes this will result in keeping the project's files on the disk, which
			//could break the following move operations.
			resource.delete(IResource.FORCE, null);
		}
	}

}
