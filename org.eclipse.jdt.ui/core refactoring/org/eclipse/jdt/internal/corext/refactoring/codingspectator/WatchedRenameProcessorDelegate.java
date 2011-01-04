package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;


/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
abstract public class WatchedRenameProcessorDelegate extends WatchedProcessorDelegate {

	public WatchedRenameProcessorDelegate(IWatchedJavaProcessor watchedProcessor) {
		super(watchedProcessor);
	}

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		JavaRefactoringDescriptor d= createRefactoringDescriptor();
		final Map augmentedArguments= populateInstrumentationData(refactoringStatus, getArguments(d));
		final RefactoringDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(d.getID(), d.getProject(), d.getDescription(), d.getComment(),
				augmentedArguments, d.getFlags());
		return descriptor;
	}

	protected RefactoringDescriptor createRefactoringDescriptor(String project, String description, String comment, Map arguments, int flags) {
		throw new UnsupportedOperationException();
	}
}
