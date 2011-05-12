package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class StructuredSelectionCodeSnippetInformationExtractor extends CodeSnippetInformationExtractor {

	private String selection;

	private IJavaElement selectedElement;

	public StructuredSelectionCodeSnippetInformationExtractor(ITypeRoot typeRoot, IJavaElement aSelectedElement, String selection) {
		this.typeRoot= typeRoot;
		this.selectedElement= aSelectedElement;
		this.selection= selection;
	}

	public CodeSnippetInformation extractCodeSnippetInformation() {
		if (isSelectedElementInsideACompilationUnit()) {
			return new CodeSnippetInformation(getCodeSnippet(), selection);
		} else {
			return new CodeSnippetInformation(selection);
		}
	}

	/**
	 * 
	 * @see org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil#getDeclarationNodes(IJavaElement,
	 *      CompilationUnit)
	 * 
	 * @param javaElement
	 * @return
	 */
	private boolean isSelectedElementInsideACompilationUnit() {
		switch (selectedElement.getElementType()) {
			case IJavaElement.FIELD:
			case IJavaElement.IMPORT_CONTAINER:
			case IJavaElement.IMPORT_DECLARATION:
			case IJavaElement.INITIALIZER:
			case IJavaElement.METHOD:
			case IJavaElement.PACKAGE_DECLARATION:
			case IJavaElement.TYPE:
				return true;
			default:
				return false;
		}
	}

	protected ASTNode findTargetNode() throws CoreException {
		ASTNode[] declarationNodes= null;
		declarationNodes= ASTNodeSearchUtil.getDeclarationNodes(selectedElement, getCompilationUnitASTFromTypeRoot());
		if (declarationNodes == null || declarationNodes.length == 0) {
			return null;
		}
		ASTNode node= declarationNodes[0];
		return node;
	}

}
