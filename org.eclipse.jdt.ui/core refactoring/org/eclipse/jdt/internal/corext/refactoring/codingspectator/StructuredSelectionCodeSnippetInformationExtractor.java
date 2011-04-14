package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

import org.eclipse.jdt.internal.ui.JavaPlugin;

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
		return new CodeSnippetInformation(getCodeSnippet(), selection);
	}

	protected ASTNode findTargetNode() {
		ASTNode[] declarationNodes= null;
		try {
			declarationNodes= ASTNodeSearchUtil.getDeclarationNodes(selectedElement, getCompilationUnitASTFromTypeRoot());
		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		}
		ASTNode node= declarationNodes[0];
		return node;
	}

}
