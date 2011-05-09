package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.internal.corext.refactoring.nls.NLSUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class CodeSnippetInformationExtractor {
	protected static final String DEFAULT_NULL_ASTNODE_CODE_SNIPPET= "EMPTY CODE SNIPPET"; //$NON-NLS-1$

	protected static final String DEFAULT_SELECTED_TEXT= "CODINGSPECTATOR: Selection is not available"; //$NON-NLS-1$

	protected static final String DEFAULT_NULL_RELATIVE_SELECTION= "-1 -1"; //$NON-NLS-1$

	protected ITypeRoot typeRoot;

	public abstract CodeSnippetInformation extractCodeSnippetInformation();

	protected abstract ASTNode findTargetNode();

	public String getText(int start, int length) throws CoreException {
		IResource resource= typeRoot.getResource();
		if (resource.getType() != IResource.FILE) {
			throw new IllegalArgumentException("Expected the resource to be a file.");
		}
		IFile file= (IFile)resource;
		String contents= NLSUtil.readString(file.getContents(), file.getCharset());
		return contents.substring(start, start + length);
	}

	public String getCodeSnippet() {
		ASTNode node= getCodeSnippetNode();
		if (node != null) {
			try {
				return getText(node.getStartPosition(), node.getLength());
			} catch (CoreException e) {
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

	protected CompilationUnit getCompilationUnitASTFromTypeRoot() {
		return RefactoringASTParser.parseWithASTProvider(typeRoot, false, new NullProgressMonitor());
	}

}
