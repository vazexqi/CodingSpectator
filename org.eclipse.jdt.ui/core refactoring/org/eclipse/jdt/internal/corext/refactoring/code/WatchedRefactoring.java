package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;

import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * This class serves as the base class for all refactorings that we instrument in JDT. It has a
 * couple of convenience methods for populating the refactoring descriptor.
 * 
 * @author vazexqi
 * 
 */
public abstract class WatchedRefactoring extends Refactoring {

	protected int fSelectionStart;

	protected int fSelectionLength;

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus) {
		Map arguments= new HashMap();
		arguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, getSelection());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		populateRefactoringSpecificFields(getJavaProjectName(), arguments);
		return arguments;
	}

	protected abstract void populateRefactoringSpecificFields(String project, final Map arguments);

	protected String getJavaProjectName() {
		String project= null;
		IJavaProject javaProject= getCompilationUnit().getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		return project;
	}

	private String getSelection() {
		try {
			return getCompilationUnit().getBuffer().getText(fSelectionStart, fSelectionLength);
		} catch (Exception e) {
			JavaPlugin.log(e);
		}
		return null;
	}


	private String getCodeSnippet() {
		ASTNode node= findTargetNode();

		final int THRESHOLD= 3200;

		while (node != null && node.subtreeBytes() < THRESHOLD) {
			node= node.getParent();
		}

		return ASTNodes.asFormattedString(node, 4, System.getProperty("line.separator"), getCompilationUnit().getJavaProject().getOptions(true)); //$NON-NLS-1$
	}

	private ASTNode findTargetNode() {
		ASTNode localNode= RefactoringASTParser.parseWithASTProvider(getCompilationUnit(), true, new NullProgressMonitor());

		// see (org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring.checkInitialConditions(IProgressMonitor))
		return NodeFinder.perform(localNode, fSelectionStart, fSelectionLength);
	}

	protected abstract ICompilationUnit getCompilationUnit();

}
