/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ReorganizedResourceOperation extends BreakableResourceOperation {

	protected String destinationPath;

	protected int updateFlags;


	public ReorganizedResourceOperation() {
		super();
	}

	public ReorganizedResourceOperation(IResource resource, IPath destination, int updateFlags, boolean success) {
		super(resource, success);
		destinationPath= destination.toPortableString();
		this.updateFlags= updateFlags;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(destinationPath);
		textChunk.append(updateFlags);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		destinationPath= operationLexer.getNextLexeme();
		updateFlags= Integer.valueOf(operationLexer.getNextLexeme());
	}

	@Override
	public void replayBreakableResourceOperation() throws CoreException {
		IResource resource= ResourceHelper.findWorkspaceMember(resourcePath);
		if (resource != null && !isIgnored(resource)) {
			findOrCreateParent(destinationPath);
			replayReorganizedResourceOperation(resource);
		}
	}

	private boolean isIgnored(IResource resource) {
		return resource instanceof IFile && !ResourceHelper.isJavaFile((IFile)resource);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Destination path: " + destinationPath + "\n");
		sb.append("Update flags: " + updateFlags + "\n");
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
