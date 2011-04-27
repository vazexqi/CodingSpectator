package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * Delegate to abstract away some of the common methods between the different classes.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class WatchedProcessorDelegate implements IWatchedJavaProcessor {

	private IWatchedJavaProcessor watchedProcessor;

	// Cache the global store of refactorings to make sure that "populateInstrumentationData" and "getCodeSnippetInformation" do not interfere with each other by clearing the refactoring global store.
	private RefactoringGlobalStore cachedRefactoringGlobalStore;

	public WatchedProcessorDelegate(IWatchedJavaProcessor watchedProcessor) {
		this.watchedProcessor= watchedProcessor;
		cachedRefactoringGlobalStore= RefactoringGlobalStore.getInstance().getShallowCopy();
	}

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		JavaRefactoringDescriptor d= createRefactoringDescriptor();
		final Map augmentedArguments= populateInstrumentationData(refactoringStatus, d.getArguments());

		return createRefactoringDescriptor(d.getProject(), d.getDescription(), d.getComment(), augmentedArguments, d.getFlags());
	}

	abstract protected RefactoringDescriptor createRefactoringDescriptor(String project, String description, String comment, Map arguments, int flags);

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus, Map basicArguments) {
		getCodeSnippetInformation().insertIntoMap(basicArguments);
		RefactoringGlobalStore.clearData();
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST, String.valueOf(isInvokedByQuickAssist()));
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_THROUGH_STRUCTURED_SELECTION, String.valueOf(cachedRefactoringGlobalStore.isInvokedThroughStructuredSelection()));
		return basicArguments;
	}

	private ITypeRoot getEnclosingCompilationUnit() {
		IJavaElement javaElementIfPossible= getJavaElementIfPossible();
		return (ITypeRoot)javaElementIfPossible.getAncestor(IJavaElement.COMPILATION_UNIT);
	}

	/**
	 * @deprecated - Use getCodeSnippetInfomration instead.
	 */
	public String getSelection() {
		IJavaElement javaElementIfPossible= getJavaElementIfPossible();
		if (javaElementIfPossible != null)
			return javaElementIfPossible.getElementName();
		return "CODINGSPECTATOR: non-Java element selected"; //$NON-NLS-1$
	}

	public CodeSnippetInformation getCodeSnippetInformation() {
		return cachedRefactoringGlobalStore.extractCodeSnippetInformation(getEnclosingCompilationUnit());
	}

	/**
	 * @deprecated - To be replaced with functionality in CodeSnippetInformationExtractor
	 */
	protected String getCodeSnippet() {
		IJavaElement javaElementIfPossible= getJavaElementIfPossible();
		if (javaElementIfPossible != null)
			return javaElementIfPossible.toString();
		return "CODINGSPECTATOR: non-Java element selected"; //$NON-NLS-1$
	}

// The following method doesn't seem to be used by anyone.
//	public String getCodeSnippet(ASTNode node) {
//		if (node != null) {
//			try {
//				return getEnclosingCompilationUnit().getBuffer().getText(node.getStartPosition(), node.getLength());
//			} catch (IndexOutOfBoundsException e) {
//				JavaPlugin.log(e);
//			} catch (JavaModelException e) {
//				JavaPlugin.log(e);
//			}
//		}
//
//		return "DEFAULT";
//	}

	private IJavaElement getJavaElementIfPossible() {
		if (getElements() == null) {
			return null;
		}
		if (getElements().length == 0) {
			return null;
		}
		if (getElements()[0] instanceof IJavaElement)
			return ((IJavaElement)getElements()[0]);
		return null;
	}

	public String getJavaProjectName() {
		String project= null;
		final IJavaProject javaProject= getJavaElementIfPossible().getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		return project;
	}

	public String getDescriptorID() {
		return watchedProcessor.getDescriptorID();
	}

	public Object[] getElements() {
		return watchedProcessor.getElements();
	}

	public boolean isInvokedByQuickAssist() {
		return watchedProcessor.isInvokedByQuickAssist();
	}

	public JavaRefactoringDescriptor createRefactoringDescriptor() {
		return watchedProcessor.createRefactoringDescriptor();
	}
}
