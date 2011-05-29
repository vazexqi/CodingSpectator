package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedRefactoring;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

/**
 * This class serves as the base class for all refactorings that we instrument in JDT. It has a
 * couple of convenience methods for populating the refactoring descriptor.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class WatchedJavaRefactoring extends Refactoring implements IWatchedRefactoring {

	public boolean isWatched() {
		return true;
	}

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		return getOriginalRefactoringDescriptor().cloneByAugmenting(populateInstrumentationData(refactoringStatus));
	}

	abstract protected RefactoringDescriptor getOriginalRefactoringDescriptor();

	private Map populateInstrumentationData(RefactoringStatus refactoringStatus) {
		Map arguments= new HashMap();
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST, String.valueOf(isInvokedByQuickAssist()));
		addAttributesFromGlobalRefactoringStore(arguments);
		return arguments;
	}

	private void addAttributesFromGlobalRefactoringStore(Map arguments) {
		arguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_THROUGH_STRUCTURED_SELECTION, String.valueOf(RefactoringGlobalStore.getInstance().isInvokedThroughStructuredSelection()));
		getCodeSnippetInformation().insertIntoMap(arguments);
	}

	protected String getJavaProjectName() {
		return RefactoringGlobalStore.getInstance().getProjectName();
	}

	protected void logUnavailableRefactoring(RefactoringStatus refactoringStatus) {
		if (isRefWizOpenOpCheckedInitConds()) {
			Logger.logUnavailableRefactoringEvent(getDescriptorID(), getJavaProjectName(), getCodeSnippetInformation(), refactoringStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL));
			unsetRefWizOpenOpCheckedInitConds();
		}
	}

	private CodeSnippetInformation getCodeSnippetInformation() {
		return CodeSnippetInformationFactory.extractCodeSnippetInformation();
	}

	abstract protected String getDescriptorID();

}
