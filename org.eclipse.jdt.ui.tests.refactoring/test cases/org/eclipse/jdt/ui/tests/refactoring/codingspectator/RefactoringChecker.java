package org.eclipse.jdt.ui.tests.refactoring.codingspectator;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedRefactoring;

import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineMethodRefactoring;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringChecker {

	static int i= 0;

	/**
	 * 
	 * @return what conditions remain to be checked on the given refactoring. If this method checks
	 *         initial conditions of the given refactoring, it will return
	 *         CheckConditionsOperation.FINAL_CONDITIONS. Otherwise, it will return
	 *         CheckConditionsOperation.ALL_CONDITIONS to indicate that it hasn't checked any
	 *         conditions on the refactoring and all conditions remain to be checked.
	 */
	public static int checkRefactoringDescriptorCreation(final Refactoring refactoring) throws OperationCanceledException, CoreException {
		System.out.println("checkRefactoringDescriptorCreation #" + ++i);
		int remainingConditionsToCheck= CheckConditionsOperation.ALL_CONDITIONS;
		if (refactoring instanceof IWatchedRefactoring) {
			IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
			if (watchedRefactoring.isWatched()) {
				RefactoringStatus refactoringStatus= null;
				if (refactoring instanceof InlineMethodRefactoring) {
					refactoringStatus= refactoring.checkInitialConditions(new NullProgressMonitor());
					remainingConditionsToCheck= CheckConditionsOperation.FINAL_CONDITIONS;
				} else if (refactoring instanceof ExtractMethodRefactoring) {
					refactoringStatus= new RefactoringStatus();
				} else {
					refactoringStatus= refactoring.checkInitialConditions(new NullProgressMonitor());
				}
				if (!refactoringStatus.hasFatalError()) {
					Assert.assertNotNull(watchedRefactoring.getSimpleRefactoringDescriptor(refactoringStatus));
				}
			}
		}
		return remainingConditionsToCheck;
	}

}
