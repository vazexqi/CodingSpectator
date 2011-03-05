package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;

import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.ui.JavaPlugin;

public class CodeSnippetInformationExtractor {
	private static final String DEFAULT_NULL_ASTNODE_CODE_SNIPPET= "EMPTY CODE SNIPPET"; //$NON-NLS-1$

	private static final String DEFAULT_SELECTED_TEXT= "CODINGSPECTATOR: Selection is not available"; //$NON-NLS-1$

	private static final String DEFAULT_NULL_RELATIVE_SELECTION= "-1 -1"; //$NON-NLS-1$

	private ITypeRoot typeRoot;

	private int selectionStart;

	private int selectionLength;

	public CodeSnippetInformationExtractor(ITypeRoot typeRoot, int selectionStart, int selectionLength) {
		this.typeRoot= typeRoot;
		this.selectionStart= selectionStart;
		this.selectionLength= selectionLength;
	}

	public CodeSnippetInformation extractCodeSnippetInformation() {
		String codeSnippet= getCodeSnippet();
		String relativeOffset= getSnippetRelativeOffset(getCodeSnippetNode());
		String selectedText= getSelectedText();
		return new CodeSnippetInformation(codeSnippet, relativeOffset, selectedText);
	}

	private String getSelectedText() {
		String selectedText= DEFAULT_SELECTED_TEXT;
		try {
			selectedText= typeRoot.getBuffer().getText(selectionStart, selectionLength);
		} catch (IndexOutOfBoundsException e) {
			JavaPlugin.log(e);
		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		}

		return selectedText;
	}

	public String getCodeSnippet() {
		ASTNode node= getCodeSnippetNode();
		if (node != null) {
			try {
				return typeRoot.getBuffer().getText(node.getStartPosition(), node.getLength());
			} catch (IndexOutOfBoundsException e) {
				JavaPlugin.log(e);
			} catch (JavaModelException e) {
				JavaPlugin.log(e);
			}
		}

		return DEFAULT_NULL_ASTNODE_CODE_SNIPPET;
	}

	public ASTNode getCodeSnippetNode() {
		ASTNode node= findTargetNode();

		if (node == null) {
			return null;
		}

		final int THRESHOLD= 3200;

		while (node.getParent() != null && node.subtreeBytes() < THRESHOLD) {
			node= node.getParent();
		}
		return node;
	}

	public String getSnippetRelativeOffset(ASTNode node) {
		String snippetOffset= DEFAULT_NULL_RELATIVE_SELECTION;

		if (node != null) {
			snippetOffset= Integer.toString(selectionStart - node.getStartPosition()) + " " + selectionLength; //$NON-NLS-1$
		}
		return snippetOffset;
	}

	public ASTNode findTargetNode() {

		ASTNode localNode= RefactoringASTParser.parseWithASTProvider(typeRoot, false, new NullProgressMonitor());

		// see (org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring.checkInitialConditions(IProgressMonitor))
		return NodeFinder.perform(localNode, selectionStart, selectionLength);
	}
}