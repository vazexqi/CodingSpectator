/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

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
	public void replayUpdatedResourceOperation(IResource resource) throws CoreException {
		resource.delete(updateFlags, null);
	}

}
