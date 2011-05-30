package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.codingspectator.IClearable;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringGlobalStore implements IClearable {
	private static RefactoringGlobalStore instance;

	static {
		resetInstance();
	}

	private CodeSnippetInformation codeSnippetInformation= new NullCodeSnippetInformationExtractor().extractCodeSnippetInformation();

	private String projectName;

	private boolean invokedThroughTextualSelection;

	private boolean invokedThroughStructuredSelection;

	private RefactoringGlobalStore() {

	}

	private static void resetInstance() {
		instance= new RefactoringGlobalStore();
		Logger.clearable= instance;
	}

	public static RefactoringGlobalStore getNewInstance() {
		resetInstance();
		return getInstance();
	}

	public static RefactoringGlobalStore getInstance() {
		return instance;
	}

	public void clearData() {
		resetInstance();
	}

	private void setInvokedThroughStructuredSelection() {
		invokedThroughStructuredSelection= true;
	}

	public boolean isInvokedThroughStructuredSelection() {
		return invokedThroughStructuredSelection;
	}

	private void setInvokedThroughTextualSelection() {
		invokedThroughTextualSelection= true;
	}

	private boolean isInvokedThroughTextualSelection() {
		return invokedThroughTextualSelection;
	}

	/**
	 * This method specifies an object invariant.
	 */
	private void assertOnlyOneKindOfSelectionExists() {
		if (isInvokedThroughTextualSelection() && isInvokedThroughStructuredSelection()) {
			JavaPlugin.log(new AssertionError("Capturing both structured and textual selections for a refactoring is unexpected.")); //$NON-NLS-1$
		}
	}

	private void setProjectName(ITypeRoot typeRoot) {
		if (typeRoot != null && typeRoot.getJavaProject() != null) {
			projectName= typeRoot.getJavaProject().getElementName();
		}
	}

	public String getProjectName() {
		return projectName;
	}

	public void setStructuredSelection(IStructuredSelection selection) {
		setInvokedThroughStructuredSelection();

		Object firstSelectedElement= selection.getFirstElement();
		String selectedElementsText= selection.toString();
		if (firstSelectedElement instanceof IJavaElement) {
			IJavaElement selectedElement= (IJavaElement)firstSelectedElement;
			ITypeRoot selectedTypeRoot= (ITypeRoot)selectedElement.getAncestor(IJavaElement.COMPILATION_UNIT);
			setProjectName(selectedTypeRoot);
			codeSnippetInformation= new StructuredSelectionCodeSnippetInformationExtractor(selectedTypeRoot, selectedElement, selectedElementsText).extractCodeSnippetInformation();
		}
		assertOnlyOneKindOfSelectionExists();
	}

	public void setEditorSelectionInfo(ITypeRoot typeRoot, ITextSelection selection) {
		setInvokedThroughTextualSelection();
		setProjectName(typeRoot);
		codeSnippetInformation= new TextSelectionCodeSnippetInformationExtractor(typeRoot, selection.getOffset(), selection.getLength()).extractCodeSnippetInformation();
		assertOnlyOneKindOfSelectionExists();
	}

	public CodeSnippetInformation getCodeSnippetInformation() {
		return codeSnippetInformation;
	}

}
