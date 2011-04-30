/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class MovedResourceOperation extends BreakableResourceOperation {

	private String destinationPath;

	private int updateFlags;


	public MovedResourceOperation() {
		super();
	}

	public MovedResourceOperation(IResource resource, IPath destination, int updateFlags, boolean success) {
		super(resource, success);
		destinationPath= destination.toPortableString();
		this.updateFlags= updateFlags;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.RESOURCE_MOVED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Moved resource";
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
		if (resource != null) {
			if (resource instanceof Project) {
				Project project= (Project)resource;
				IProjectDescription description= project.getDescription();
				description.setName(destinationPath.substring(1)); //remove leading slash
				project.move(description, updateFlags, null);
			} else {
				resource.move(new Path(destinationPath), updateFlags, null);
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Destination path: " + destinationPath + "\n");
		sb.append("Update flags: " + updateFlags + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
