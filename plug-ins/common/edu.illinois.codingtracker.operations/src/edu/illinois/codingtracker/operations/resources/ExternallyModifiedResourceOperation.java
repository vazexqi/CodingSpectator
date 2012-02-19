/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ExternallyModifiedResourceOperation extends ResourceOperation {

	private boolean isDeleted;


	public ExternallyModifiedResourceOperation() {
		super();
	}

	public ExternallyModifiedResourceOperation(IResource externallyModifiedResource, boolean isDeleted) {
		super(externallyModifiedResource);
		this.isDeleted= isDeleted;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.RESOURCE_EXTERNALLY_MODIFIED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Externally modified resource";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(isDeleted);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		if (!Configuration.isOldFormat) {
			isDeleted= operationLexer.readBoolean();
		} else {
			isDeleted= false;
		}
	}

	@Override
	public void replay() throws CoreException {
		IResource resource= findResource();
		if (resource != null) {
			if (isDeleted) {
				//To avoid confusing the replayer that tracks the currently active editor, close the editor (if any) 
				//of the deleted resource.
				EditorHelper.closeAllEditorsForResource(resourcePath);
				resource.delete(IResource.FORCE, null);
			} else {
				addExternallyModifiedResource(resourcePath);
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Is deleted: " + isDeleted + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
