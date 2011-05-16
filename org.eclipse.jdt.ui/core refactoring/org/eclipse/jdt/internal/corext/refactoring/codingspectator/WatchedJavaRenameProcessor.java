package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;

public abstract class WatchedJavaRenameProcessor extends RenameProcessor implements IWatchedJavaProcessor {

	protected WatchedProcessorDelegate watchedProcessorDelegate;

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		return getWatchedProcessorDelegate().getSimpleRefactoringDescriptor(refactoringStatus);
	}

	/**
	 * @deprecated: Use getCodeSnippetInformation() instead.
	 */
	public String getSelection() {
		return getWatchedProcessorDelegate().getSelection();
	}

	public CodeSnippetInformation getCodeSnippetInformation() {
		return getWatchedProcessorDelegate().getCodeSnippetInformation();
	}

	public String getJavaProjectName() {
		return getWatchedProcessorDelegate().getJavaProjectName();
	}

	protected WatchedProcessorDelegate getWatchedProcessorDelegate() {
		if (watchedProcessorDelegate == null)
			watchedProcessorDelegate= instantiateDelegate();
		return watchedProcessorDelegate;
	}

	protected abstract WatchedProcessorDelegate instantiateDelegate();

}
