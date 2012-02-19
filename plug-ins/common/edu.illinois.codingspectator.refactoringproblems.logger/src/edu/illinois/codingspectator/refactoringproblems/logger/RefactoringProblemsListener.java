/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;
import org.eclipse.ui.IStartup;

/**
 * 
 * This class is based on edu.illinois.codingtracker.listeners.OperationHistoryListener and
 * edu.illinois.codingtracker.listeners.RefactoringExecutionListener
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class RefactoringProblemsListener implements IStartup, IOperationHistoryListener, IRefactoringExecutionListener {

	private ProblemsFinder problemsFinder;

	private ProblemsComparer problemsComparer;

	private Set<ICompilationUnit> affectedCompilationUnits;

	public RefactoringProblemsListener() {
		problemsFinder= new ProblemsFinder();
		problemsComparer= new ProblemsComparer();
	}

	@Override
	public void earlyStartup() {
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		RefactoringCore.getHistoryService().addExecutionListener(this);
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		if (isAboutToRefactor(event)) {
			affectedCompilationUnits= getAffectedCompilationUnits(event);
			try {
				storeCurrentProblems();
			} catch (JavaModelException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "CODINGSPECTATOR: Failed to log compilation problems", e));
			}
		} else if (hasRefactoringFailed(event)) {
			storeAndCompareProblems();
		}
	}

	private Collection<ICompilationUnit> makeAffectedCompilationUnitsBecomeWorkingCopies() throws JavaModelException {
		Collection<ICompilationUnit> compilationUnitsMadeWorkingCopies= new HashSet<ICompilationUnit>();
		for (ICompilationUnit cu : affectedCompilationUnits) {
			if (cu.exists() && !cu.isWorkingCopy()) {
				cu.becomeWorkingCopy(new NullProgressMonitor());
				compilationUnitsMadeWorkingCopies.add(cu);
			}
		}
		return compilationUnitsMadeWorkingCopies;
	}

	private void revertWorkingCopyChanges(Collection<ICompilationUnit> wereWorkingCopies) throws JavaModelException {
		for (ICompilationUnit cu : wereWorkingCopies) {
			cu.discardWorkingCopy();
		}
	}

	private void storeCurrentProblems() throws JavaModelException {
		Collection<ICompilationUnit> compilationUnitsMadeWorkingCopies= makeAffectedCompilationUnitsBecomeWorkingCopies();
		Set<DefaultProblemWrapper> computeProblems= problemsFinder.computeProblems(affectedCompilationUnits);
		revertWorkingCopyChanges(compilationUnitsMadeWorkingCopies);
		problemsComparer.pushNewProblemsSet(computeProblems);
	}

	private UndoableOperation2ChangeAdapter getUndoableOperation2ChangeAdapter(IUndoableOperation operation) {
		if (operation instanceof TriggeredOperations) {
			operation= ((TriggeredOperations)operation).getTriggeringOperation();
		}
		if (operation instanceof UndoableOperation2ChangeAdapter) {
			return (UndoableOperation2ChangeAdapter)operation;
		}
		return null;
	}

	/**
	 * 
	 * @see org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService#getRefactoringDescriptor(IUndoableOperation)
	 * 
	 * @param operation
	 * @return
	 */
	private RefactoringDescriptor getRefactoringDescriptor(IUndoableOperation operation) {
		UndoableOperation2ChangeAdapter o= getUndoableOperation2ChangeAdapter(operation);
		if (o == null) {
			return null;
		}
		ChangeDescriptor changeDescriptor= o.getChangeDescriptor();
		if (changeDescriptor instanceof RefactoringChangeDescriptor) {
			return ((RefactoringChangeDescriptor)changeDescriptor).getRefactoringDescriptor();
		}
		return null;
	}

	private RefactoringDescriptor getRefactoringDescriptor(OperationHistoryEvent event) {
		return getRefactoringDescriptor(event.getOperation());
	}

	private Set<ICompilationUnit> getAffectedCompilationUnits(OperationHistoryEvent event) {
		Set<ICompilationUnit> affectedCompilationUnits= new HashSet<ICompilationUnit>();
		UndoableOperation2ChangeAdapter o= getUndoableOperation2ChangeAdapter(event.getOperation());
		if (o != null) {
			Object[] affectedObjects= o.getAllAffectedObjects();
			if (affectedObjects != null) {
				for (Object affectedObject : affectedObjects) {
					if (affectedObject instanceof ICompilationUnit) {
						affectedCompilationUnits.add((ICompilationUnit)affectedObject);
					} else if (affectedObject instanceof IPackageFragment) {
						affectedCompilationUnits.addAll(getPackageCompilationUnits((IPackageFragment)affectedObject));
					}
				}
			}
		}
		return affectedCompilationUnits;
	}

	private Set<ICompilationUnit> getPackageCompilationUnits(IPackageFragment packageFragment) {
		Set<ICompilationUnit> packageCompilationUnits= new HashSet<ICompilationUnit>();
		try {
			for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
				packageCompilationUnits.add(compilationUnit);
			}
		} catch (JavaModelException e) {
			//do nothing
		}
		return packageCompilationUnits;
	}

	private boolean isAboutToRefactor(OperationHistoryEvent event) {
		if (!isRefactoringEvent(event)) {
			return false;
		}

		int eventType= event.getEventType();

		return eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || (eventType == OperationHistoryEvent.ABOUT_TO_REDO) ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO;
	}

	private boolean isRefactoringEvent(OperationHistoryEvent event) {
		RefactoringDescriptor descriptor= getRefactoringDescriptor(event);
		return descriptor != null;
	}

	private long getRefactoringTimestamp(RefactoringExecutionEvent event) {
		return event.getDescriptor().getTimeStamp();
	}

	/**
	 * A refactroing can fail in many ways, e.g. if a resource that it expects does not exist.
	 * Create a project, say, P1. Create a class in the project, say, C. Create another project, say
	 * P2. Perform a move refactoring of Class C from P1 to P2. Delete P2. Go to the project
	 * explorer view and start undoing. First the project P2 will appear. The next undo will fail.
	 * That's the case we're looking for.
	 * 
	 * @param event the history event that is in progress.
	 * 
	 * @return true if the event is about the failure of a refactoring.
	 */
	private boolean hasRefactoringFailed(OperationHistoryEvent event) {
		return isRefactoringEvent(event) && event.getEventType() == OperationHistoryEvent.OPERATION_NOT_OK;
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		if (isRefactoringPerformedEvent(event)) {
			problemsComparer.setRefactoringTimestamp(getRefactoringTimestamp(event));
			storeAndCompareProblems();
		}
	}

	private void storeAndCompareProblems() {
		try {
			storeCurrentProblems();
			ProblemChanges problemChanges= problemsComparer.compareProblems();
			Logger.logDebug(problemChanges.toString());
			problemChanges.log();
		} catch (JavaModelException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "CODINGSPECTATOR: Failed to log compilation problems", e));
		}
	}

	private boolean isRefactoringPerformedEvent(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		return (eventType == RefactoringExecutionEvent.PERFORMED || eventType == RefactoringExecutionEvent.REDONE || eventType == RefactoringExecutionEvent.UNDONE);
	}

}
