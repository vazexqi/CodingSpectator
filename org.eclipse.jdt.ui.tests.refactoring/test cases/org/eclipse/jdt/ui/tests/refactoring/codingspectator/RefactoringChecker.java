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

	public static RefactoringStatus checkRefactoringDescriptorCreation(final Refactoring refactoring) throws OperationCanceledException, CoreException {
		RefactoringStatus refactoringStatus= new RefactoringStatus();

		if (refactoring instanceof IWatchedRefactoring) {
			IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
			if (watchedRefactoring.isWatched()) {
				if (refactoring instanceof InlineMethodRefactoring) {
					refactoringStatus.merge(refactoring.checkInitialConditions(new NullProgressMonitor()));
				} else if (refactoring instanceof ExtractMethodRefactoring) {
					// don't execute checkInitialConditions since that would modify its initial settings
				} else {
					refactoringStatus.merge(refactoring.checkInitialConditions(new NullProgressMonitor()));
				}
				// We are being more liberal here to not log only in the presence of a "fatal" error. We could have chosen to not log on a "standard" error 
				if (!refactoringStatus.hasFatalError()) {
					Assert.assertNotNull(watchedRefactoring.getSimpleRefactoringDescriptor(refactoringStatus));
				}
			}
		}

		return refactoringStatus;
	}

}
