package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * Delegate to abstract away some of the common methods between the different classes.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class WatchedProcessorDelegate implements IWatchedJavaProcessor {

	private IWatchedJavaProcessor watchedProcessor;

	public WatchedProcessorDelegate(IWatchedJavaProcessor watchedProcessor) {
		this.watchedProcessor= watchedProcessor;
	}

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		JavaRefactoringDescriptor originalRefactoringDescriptor= getOriginalRefactoringDescriptor();
		final Map augmentedArguments= populateInstrumentationData(refactoringStatus, originalRefactoringDescriptor.getArguments());
		return originalRefactoringDescriptor.cloneByAugmenting(augmentedArguments);
	}

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus, Map basicArguments) {
		getCodeSnippetInformation().insertIntoMap(basicArguments);
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST, String.valueOf(isInvokedByQuickAssist()));
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_THROUGH_STRUCTURED_SELECTION, String.valueOf(RefactoringGlobalStore.getInstance().isInvokedThroughStructuredSelection()));
		return basicArguments;
	}

	public CodeSnippetInformation getCodeSnippetInformation() {
		return CodeSnippetInformationFactory.extractCodeSnippetInformation();
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

	public JavaRefactoringDescriptor getOriginalRefactoringDescriptor() {
		return watchedProcessor.getOriginalRefactoringDescriptor();
	}

}
