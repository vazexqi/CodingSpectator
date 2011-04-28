package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IFile;

/**
 * 
 * @author Stas Negara
 * 
 */
public interface IResourceListener {

	public void savedFile(IFile file, boolean success);

	public void aboutToSaveCompareEditor(Object compareEditor);

	public void savedCompareEditor(Object compareEditor);

}
