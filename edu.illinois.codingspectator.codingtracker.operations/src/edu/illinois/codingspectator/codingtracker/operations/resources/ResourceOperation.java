/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;

import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ResourceOperation extends UserOperation {

	protected String resourcePath;

	public ResourceOperation() {
		super();
	}

	public ResourceOperation(IResource resource) {
		super();
		resourcePath= ResourceHelper.getPortableResourcePath(resource);
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(resourcePath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		resourcePath= operationLexer.getNextLexeme();
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Resource path: " + resourcePath + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
