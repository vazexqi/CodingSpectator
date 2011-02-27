/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.actions;

import java.io.CharConversionException;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;

import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringExecutionStarter;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringActions;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;

/**
 * Extract a new interface from a class and tries to use the interface instead of the concrete class
 * where possible.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.1
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @author Balaji Ambresh Rajkumar nchen, Mohsen Vakilian - Captured when the refactoring is
 *         unavailable.
 */
public class ExtractInterfaceAction extends SelectionDispatchAction {

	private JavaEditor fEditor;

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * 
	 * @param editor the java editor
	 * 
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public ExtractInterfaceAction(JavaEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}

	/**
	 * Creates a new <code>ExtractInterfaceAction</code>. The action requires that the selection
	 * provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public ExtractInterfaceAction(IWorkbenchSite site) {
		super(site);
		setText(RefactoringMessages.ExtractInterfaceAction_Extract_Interface);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.EXTRACT_INTERFACE_ACTION);
	}

	//---- structured selection -------------------------------------------

	/*
	 * @see SelectionDispatchAction#selectionChanged(IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		try {
			setEnabled(RefactoringAvailabilityTester.isExtractInterfaceAvailable(selection));
		} catch (JavaModelException e) {
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=19253
			if (!(e.getException() instanceof CharConversionException) && JavaModelUtil.isExceptionToBeLogged(e))
				JavaPlugin.log(e);
			setEnabled(false);//no UI - happens on selection changes
		}
	}

	/*
	 * @see SelectionDispatchAction#run(IStructuredSelection)
	 */
	public void run(IStructuredSelection selection) {
		try {
			if (RefactoringAvailabilityTester.isExtractInterfaceAvailable(selection)) {
				IType singleSelectedType= RefactoringAvailabilityTester.getSingleSelectedType(selection);
				if (!ActionUtil.isEditable(getShell(), singleSelectedType))
					return;
				RefactoringExecutionStarter.startExtractInterfaceRefactoring(singleSelectedType, getShell());
			}
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_exception);
		}
	}

	/*
	 * @see SelectionDispatchAction#selectionChanged(ITextSelection)
	 */
	public void selectionChanged(ITextSelection selection) {
		setEnabled(true);
	}

	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 * 
	 * @param selection the Java text selection (internal type)
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void selectionChanged(JavaTextSelection selection) {
		try {
			setEnabled(RefactoringAvailabilityTester.isExtractInterfaceAvailable(selection));
		} catch (JavaModelException e) {
			setEnabled(false);
		}
	}

	/*
	 * @see SelectionDispatchAction#run(ITextSelection)
	 */
	public void run(ITextSelection selection) {
		try {
			IType type= RefactoringActions.getEnclosingOrPrimaryType(fEditor);
			if (RefactoringAvailabilityTester.isExtractInterfaceAvailable(type)) {
				if (!ActionUtil.isEditable(fEditor, getShell(), type))
					return;
				RefactoringExecutionStarter.startExtractInterfaceRefactoring(type, getShell());
			} else {
				String unavailable= RefactoringMessages.ExtractInterfaceAction_To_activate;
				MessageDialog.openInformation(getShell(), RefactoringMessages.OpenRefactoringWizardAction_unavailable, unavailable);

				//CODINGSPECTATOR: Record the invocation of the refactoring when it is not available.
				ITypeRoot typeRoot= SelectionConverter.getInput(fEditor);
				String javaProject= typeRoot.getJavaProject().getElementName();
				ASTNode node= getCodeSnippetNode(typeRoot, selection);

				Logger.logUnavailableRefactoringEvent(IJavaRefactorings.EXTRACT_INTERFACE, javaProject, selection.getText(), getCodeSnippet(typeRoot, selection),
						getSnippetRelativeOffset(node, selection), unavailable);
			}
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_exception);
		}
	}

	/////////////////
	//CODINGSPECTATOR
	/////////////////

	private static final String DEFAULT_NULL_ASTNODE_CODE_SNIPPET= "EMPTY CODE SNIPPET"; //$NON-NLS-1$

	private static final String DEFAULT_NULL_RELATIVE_SELECTION= "-1 -1"; //$NON-NLS-1$

	private String getCodeSnippet(ITypeRoot root, ITextSelection selection) {
		ASTNode node= getCodeSnippetNode(root, selection);
		IDocument document= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());

		if (node != null) {
			try {
				return document.get(node.getStartPosition(), node.getLength());
			} catch (BadLocationException e) {
				JavaPlugin.log(e);
			}
		}

		return DEFAULT_NULL_ASTNODE_CODE_SNIPPET;
	}

	private ASTNode getCodeSnippetNode(ITypeRoot root, ITextSelection selection) {
		ASTNode node= findTargetNode(root, selection);

		if (node == null) {
			return null;
		}

		final int THRESHOLD= 3200;

		while (node.getParent() != null && node.subtreeBytes() < THRESHOLD) {
			node= node.getParent();
		}
		return node;
	}

	private String getSnippetRelativeOffset(ASTNode node, ITextSelection selection) {
		String snippetOffset= DEFAULT_NULL_RELATIVE_SELECTION;

		if (node != null) {
			snippetOffset= Integer.toString(selection.getOffset() - node.getStartPosition()) + " " + selection.getLength(); //$NON-NLS-1$
		}
		return snippetOffset;
	}

	private ASTNode findTargetNode(ITypeRoot root, ITextSelection selection) {

		ASTNode localNode= RefactoringASTParser.parseWithASTProvider(root, false, new NullProgressMonitor());

		// see (org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring.checkInitialConditions(IProgressMonitor))
		return NodeFinder.perform(localNode, selection.getOffset(), selection.getLength());
	}
}
