/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

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
		isDeleted= operationLexer.readBoolean();
	}

	@Override
	public void replay() throws CoreException {
		IResource resource= findResource();
		if (resource != null) {
			resource.delete(true, null);
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
