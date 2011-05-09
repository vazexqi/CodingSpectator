package org.eclipse.jdt.internal.corext.refactoring.codingspectator;


import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class TextSelectionCodeSnippetInformationExtractor extends CodeSnippetInformationExtractor {
	private int selectionStart;

	private int selectionLength;

	public TextSelectionCodeSnippetInformationExtractor(ITypeRoot typeRoot, int selectionStart, int selectionLength) {
		this.typeRoot= typeRoot;
		this.selectionStart= selectionStart;
		this.selectionLength= selectionLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.codingspectator.ICodeSnippetInformationExtractor#extractCodeSnippetInformation()
	 */
	public CodeSnippetInformation extractCodeSnippetInformation() {
		String codeSnippet= getCodeSnippet();
		String relativeOffset= getSnippetRelativeOffset(getCodeSnippetNode());
		String selectedText= getSelectedText();
		return new CodeSnippetInformation(codeSnippet, relativeOffset, selectedText);
	}

	private String getSelectedText() {
		try {
			return getText(selectionStart, selectionLength);
		} catch (CoreException e) {
			JavaPlugin.log(e);
		}

		return DEFAULT_SELECTED_TEXT;
	}

	public String getSnippetRelativeOffset(ASTNode node) {
		String snippetOffset= DEFAULT_NULL_RELATIVE_SELECTION;

		if (node != null) {
			snippetOffset= Integer.toString(selectionStart - node.getStartPosition()) + " " + selectionLength; //$NON-NLS-1$
		}
		return snippetOffset;
	}

	protected ASTNode findTargetNode() throws CoreException {
		ASTNode localNode= getCompilationUnitASTFromTypeRoot();

		// see (org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring.checkInitialConditions(IProgressMonitor))
		return NodeFinder.perform(localNode, selectionStart, selectionLength);
	}
}
