/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jdt.internal.core.IDocumentListenersFactory;
import org.eclipse.jface.text.IDocumentListener;

import edu.illinois.codingspectator.codingtracker.listeners.document.FileDocumentListener;


/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class DocumentAdapterListener extends BasicListener implements IDocumentListenersFactory {

	public static void register() {
		DocumentAdapter.setDocumentListenersFactory(new DocumentAdapterListener());
	}

	@Override
	public IDocumentListener getDocumentListener(IFile file) {
		return new FileDocumentListener(file);
	}

}
