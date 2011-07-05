package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocumentListener;

/**
 * 
 * @author Stas Negara
 * 
 */
public interface IDocumentListenersFactory {

	public IDocumentListener getDocumentListener(IFile file);
	
}
