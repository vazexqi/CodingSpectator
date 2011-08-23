/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.replaying;

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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;

import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.ViewerHelper;
import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class UserOperationReplayer {

	private enum ReplayPace {
		FAST, SIMULATE, CUSTOM
	}

	private final OperationSequenceView operationSequenceView;

	private IAction loadAction;

	private IAction resetAction;

	private IAction findAction;

	private final Collection<IAction> replayActions= new LinkedList<IAction>();

	private List<UserOperation> userOperations;

	private Iterator<UserOperation> userOperationsIterator;

	private UserOperation currentUserOperation;

	private Collection<UserOperation> breakpoints;

	private Thread userOperationExecutionThread;

	private volatile boolean forcedExecutionStop= false;

	private IEditorPart currentEditor= null;

	public UserOperationReplayer(OperationSequenceView operationSequenceView) {
		this.operationSequenceView= operationSequenceView;
	}

	void addToolBarActions() {
		IToolBarManager toolBarManager= operationSequenceView.getToolBarManager();
		toolBarManager.add(createLoadOperationSequenceAction());
		toolBarManager.add(createResetOperationSequenceAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(createReplayAction(newReplaySingleOperationAction(), "Step", "Replay the current user operation", false));
		toolBarManager.add(createReplayAction(newReplayOperationSequenceAction(ReplayPace.CUSTOM), "Custom", "Replay the remaining user operations at a custom pace", true));
		toolBarManager.add(createReplayAction(newReplayOperationSequenceAction(ReplayPace.SIMULATE), "Simulate", "Simulate the remaining user operations at the user pace", true));
		toolBarManager.add(createReplayAction(newReplayOperationSequenceAction(ReplayPace.FAST), "Fast", "Fast replay of the remaining user operations", true));
		toolBarManager.add(new Separator());
		toolBarManager.add(createFindOperationAction());
		toolBarManager.add(new Separator());
	}

	private IAction createFindOperationAction() {
		findAction= new Action() {
			@Override
			public void run() {
				FindOperationDialog dialog= new FindOperationDialog(operationSequenceView.getShell());
				if (dialog.open() == Window.OK) {
					UserOperation foundUserOperation= null;
					long minimumDeltaTime= Long.MAX_VALUE;
					for (UserOperation userOperation : userOperations) {
						long currentDeltaTime= Math.abs(userOperation.getTime() - dialog.getTimestamp());
						if (currentDeltaTime < minimumDeltaTime) {
							minimumDeltaTime= currentDeltaTime;
							foundUserOperation= userOperation;
							if (minimumDeltaTime == 0) {
								//Found the exact match, no need to proceed.
								break;
							}
						}
					}
					showSearchResults(dialog.getTimestamp(), foundUserOperation, minimumDeltaTime);
				}
			}

			private void showSearchResults(long searchedTimestamp, UserOperation foundUserOperation, long minimumDeltaTime) {
				if (foundUserOperation == null) {
					showMessage("There are no operations near timestamp " + searchedTimestamp);
				} else {
					if (minimumDeltaTime == 0) {
						showMessage("Found the exact match!");
					} else {
						showMessage("Found the closest operation, delta = " + minimumDeltaTime + " ms.");
					}
					operationSequenceView.setSelection(new StructuredSelection(foundUserOperation));
					if (!operationSequenceView.getOperationSequenceFilter().isShown(foundUserOperation)) {
						showMessage("Operation with timestamp " + foundUserOperation.getTime() + " is filtered out");
					}
				}
			}
		};
		ViewerHelper.initAction(findAction, "Find", "Find operation by its timestamp", false, false, false);
		return findAction;
	}

	private IAction createLoadOperationSequenceAction() {
		loadAction= new Action() {
			@Override
			public void run() {
				FileDialog fileDialog= new FileDialog(operationSequenceView.getShell(), SWT.OPEN);
				String selectedFilePath= fileDialog.open();
				if (selectedFilePath != null) {
					String operationsRecord= ResourceHelper.readFileContent(new File(selectedFilePath));
					try {
						userOperations= OperationDeserializer.getUserOperations(operationsRecord);
					} catch (RuntimeException e) {
						showMessage("Wrong format. Could not load user operations from file: " + selectedFilePath);
						throw e;
					}
					if (userOperations.size() > 0) {
						resetAction.setEnabled(true);
						findAction.setEnabled(true);
					}
					breakpoints= new HashSet<UserOperation>();
					prepareForReplay();
				}
			}

		};
		ViewerHelper.initAction(loadAction, "Load", "Load operation sequence from a file", true, false, false);
		return loadAction;
	}

	private IAction createResetOperationSequenceAction() {
		resetAction= new Action() {
			@Override
			public void run() {
				JavaProjectsUpkeeper.clearWorkspace();
				prepareForReplay();
			}
		};
		ViewerHelper.initAction(resetAction, "Reset", "Reset operation sequence", false, false, false);
		return resetAction;
	}

	private void prepareForReplay() {
		UserOperation.isRefactoring= false;
		currentEditor= null;
		userOperationsIterator= userOperations.iterator();
		advanceCurrentUserOperation();
		operationSequenceView.setTableViewerInput(userOperations);
		updateReplayActionsStateForCurrentUserOperation();
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
				try {
					replayAndAdvanceCurrentUserOperation();
				} catch (RuntimeException e) {
					showReplayExceptionMessage();
					throw e;
				}
				updateReplayActionsStateForCurrentUserOperation();
			}
		};
	}

	private IAction newReplayOperationSequenceAction(final ReplayPace replayPace) {
		return new Action() {
			@Override
			public void run() {
				if (!this.isChecked()) {
					forcedExecutionStop= true;
					this.setEnabled(false);
					userOperationExecutionThread.interrupt();
				} else {
					replayUserOperationSequence(this, replayPace);
				}
			}
		};
	}

	private void replayUserOperationSequence(IAction executionAction, ReplayPace replayPace) {
		if (replayPace == ReplayPace.CUSTOM) {
			CustomDelayDialog dialog= new CustomDelayDialog(operationSequenceView.getShell());
			if (dialog.open() == Window.CANCEL) {
				executionAction.setChecked(false);
				return;
			}
		}
		forcedExecutionStop= false;
		loadAction.setEnabled(false);
		resetAction.setEnabled(false);
		findAction.setEnabled(false);
		toggleReplayActions(false);
		executionAction.setEnabled(true);
		userOperationExecutionThread= new UserOperationExecutionThread(executionAction, replayPace, CustomDelayDialog.getDelay());
		userOperationExecutionThread.start();
	}

	private void replayAndAdvanceCurrentUserOperation() {
		try {
			if (!UserOperation.isInTestMode && currentEditor != null && currentEditor != EditorHelper.getActiveEditor()) {
				if (userOperationExecutionThread != null && userOperationExecutionThread.isAlive()) {
					forcedExecutionStop= true;
					userOperationExecutionThread.interrupt();
				}
				showMessage("The current editor is wrong. Should be: \"" + currentEditor.getTitle() + "\"");
				return;
			}
			currentUserOperation.replay();
			currentEditor= EditorHelper.getActiveEditor();
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

	private void showMessage(String message) {
		MessageBox messageBox= new MessageBox(operationSequenceView.getShell());
		messageBox.setMessage(message);
		messageBox.open();
	}

	private void showReplayExceptionMessage() {
		showMessage("An exception occured while executing the current user operation");
	}

	private class UserOperationExecutionThread extends Thread {

		private final IAction executionAction;

		private final ReplayPace replayPace;

		private final int customDelayTime;

		private UserOperationExecutionThread(IAction executionAction, ReplayPace replayPace, int customDelayTime) {
			this.executionAction= executionAction;
			this.replayPace= replayPace;
			this.customDelayTime= customDelayTime;
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
					if (replayPace == ReplayPace.SIMULATE) {
						long nextOperationTime= currentUserOperation.getTime();
						long delayTime= nextOperationTime - executedOperationTime;
						simulateDelay(delayTime, startTime);
					} else if (replayPace == ReplayPace.CUSTOM) {
						simulateDelay(customDelayTime, startTime);
					}
					if (forcedExecutionStop) {
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
					try {
						replayAndAdvanceCurrentUserOperation();
					} catch (RuntimeException e) {
						showReplayExceptionMessage();
						updateToolBarActions(); //Before re-throwing the exception, restore the tool bar.
						throw e;
					}
				}
			});
		}

		private boolean shouldStopExecution() {
			return currentUserOperation == null || breakpoints.contains(currentUserOperation);
		}

		private void simulateDelay(long delayTime, long startTime) {
			long finishTime= System.currentTimeMillis();
			long sleepTime= delayTime - (finishTime - startTime);
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
			resetAction.setEnabled(true);
			findAction.setEnabled(true);
			updateReplayActionsStateForCurrentUserOperation();
		}

	}

}
