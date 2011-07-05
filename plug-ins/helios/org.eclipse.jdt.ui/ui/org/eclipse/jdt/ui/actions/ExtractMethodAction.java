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
package org.eclipse.jdt.ui.actions;

import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.codingspectator.RefactoringGlobalStore;

import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodWizard;

/**
 * Extracts the code selected inside a compilation unit editor into a new method. Necessary
 * arguments, exceptions and returns values are computed and an appropriate method signature is
 * generated.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @author Mohsen Vakilian, nchen - Made the extract method refactoring menu item always enabled in
 *         testing mode. This is a hack to circumvent a bug in SWTBot. Also, initialized the global
 *         store of refactorings at the beginning of the run methods.
 */
public class ExtractMethodAction extends SelectionDispatchAction {

	private final JavaEditor fEditor;

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * 
	 * @param editor the java editor
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ExtractMethodAction(JavaEditor editor) {
		super(editor.getEditorSite());
		setText(RefactoringMessages.ExtractMethodAction_label);
		fEditor= editor;
		//CODINGSPECTATOR
		setEnabled(isEnabled(SelectionConverter.getInputAsCompilationUnit(fEditor) != null));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.EXTRACT_METHOD_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction
	 */
	public void selectionChanged(ITextSelection selection) {
		//CODINGSPECTATOR
		setEnabled(isEnabled(selection.getLength() == 0 ? false : fEditor != null && SelectionConverter.getInputAsCompilationUnit(fEditor) != null));
	}

	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 * 
	 * @param selection the Java text selection (internal type)
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void selectionChanged(JavaTextSelection selection) {
		//CODINGSPECTATOR
		setEnabled(isEnabled(RefactoringAvailabilityTester.isExtractMethodAvailable(selection)));
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction
	 */
	public void run(ITextSelection selection) {
		//CODINGSPECTATOR
		RefactoringGlobalStore.getNewInstance().setEditorSelectionInfo(EditorUtility.getEditorInputJavaElement(fEditor, false), selection);

		if (!ActionUtil.isEditable(fEditor))
			return;
		ExtractMethodRefactoring refactoring= new ExtractMethodRefactoring(SelectionConverter.getInputAsCompilationUnit(fEditor), selection.getOffset(), selection.getLength());
		new RefactoringStarter().activate(new ExtractMethodWizard(refactoring), getShell(), RefactoringMessages.ExtractMethodAction_dialog_title, RefactoringSaveHelper.SAVE_NOTHING);
	}

	//CODINGSPECTATOR
	private boolean isEnabled(boolean isGoingToBeEnabled) {
		if (Platform.inDevelopmentMode()) {
			return true;
		} else {
			return isGoingToBeEnabled;
		}
	}
}
