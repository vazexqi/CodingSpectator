/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class MovedResourceOperation extends ReorganizedResourceOperation {

	public MovedResourceOperation() {
		super();
	}

	public MovedResourceOperation(IResource resource, IPath destination, int updateFlags, boolean success) {
		super(resource, destination, updateFlags, success);
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
	public void replayReorganizedResourceOperation(IResource resource) throws CoreException {
		if (resource instanceof Project) {
			Project project= (Project)resource;
			IProjectDescription description= project.getDescription();
			description.setName(destinationPath.substring(1)); //remove leading slash
			project.move(description, updateFlags, null);
		} else {
			//If a Java file is moved to a non Java file (e.g. file without ".java" extension), the editor (if any) of the moved Java file is closed.
			//Therefore, if not in test mode, explicitly close the editors of the files that are contained in the moved resource such that the replayer 
			//does not complain about the wrong editor, and do it before the resource is moved such that the affected files still exist.
			if (!Configuration.isInTestMode) {
				EditorHelper.closeAllEditorsForResource(resourcePath);

				//TODO: The following check is a duplicate of a part of ClosedFileOperation#replay.
				//If the currentEditor no longer exist, reset the corresponding field.
				if (!EditorHelper.isExistingEditor(currentEditor)) {
					currentEditor= null;
				}
			}
			resource.move(new Path(destinationPath), updateFlags, null);
		}
	}

}
