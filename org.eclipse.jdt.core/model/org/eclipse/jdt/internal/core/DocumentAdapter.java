/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * Adapts an IBuffer to IDocument
 * 
 * @author Stas Negara - Added a document listener (obtained via a factory) to every new instance
 *         of DocumentAdapter.  
 */
public class DocumentAdapter extends Document {

	private IBuffer buffer;
	
	//CODINGSPECTATOR
	private static IDocumentListenersFactory documentListenersFactory;

	//CODINGSPECTATOR
	public static void setDocumentListenersFactory(IDocumentListenersFactory newDocumentListenersFactory){
		documentListenersFactory=newDocumentListenersFactory;
	}
	
	public DocumentAdapter(IBuffer buffer) {
		super(buffer.getContents());
		this.buffer = buffer;
		//CODINGSPECTATOR
		IResource underlyingResource = buffer.getUnderlyingResource();
		if (underlyingResource instanceof IFile && underlyingResource.exists()){
			addDocumentListener(documentListenersFactory.getDocumentListener((IFile) underlyingResource));			
		}
	}

	public void set(String text) {
		super.set(text);
		this.buffer.setContents(text);
	}

	public void replace(int offset, int length, String text) throws BadLocationException {
		super.replace(offset, length, text);
		this.buffer.replace(offset, length, text);
	}

}
