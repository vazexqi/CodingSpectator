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

	public static class Result {
		/**
		 * If this field is CheckConditionsOperation.FINAL_CONDITIONS, it means that
		 * RefactoringChecker has checked the initial conditions and what remains to check are the
		 * final conditions. And, if this field is CheckConditionsOperation.ALL_CONDITIONS, it means
		 * that RefactoringChecker hasn't checked any conditions and all conditions remain to be
		 * checked. Finally, if this field is CheckConditionsOperation.NONE it means that enough
		 * checks have been performed, e.g. the performed checks have found fatal errors, and there
		 * is no need to check any further conditions.
		 */
		int remainingConditionsToCheck;

		RefactoringStatus refactoringStatus;

		public Result(int remainingConditionsToCheck, RefactoringStatus refactoringStatus) {
			super();
			Assert.assertNotNull(refactoringStatus);
			this.remainingConditionsToCheck= remainingConditionsToCheck;
			this.refactoringStatus= refactoringStatus;
		}

		public int getRemainingConditionsToCheck() {
			return remainingConditionsToCheck;
		}

		public RefactoringStatus getRefactoringStatus() {
			return refactoringStatus;
		}

	}

	static int i= 0;

	public static Result checkRefactoringDescriptorCreation(final Refactoring refactoring) throws OperationCanceledException, CoreException {
		System.out.println("checkRefactoringDescriptorCreation #" + ++i);
		int remainingConditionsToCheck= CheckConditionsOperation.ALL_CONDITIONS;
		RefactoringStatus refactoringStatus= new RefactoringStatus();
		if (refactoring instanceof IWatchedRefactoring) {
			IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
			if (watchedRefactoring.isWatched()) {
				if (refactoring instanceof InlineMethodRefactoring) {
					refactoringStatus.merge(refactoring.checkInitialConditions(new NullProgressMonitor()));
					remainingConditionsToCheck= CheckConditionsOperation.FINAL_CONDITIONS;
				} else if (refactoring instanceof ExtractMethodRefactoring) {
					// We don't execute checkInitialConditions since that would modify its initial settings.
				} else {
					refactoringStatus.merge(refactoring.checkInitialConditions(new NullProgressMonitor()));
					remainingConditionsToCheck= CheckConditionsOperation.FINAL_CONDITIONS;
				}

				if (refactoringStatus.hasFatalError()) {
					//See org.eclipse.ltk.core.refactoring.Refactoring#checkAllConditions(IProgressMonitor)
					remainingConditionsToCheck= CheckConditionsOperation.NONE;
				} else {
					Assert.assertNotNull(watchedRefactoring.getSimpleRefactoringDescriptor(refactoringStatus));
				}
			}
		}
		return new Result(remainingConditionsToCheck, refactoringStatus);
	}

}
