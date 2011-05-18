/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.conflicteditors;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.BufferedResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class DocumentCompareEditorInput extends CompareEditorInput {

	private BufferedResourceNode leftNode;

	private BufferedResourceNode rightNode;

	public DocumentCompareEditorInput(IResource resource, String initialContent) {
		super(new CompareConfiguration());
		setupNodes(resource, initialContent);
		setTitle(resource.getName());
		setDirty(true);
	}

	@Override
	public void saveChanges(IProgressMonitor monitor) throws CoreException {
		flushViewers(monitor);
		leftNode.commit(monitor);
		setDirty(false);
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		return new DiffNode(Differencer.CHANGE, null, leftNode, rightNode);
	}

	private void setupNodes(IResource resource, String initialContent) {
		leftNode= new BufferedResourceNode(resource);
		leftNode.setContent(initialContent.getBytes());
		rightNode= new BufferedResourceNode(resource);
		rightNode.setContent(new byte[0]);
	}

}
