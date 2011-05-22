package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

/**
 * 
 * This class provides the infrastructure necessary for capturing information from a watched
 * refactoring processor. Note that subclasses of this class are not necessarily watched.
 * 
 * @author Mohsen Vakilian
 * 
 */
abstract public class WatchableRefactoringProcessor extends RefactoringProcessor {

	protected WatchedProcessorDelegate watchedProcessorDelegate;

	protected WatchedProcessorDelegate instantiateDelegate() {
		if (this instanceof IWatchedJavaProcessor) {
			return new WatchedProcessorDelegate((IWatchedJavaProcessor)this);
		} else {
			throw new AssertionError("Attempted to capture an unwatched refactoring processor."); //$NON-NLS-1$
		}
	}

	protected WatchedProcessorDelegate getWatchedProcessorDelegate() {
		if (watchedProcessorDelegate == null)
			watchedProcessorDelegate= instantiateDelegate();
		return watchedProcessorDelegate;
	}

	public CodeSnippetInformation getCodeSnippetInformation() {
		return getWatchedProcessorDelegate().getCodeSnippetInformation();
	}

	public String getJavaProjectName() {
		return getWatchedProcessorDelegate().getJavaProjectName();
	}

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		return getWatchedProcessorDelegate().getSimpleRefactoringDescriptor(refactoringStatus);
	}

}
