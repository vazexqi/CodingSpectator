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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;
import org.eclipse.ui.IStartup;

/**
 * 
 * This class is based on
 * edu.illinois.codingspectator.codingtracker.listeners.OperationHistoryListener
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class RefactoringProblemsListener implements IStartup, IOperationHistoryListener {

	@Override
	public void earlyStartup() {
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new RefactoringProblemsListener());
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		int eventType= event.getEventType();
		if (isAboutEvent(eventType)) {
			Set<CompilationUnit> affectedCompilationUnits= getAffectedCompilationUnits(event);
			ProblemFinder problemFinder= new ProblemFinder(affectedCompilationUnits);
			try {
				problemFinder.computeProblems();
			} catch (JavaModelException e) {
				//FIXME
				e.printStackTrace();
			}
		}
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

	private boolean isAboutEvent(int eventType) {
		return eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || (eventType == OperationHistoryEvent.ABOUT_TO_REDO) ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO;
	}

}
