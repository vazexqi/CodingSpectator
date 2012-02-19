/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;

/**
 * 
 * @author Stas Negara
 * 
 */
public class InactiveASTOperationRecorder extends ASTOperationRecorder {

	@Override
	public void beforeDocumentChange(DocumentEvent event, String fileID) {
		//do nothing
	}

	@Override
	public void flushCurrentTextChanges(boolean isForced) {
		//do nothing
	}

	@Override
	public void recordASTOperationForDeletedResource(IResource deletedResource, boolean success) {
		//do nothing
	}

	@Override
	public void recordASTOperationForMovedResource(IResource movedResource, IPath destination, boolean success) {
		//do nothing
	}

	@Override
	public void recordASTOperationForCopiedResource(IResource copiedResource, IPath destination, boolean success) {
		//do nothing
	}

	@Override
	public void recordASTOperationForCreatedResource(IResource createdResource, boolean success) {
		//do nothing
	}

}
