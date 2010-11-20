package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatched;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedProcessor;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * WatchedProcessor uses a less common Java idiom - simulating multiple inheritance using inner
 * classes. It is worth documenting why we are doing this.
 * 
 * <br>
 * <br>
 * 
 * Currently the Java processor-based refactorings e.g. rename and move have their own hierarchy
 * that is tied to the LTK {@link RefactoringProcessor} hierarchy. For instance, rename is tied to
 * RenameProcessor and move is tied to MoveProcessor. There is however common functionality between
 * rename and move that we can consolidate. However, we <b>cannot</b> consolidate that functionality
 * in their common ancestor in LTK {@link RefactoringProcessor} since that would provide useless
 * functionality to its other descendants.
 * 
 * Therefore we use a combination of implementing {@link IWatched} and inner classes i.e.
 * {@link WatchedProcessor} and its descendants. The main refactoring i.e.
 * {@link JavaRenameProcessor} implements {@link IWatchedProcessor} but delegates the call to
 * {@link IWatchedProcessor#getSimpleRefactoringDescriptor(RefactoringStatus)} to its inner class.
 * The inner class can use simple <em>closure</em> to gain access to the <em>guts</em> of
 * {@link JavaRenameProcessor} to get the specific details. All common code to refactoring
 * processors are abstracted away in {@link WatchedProcessor}. This gives us some code reuse albeit
 * through a more complicated mechanism that simple inheritance and composition.
 * 
 * <br>
 * <br>
 * 
 * For more information on this technique (including a diagram), @see <a
 * href="http://www.erik-rasmussen.com/blog/2006/10/23/multiple-inheritance-in-java/">Multiple
 * Inheritance in Java</a>
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
abstract public class WatchedProcessor implements IWatchedProcessor {

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

	protected abstract JavaRefactoringDescriptor createRefactoringDescriptor();

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus, Map basicArguments) {
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, getSelection());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
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
		if (getElements()[0] instanceof IJavaElement)
			return ((IJavaElement)getElements()[0]);
		return null;
	}

	/**
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#getElements()
	 */
	abstract protected Object[] getElements();

	public String getDescriptorID() {
		throw new UnsupportedOperationException();
	}

	public String getJavaProjectName() {
		String project= null;
		final IJavaProject javaProject= getJavaElementIfPossible().getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		return project;
	}

}
