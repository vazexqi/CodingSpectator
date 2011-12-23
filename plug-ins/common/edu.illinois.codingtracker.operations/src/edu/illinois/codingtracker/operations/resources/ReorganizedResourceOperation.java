/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ReorganizedResourceOperation extends UpdatedResourceOperation {

	protected String destinationPath;


	public ReorganizedResourceOperation() {
		super();
	}

	public ReorganizedResourceOperation(IResource resource, IPath destination, int updateFlags, boolean success) {
		super(resource, updateFlags, success);
		destinationPath= destination.toPortableString();
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(destinationPath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		destinationPath= operationLexer.readString();
	}

	@Override
	public void replayBreakableResourceOperation() throws CoreException {
		IResource resource= findResource();
		if (resource != null) {
			findOrCreateParent(destinationPath);
			replayReorganizedResourceOperation(resource);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Destination path: " + destinationPath + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

	/**
	 * 
	 * @param resource is guaranteed to NOT be null
	 * @throws CoreException
	 */
	protected abstract void replayReorganizedResourceOperation(IResource resource) throws CoreException;

}
