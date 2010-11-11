package org.eclipse.jdt.ui.tests.refactoring.codingspectator;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

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

	public static void checkRefactoringDescriptorCreation(final Refactoring refactoring) throws OperationCanceledException, CoreException {
		System.out.println("checkRefactoringDescriptorCreation #" + ++i);
		if (refactoring instanceof IWatchedRefactoring) {
			IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
			if (watchedRefactoring.isWatched()) {
				RefactoringStatus refactoringStatus= null;
				if (refactoring instanceof InlineMethodRefactoring) {
					refactoringStatus= refactoring.checkInitialConditions(new NullProgressMonitor());
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
	}

}
