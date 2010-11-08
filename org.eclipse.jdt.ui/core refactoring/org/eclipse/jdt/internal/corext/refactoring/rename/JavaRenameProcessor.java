/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.rename;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;

import org.eclipse.ltk.core.refactoring.IWatchedProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.tagging.INameUpdating;

import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author Mohsen Vakilian, nchen - Added template methods.
 * 
 */
public abstract class JavaRenameProcessor extends RenameProcessor implements INameUpdating, IWatchedProcessor {

	private String fNewElementName;

	private RenameModifications fRenameModifications;

	public final RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared) throws CoreException {
		return fRenameModifications.loadParticipants(status, this, getAffectedProjectNatures(), shared);
	}

	public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
		ResourceChangeChecker checker= (ResourceChangeChecker)context.getChecker(ResourceChangeChecker.class);
		IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();
		RefactoringStatus result= doCheckFinalConditions(pm, context);
		if (result.hasFatalError())
			return result;
		IFile[] changed= getChangedFiles();
		for (int i= 0; i < changed.length; i++) {
			deltaFactory.change(changed[i]);
		}
		fRenameModifications= computeRenameModifications();
		fRenameModifications.buildDelta(deltaFactory);
		fRenameModifications.buildValidateEdits((ValidateEditChecker)context.getChecker(ValidateEditChecker.class));
		return result;
	}

	protected abstract RenameModifications computeRenameModifications() throws CoreException;

	protected abstract RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException;

	protected abstract IFile[] getChangedFiles() throws CoreException;

	protected abstract String[] getAffectedProjectNatures() throws CoreException;

	public void setNewElementName(String newName) {
		Assert.isNotNull(newName);
		fNewElementName= newName;
	}

	public String getNewElementName() {
		return fNewElementName;
	}

	/**
	 * @return a save mode from {@link RefactoringSaveHelper}
	 * 
	 * @see RefactoringSaveHelper
	 */
	public abstract int getSaveMode();

	// CODINGSPECTATOR: The following methods have been added as part of CodingSpectator to capture renames.

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		JavaRefactoringDescriptor d= createRefactoringDescriptor();
		final Map augmentedArguments= populateInstrumentationData(refactoringStatus, getArguments(d));
		final RefactoringDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(d.getID(), d.getProject(), d.getDescription(), d.getComment(),
				augmentedArguments, d.getFlags());
		return descriptor;
	}

	protected Map getArguments(JavaRefactoringDescriptor d) {
		try {
			Class c= JavaRefactoringDescriptor.class;
			Method getArgumentsMethod= c.getDeclaredMethod("getArguments", new Class[] {}); //$NON-NLS-1$
			getArgumentsMethod.setAccessible(true);
			return (Map)getArgumentsMethod.invoke(d, new Object[] {});
		} catch (Exception e) {
			JavaPlugin.log(e);
		}
		return new HashMap();

	}

	protected abstract JavaRefactoringDescriptor createRefactoringDescriptor();

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus, Map basicArguments) {
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, getSelection());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		return basicArguments;
	}

	protected String getSelection() {
		return ((IJavaElement)getElements()[0]).getElementName();
	}

	protected String getCodeSnippet() {
		return ((IJavaElement)getElements()[0]).toString();
	}

}
