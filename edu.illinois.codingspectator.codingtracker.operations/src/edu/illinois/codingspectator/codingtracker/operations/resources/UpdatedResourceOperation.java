/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class UpdatedResourceOperation extends BreakableResourceOperation {

	protected int updateFlags;


	public UpdatedResourceOperation() {
		super();
	}

	public UpdatedResourceOperation(IResource resource, int updateFlags, boolean success) {
		super(resource, success);
		this.updateFlags= updateFlags;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(updateFlags);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		updateFlags= Integer.valueOf(operationLexer.getNextLexeme());
	}

	@Override
	public void replayBreakableResourceOperation() throws CoreException {
		IResource resource= ResourceHelper.findWorkspaceMember(resourcePath);
		if (resource != null && !isIgnored(resource)) {
			replayUpdatedResourceOperation(resource);
		}
	}

	private boolean isIgnored(IResource resource) {
		return resource instanceof IFile && !ResourceHelper.isJavaFile((IFile)resource);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Update flags: " + updateFlags + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

	/**
	 * 
	 * @param resource is guaranteed to NOT be null
	 * @throws CoreException
	 */
	protected abstract void replayUpdatedResourceOperation(IResource resource) throws CoreException;

}
