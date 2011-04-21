package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextSelection;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedRefactoring;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;

/**
 * This class serves as the base class for all refactorings that we instrument in JDT. It has a
 * couple of convenience methods for populating the refactoring descriptor.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class WatchedJavaRefactoring extends Refactoring implements IWatchedRefactoring {

	protected int fSelectionStart;

	protected int fSelectionLength;

	protected ITypeRoot fCompilationUnit;

	// Cache the global store of refactorings to make sure that "addAttributesFromGlobalRefactoringStore" and "getCodeSnippetInformation" do not interfere with each other by clearing the refactoring global store.
	private RefactoringGlobalStore cachedRefactoringGlobalStore;

	public WatchedJavaRefactoring() {
		cachedRefactoringGlobalStore= RefactoringGlobalStore.getInstance().getShallowCopy();
	}

	public boolean isWatched() {
		return true;
	}

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus) {
		Map arguments= new HashMap();
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST, String.valueOf(isInvokedByQuickAssist()));
		addAttributesFromGlobalRefactoringStore(arguments);
		populateRefactoringSpecificFields(getJavaProjectName(), arguments);
		return arguments;
	}

	private void addAttributesFromGlobalRefactoringStore(Map arguments) {
		arguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_THROUGH_STRUCTURED_SELECTION, String.valueOf(cachedRefactoringGlobalStore.isInvokedThroughStructuredSelection()));
		getCodeSnippetInformation().insertIntoMap(arguments);
		RefactoringGlobalStore.clearData();
	}

	protected abstract void populateRefactoringSpecificFields(String project, final Map arguments);

	protected String getJavaProjectName() {
		String project= null;
		IJavaProject javaProject= getJavaTypeRoot().getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		return project;
	}

	abstract protected ITypeRoot getJavaTypeRoot();

	protected void logUnavailableRefactoring(RefactoringStatus refactoringStatus) {
		if (isRefWizOpenOpCheckedInitConds()) {
			Logger.logUnavailableRefactoringEvent(getDescriptorID(), getJavaProjectName(), getCodeSnippetInformation(), refactoringStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL));
			unsetRefWizOpenOpCheckedInitConds();
		}
	}

	private CodeSnippetInformation getCodeSnippetInformation() {
		cachedRefactoringGlobalStore.setSelectionInEditor(new TextSelection(fSelectionStart, fSelectionLength));
		CodeSnippetInformation codeSnippetInformation= cachedRefactoringGlobalStore.extractCodeSnippetInformation(getJavaTypeRoot());
		RefactoringGlobalStore.clearData();
		return codeSnippetInformation;
	}

	protected String getDescriptorID() {
		throw new UnsupportedOperationException();
	}

}
