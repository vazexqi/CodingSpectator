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

	public void movedResource(IResource resource, IPath destination, int updateFlags, boolean success);

	public void copiedResource(IResource resource, IPath destination, int updateFlags, boolean success);

	public void deletedResource(IResource resource, int updateFlags, boolean success);

	public void savedFile(IFile file, boolean success);

	public void aboutToSaveCompareEditor(Object compareEditor);

	public void savedCompareEditor(Object compareEditor);

}
