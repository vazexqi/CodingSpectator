/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
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
		}
	}

	private void makeAffectedCompilationUnitsBecomeWorkingCopies() throws JavaModelException {
		for (CompilationUnit cu : affectedCompilationUnits) {
			cu.becomeWorkingCopy(new NullProgressMonitor());
		}
	}

	private void storeCurrentProblems() throws JavaModelException {
		makeAffectedCompilationUnitsBecomeWorkingCopies();
		Set<Map<String, DefaultProblemWrapper[]>> computeProblems= problemsFinder.computeProblems(affectedCompilationUnits);
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

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		if (isRefactoringPerformedEvent(event)) {
			try {
				storeCurrentProblems();
				Set<DefaultProblemWrapper> compareProblems= problemsComparer.compareProblems();
				System.err.println(compareProblems);
			} catch (JavaModelException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "CODINGSPECTATOR: Failed to log compilation problems", e));
			}
		}
	}

	private boolean isRefactoringPerformedEvent(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		return (eventType == RefactoringExecutionEvent.PERFORMED || eventType == RefactoringExecutionEvent.REDONE || eventType == RefactoringExecutionEvent.UNDONE);
	}

}

class ProblemsComparer {

	private Set<Map<String, DefaultProblemWrapper[]>> previousProblems;

	private Set<Map<String, DefaultProblemWrapper[]>> currentProblems;

	public void pushNewProblemsSet(Set<Map<String, DefaultProblemWrapper[]>> problems) {
		previousProblems= currentProblems;
		currentProblems= problems;
	}

	public Set<DefaultProblemWrapper> compareProblems() {
		Set<DefaultProblemWrapper> reportedRefactoringProblems= new HashSet<DefaultProblemWrapper>();
		currentProblems.removeAll(previousProblems);
		for (Map<String, DefaultProblemWrapper[]> newProblemsFromPerformedRefactoring : currentProblems) {
			DefaultProblemWrapper[] defaultProblemWrappers= newProblemsFromPerformedRefactoring.get((IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER));
			reportedRefactoringProblems.addAll(Arrays.asList(defaultProblemWrappers));
		}

		return reportedRefactoringProblems;
	}
}
