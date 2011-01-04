package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedRefactoring;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;

import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * This class serves as the base class for all refactorings that we instrument in JDT. It has a
 * couple of convenience methods for populating the refactoring descriptor.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class WatchedJavaRefactoring extends Refactoring implements IWatchedRefactoring {


	private static final String DEFAULT_NULL_ASTNODE_CODE_SNIPPET= "EMPTY CODE SNIPPET"; //$NON-NLS-1$

	protected int fSelectionStart;

	protected int fSelectionLength;

	protected ITypeRoot fCompilationUnit;

	public WatchedJavaRefactoring() {

	}

	public boolean isWatched() {
		return true;
	}

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus) {
		Map arguments= new HashMap();
		arguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, getSelection());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST, String.valueOf(isInvokedByQuickAssist()));
		populateRefactoringSpecificFields(getJavaProjectName(), arguments);
		return arguments;
	}

	protected abstract void populateRefactoringSpecificFields(String project, final Map arguments);

	protected String getJavaProjectName() {
		String project= null;
		IJavaProject javaProject= getJavaTypeRoot().getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		return project;
	}

	protected String getSelection() {
		try {
			return getJavaTypeRoot().getBuffer().getText(fSelectionStart, fSelectionLength);
		} catch (Exception e) {
			JavaPlugin.log(e);
		}
		return null;
	}


	private String getCodeSnippet() {
		ASTNode node= findTargetNode();

		if (node == null) {
			return DEFAULT_NULL_ASTNODE_CODE_SNIPPET;
		}

		final int THRESHOLD= 3200;

		while (node.getParent() != null && node.subtreeBytes() < THRESHOLD) {
			node= node.getParent();
		}

		return ASTNodes.asFormattedString(node, 4, System.getProperty("line.separator"), getJavaTypeRoot().getJavaProject().getOptions(true)); //$NON-NLS-1$
	}

	private ASTNode findTargetNode() {
		ASTNode localNode= RefactoringASTParser.parseWithASTProvider(getJavaTypeRoot(), false, new NullProgressMonitor());

		// see (org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring.checkInitialConditions(IProgressMonitor))
		return NodeFinder.perform(localNode, fSelectionStart, fSelectionLength);
	}

	abstract protected ITypeRoot getJavaTypeRoot();

	protected void logUnavailableRefactoring(RefactoringStatus refactoringStatus) {
		if (isRefWizOpenOpCheckedInitConds()) {
			Logger.logUnavailableRefactoringEvent(getDescriptorID(), getJavaProjectName(), getSelection(), refactoringStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL));
			unsetRefWizOpenOpCheckedInitConds();
		}
	}

	protected String getDescriptorID() {
		throw new UnsupportedOperationException();
	}

}
