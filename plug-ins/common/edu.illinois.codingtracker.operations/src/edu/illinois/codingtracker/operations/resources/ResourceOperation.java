/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.helpers.ResourceOperationHelper;

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
		this(resource, System.currentTimeMillis());
	}

	public ResourceOperation(IResource resource, long timestamp) {
		super(timestamp);
		resourcePath= ResourceHelper.getPortableResourcePath(resource);
	}

	public ResourceOperation(String resourcePath, long timestamp) {
		super(timestamp);
		this.resourcePath= resourcePath;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(resourcePath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		resourcePath= operationLexer.readString();
	}


	public void createCompilationUnit(String content) throws CoreException {
		ResourceOperationHelper.createCompilationUnit(content, resourcePath);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Resource path: " + resourcePath + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
