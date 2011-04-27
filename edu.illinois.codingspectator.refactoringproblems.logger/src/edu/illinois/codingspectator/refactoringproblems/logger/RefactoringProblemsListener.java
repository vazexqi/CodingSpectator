/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;


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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;
import org.eclipse.ui.IStartup;

/**
 * 
 * This class is based on
 * edu.illinois.codingspectator.codingtracker.listeners.OperationHistoryListener and
 * edu.illinois.codingspectator.codingtracker.listeners.RefactoringExecutionListener
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

	private Set<CompilationUnit> affectedCompilationUnits;

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
		} else if (hasOperationFailed(event)) {
			storeAndCompareProblems();
		}
	}

	private void makeAffectedCompilationUnitsBecomeWorkingCopies() throws JavaModelException {
		for (CompilationUnit cu : affectedCompilationUnits) {
			cu.becomeWorkingCopy(new NullProgressMonitor());
		}
	}

	private void storeCurrentProblems() throws JavaModelException {
		makeAffectedCompilationUnitsBecomeWorkingCopies();
		Set<DefaultProblemWrapper> computeProblems= problemsFinder.computeProblems(affectedCompilationUnits);
		problemsComparer.pushNewProblemsSet(computeProblems);
	}

	private Set<CompilationUnit> getAffectedCompilationUnits(OperationHistoryEvent event) {
		Set<CompilationUnit> affectedCompilationUnits= new HashSet<CompilationUnit>();
		IUndoableOperation undoableOperation= event.getOperation();
		if (undoableOperation instanceof TriggeredOperations) {
			IUndoableOperation triggeringOperation= ((TriggeredOperations)undoableOperation).getTriggeringOperation();
			if (triggeringOperation instanceof UndoableOperation2ChangeAdapter) {
				Object[] affectedObjects= ((UndoableOperation2ChangeAdapter)triggeringOperation).getAllAffectedObjects();
				if (affectedObjects != null) {
					for (Object affectedObject : affectedObjects) {
						if (affectedObject instanceof CompilationUnit) {
							affectedCompilationUnits.add((CompilationUnit)affectedObject);
						}
					}
				}
			}
		}
		return affectedCompilationUnits;
	}

	private boolean isAboutToRefactor(OperationHistoryEvent event) {
		int eventType= event.getEventType();

		return eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || (eventType == OperationHistoryEvent.ABOUT_TO_REDO) ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO;
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
	 * @return true if the operation history event indicates that it's not possible to performed the
	 *         operation, false otherwise.
	 */
	private boolean hasOperationFailed(OperationHistoryEvent event) {
		return event.getEventType() == OperationHistoryEvent.OPERATION_NOT_OK;
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		if (isRefactoringPerformedEvent(event)) {
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

class ProblemsComparer {

	private long previousTimestamp= -1;

	private Set<DefaultProblemWrapper> previousProblems;

	private long currentTimestamp= -1;

	private Set<DefaultProblemWrapper> currentProblems;

	public void pushNewProblemsSet(Set<DefaultProblemWrapper> problems) {
		previousProblems= currentProblems;
		previousTimestamp= currentTimestamp;
		currentProblems= problems;
		currentTimestamp= System.currentTimeMillis();
	}

	public ProblemChanges compareProblems() {
		return new ProblemChanges(currentTimestamp, setDifference(currentProblems, previousProblems), previousTimestamp, setDifference(previousProblems, currentProblems));
	}

	/**
	 * 
	 * @param left
	 * @param right
	 * @return left - right
	 */
	public Set<DefaultProblemWrapper> setDifference(Set<DefaultProblemWrapper> left, Set<DefaultProblemWrapper> right) {
		Set<DefaultProblemWrapper> copyOfLeft= new HashSet<DefaultProblemWrapper>(left);
		copyOfLeft.removeAll(right);
		return copyOfLeft;
	}
}
