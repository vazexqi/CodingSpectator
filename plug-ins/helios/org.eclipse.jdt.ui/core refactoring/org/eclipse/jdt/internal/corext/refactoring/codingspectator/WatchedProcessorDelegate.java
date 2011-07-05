package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

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
		return RefactoringGlobalStore.getInstance().getCodeSnippetInformation();
	}

	public String getJavaProjectName() {
		return RefactoringGlobalStore.getInstance().getProjectName();
	}

	public String getDescriptorID() {
		return watchedProcessor.getDescriptorID();
	}

	public boolean isInvokedByQuickAssist() {
		return watchedProcessor.isInvokedByQuickAssist();
	}

	public JavaRefactoringDescriptor getOriginalRefactoringDescriptor() {
		return watchedProcessor.getOriginalRefactoringDescriptor();
	}

}
