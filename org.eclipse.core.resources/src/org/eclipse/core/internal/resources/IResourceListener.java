package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * @author Stas Negara
 * 
 */
public interface IResourceListener {

	public void createdResource(IResource resource, int updateFlags, boolean success);

	public void movedResource(IResource resource, IPath destination, int updateFlags, boolean success);

	public void copiedResource(IResource resource, IPath destination, int updateFlags, boolean success);

	public void deletedResource(IResource resource, int updateFlags, boolean success);

	public void externallyModifiedResource(IResource resource, boolean isDeleted);

	public void externallyCreatedResource(IResource resource);

	public void refreshedResource(IResource resource);

	public void savedFile(IFile file, boolean success);

	public void savedFile(IPath filePath, boolean success);

	public void savedCompareEditor(Object compareEditor, boolean success);

}
