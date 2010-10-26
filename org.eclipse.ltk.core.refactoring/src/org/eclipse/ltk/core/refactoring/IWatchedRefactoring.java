package org.eclipse.ltk.core.refactoring;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 * 
 * 
 */
public interface IWatchedRefactoring {

	public abstract RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus);

}
