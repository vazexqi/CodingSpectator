/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.replaying;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.ViewerHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class UserOperationReplayer {

	private final OperationSequenceView operationSequenceView;

	private IAction loadAction;

	private final Collection<IAction> replayActions= new LinkedList<IAction>();

	private Iterator<UserOperation> userOperationsIterator;

	private UserOperation currentUserOperation;

	private Collection<UserOperation> breakpoints;

	private Thread userOperationExecutionThread;

	private volatile boolean userStoppedExecution= false;


	public UserOperationReplayer(OperationSequenceView operationSequenceView) {
		this.operationSequenceView= operationSequenceView;
	}

	void addToolBarActions() {
		IToolBarManager toolBarManager= operationSequenceView.getToolBarManager();
		toolBarManager.add(createLoadOperationSequenceAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(createReplayAction(newReplaySingleOperationAction(), "Step", "Replay the current user operation", false));
		toolBarManager.add(createReplayAction(newReplayOperationSequenceAction(true), "Simulate", "Simulate the remaining user operations at the user pace", true));
		toolBarManager.add(createReplayAction(newReplayOperationSequenceAction(false), "Replay", "Fast replay of the remaining user operations", true));
		toolBarManager.add(new Separator());
	}

	private IAction createLoadOperationSequenceAction() {
		loadAction= new Action() {
			@Override
			public void run() {
				FileDialog fileDialog= new FileDialog(operationSequenceView.getShell(), SWT.OPEN);
				String selectedFilePath= fileDialog.open();
				if (selectedFilePath != null) {
					String operationsRecord= FileHelper.getFileContent(new File(selectedFilePath));
					List<UserOperation> userOperations= OperationDeserializer.getUserOperations(operationsRecord);
					breakpoints= new HashSet<UserOperation>();
					userOperationsIterator= userOperations.iterator();
					advanceCurrentUserOperation();
					operationSequenceView.setTableViewerInput(userOperations);
					updateReplayActionsStateForCurrentUserOperation();
				}
			}
		};
		ViewerHelper.initAction(loadAction, "Load", "Load operation sequence from a file", true, false, false);
		return loadAction;
	}

	private IAction createReplayAction(IAction action, String actionText, String actionToolTipText, boolean isToggable) {
		ViewerHelper.initAction(action, actionText, actionToolTipText, false, isToggable, false);
		replayActions.add(action);
		return action;
	}

	private IAction newReplaySingleOperationAction() {
		return new Action() {
			@Override
			public void run() {
				replayAndAdvanceCurrentUserOperation();
				updateReplayActionsStateForCurrentUserOperation();
			}
		};
	}

	private IAction newReplayOperationSequenceAction(final boolean isSimulating) {
		return new Action() {
			@Override
			public void run() {
				if (!this.isChecked()) {
					userStoppedExecution= true;
					this.setEnabled(false);
					userOperationExecutionThread.interrupt();
				} else {
					replayUserOperationSequence(this, isSimulating);
				}
			}
		};
	}

	private void replayUserOperationSequence(IAction executionAction, boolean isSimulating) {
		userStoppedExecution= false;
		loadAction.setEnabled(false);
		toggleReplayActions(false);
		executionAction.setEnabled(true);
		userOperationExecutionThread= new UserOperationExecutionThread(executionAction, isSimulating);
		userOperationExecutionThread.start();
	}

	private void replayAndAdvanceCurrentUserOperation() {
		try {
			currentUserOperation.replay();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		advanceCurrentUserOperation();
	}

	private void advanceCurrentUserOperation() {
		if (userOperationsIterator.hasNext()) {
			currentUserOperation= userOperationsIterator.next();
		} else {
			currentUserOperation= null;
		}
		operationSequenceView.removeSelection();
		operationSequenceView.displayInOperationTextPane(currentUserOperation);
		operationSequenceView.refreshTableViewer();
	}

	private void updateReplayActionsStateForCurrentUserOperation() {
		toggleReplayActions(currentUserOperation != null);
	}

	private void toggleReplayActions(boolean state) {
		for (IAction action : replayActions) {
			action.setEnabled(state);
		}
	}

	boolean isBreakpoint(Object object) {
		return breakpoints.contains(object);
	}

	boolean isCurrentUserOperation(Object object) {
		return currentUserOperation == object;
	}

	void toggleBreakpoint(UserOperation userOperation) {
		if (breakpoints.contains(userOperation)) {
			breakpoints.remove(userOperation);
		} else {
			breakpoints.add(userOperation);
		}
	}

	private class UserOperationExecutionThread extends Thread {

		private final IAction executionAction;

		private final boolean isSimulating;

		private UserOperationExecutionThread(IAction executionAction, boolean isSimulating) {
			this.executionAction= executionAction;
			this.isSimulating= isSimulating;
		}

		@Override
		public void run() {
			do {
				long executedOperationTime= currentUserOperation.getTime();
				long startTime= System.currentTimeMillis();
				executeUserOperationInUIThread();
				if (shouldStopExecution()) {
					break;
				} else {
					if (isSimulating) {
						simulateDelay(executedOperationTime, startTime);
					}
					if (userStoppedExecution) {
						break;
					}
				}
			} while (true);
			updateToolBarActions();
		}

		private void executeUserOperationInUIThread() {
			operationSequenceView.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					replayAndAdvanceCurrentUserOperation();
				}
			});
		}

		private boolean shouldStopExecution() {
			return currentUserOperation == null || breakpoints.contains(currentUserOperation);
		}

		private void simulateDelay(long executedOperationTime, long startTime) {
			long finishTime= System.currentTimeMillis();
			long nextOperationTime= currentUserOperation.getTime();
			long sleepTime= nextOperationTime - executedOperationTime - (finishTime - startTime);
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					//ignore
				}
			}
		}

		private void updateToolBarActions() {
			executionAction.setChecked(false);
			loadAction.setEnabled(true);
			updateReplayActionsStateForCurrentUserOperation();
		}

	}

}
