package org.eclipse.ltk.core.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface IWatched {

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus);

	public boolean isInvokedByQuickAssist();

}
