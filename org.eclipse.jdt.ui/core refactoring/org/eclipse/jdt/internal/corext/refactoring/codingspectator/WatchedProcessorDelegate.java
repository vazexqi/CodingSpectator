package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * Delegate to abstract away some of the common methods between the different classes.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class WatchedProcessorDelegate implements IWatchedJavaProcessor {

	private IWatchedJavaProcessor watchedProcessor;

	public WatchedProcessorDelegate(IWatchedJavaProcessor watchedProcessor) {
		this.watchedProcessor= watchedProcessor;
	}

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		JavaRefactoringDescriptor d= createRefactoringDescriptor();
		final Map augmentedArguments= populateInstrumentationData(refactoringStatus, getArguments(d));

		return createRefactoringDescriptor(d.getProject(), d.getDescription(), d.getComment(), augmentedArguments, d.getFlags());
	}

	abstract protected RefactoringDescriptor createRefactoringDescriptor(String project, String description, String comment, Map arguments, int flags);

	protected Map getArguments(JavaRefactoringDescriptor d) {
		try {
			Class c= JavaRefactoringDescriptor.class;
			Method getArgumentsMethod= c.getDeclaredMethod("getArguments", new Class[] {}); //$NON-NLS-1$
			getArgumentsMethod.setAccessible(true);
			return (Map)getArgumentsMethod.invoke(d, new Object[] {});
		} catch (Exception e) {
			JavaPlugin.log(e);
		}
		return new HashMap();

	}

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus, Map basicArguments) {
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION_TEXT, getSelection());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST, String.valueOf(isInvokedByQuickAssist()));
		return basicArguments;
	}

	public String getSelection() {
		IJavaElement javaElementIfPossible= getJavaElementIfPossible();
		if (javaElementIfPossible != null)
			return javaElementIfPossible.getElementName();
		return "CODINGSPECTATOR: non-Java element selected"; //$NON-NLS-1$
	}


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

	public JavaRefactoringDescriptor createRefactoringDescriptor() {
		return watchedProcessor.createRefactoringDescriptor();
	}
}
