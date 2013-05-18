/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.codeskimmer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;

/**
 * Main controller for this plugin which handles communication between the
 * replay and operation model and the view. Actions are assigned to all the view
 * controls early during initialization. This class also handles the
 * multithreading of the replays.
 * 
 * @author Connor Simmons
 * 
 */
public class CodeSkimmerController {

	private final CodeSkimmerView skimView;
	private final CodeSkimmerLib skimLib;

	private ReplayExecutionThread replayExecutionThread;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition cvar = lock.newCondition();

	private boolean initialReset;

	public CodeSkimmerController(CodeSkimmerView codeSkimmerView) {
		this.skimView = codeSkimmerView;
		this.skimLib = new CodeSkimmerLib();

		initialReset = false;
	}

	/**
	 * Adds all the actions to their respective controls in the view
	 */
	public void addViewActions() {
		addOperationPaneActions();
		addVisualizationActions();
	}

	private void addOperationPaneActions() {
		skimView.setCaptureCodeFilterButtonListener(createCaptureCodeFilterListener());
		skimView.setClearCodeFiltersButtonListener(createClearCodeFiltersListener());
		skimView.setAddUsernameFilterButtonListener(createAddUsernameFilterListener());
		skimView.setClearUsernameFiltersButtonListner(createClearUsernameFiltersListener());
		skimView.setVisualizationSelectListener(createVisualizationSelectListener());

		skimView.setApplyOperationRangeButtonListener(createApplyOperationRangeListner());
		skimView.setResetOperationRangeButtonListener(createResetOperationRangeListner());
	}

	private void addVisualizationActions() {
		skimView.setVisualizationMouseListener(createVisualizationMouseListener());
	}

	/**
	 * Action taken when the rewind button is pressed. Clears the workspace then
	 * sets the current operation back to the very beginnign of the list.
	 */
	public void rewindButtonAction() {
		JavaProjectsUpkeeper.clearWorkspace();
		skimLib.resetCurrentOperation();
		updateViewVisualization();
		updateEnabledViewButtons();
	}

	/**
	 * Action taken when the or stop button is pressed. If we are currently
	 * replaying, this should act as the stop button and stop the playback
	 * execution. If we aren't currently replaying, we should initialize a new
	 * replay.
	 */
	public void playButtonAction() {
		if (!skimLib.isCurrentReplay()) { // initialize and start replay
			skimLib.initReplay(skimLib.getCurrentOperation(),
					skimLib.getCurrentRangeEnd(),
					CodeSkimmerConstants.MAX_REPLAY_DELAY_TIME);
			replayExecutionThread = new ReplayExecutionThread();
			replayExecutionThread.start();
		} else { // stop replay
			skimLib.endCurrentReplay();
		}
		updateEnabledViewButtons();
	}

	/**
	 * Action taken when the pause button is pressed. Simply updates the model
	 * so that the execution thread will know whether or not to suspend.
	 */
	public void pauseButtonAction() {
		if (!skimLib.isCurrentReplayPaused()) { // pause replay
			skimLib.pauseReplay(true);
		} else { // unpause replay
			skimLib.pauseReplay(false);
			lock.lock();
			try {
				cvar.signalAll();
			} finally {
				lock.unlock();
			}
		}

		updateEnabledViewButtons();
	}

	/**
	 * Action for when the step button is pressed. If we are not in the middle
	 * of a current replay, this will create one that only performs a single
	 * operation.
	 */
	public void stepButtonAction() {
		if (skimLib.isCurrentReplay()) { // we must be paused so just step once
			lock.lock();
			try {
				cvar.signalAll();
			} finally {
				lock.unlock();
			}
		} else { // create new replay with one operation
			skimLib.initReplay(skimLib.getCurrentOperation(),
					skimLib.getCurrentOperation(),
					CodeSkimmerConstants.MAX_REPLAY_DELAY_TIME);
			replayExecutionThread = new ReplayExecutionThread();
			replayExecutionThread.start();
		}
	}

	/**
	 * Called when the fast forward button is pressed. This creates a replay
	 * with a 0 max delay period so that all filters will be effectively
	 * ignored.
	 */
	public void fastForwardButtonAction() {
		// initialize and start replay with 0 delay
		skimLib.initReplay(skimLib.getCurrentOperation(),
				skimLib.getSelectedOperation(), 0);
		replayExecutionThread = new ReplayExecutionThread();
		replayExecutionThread.start();

		updateEnabledViewButtons();
	}

	/**
	 * Triggers dialogs for selecting an operation file and then specifying a
	 * username. The operations are then loaded and inserted into a collective
	 * list based on timestamp.
	 */
	public void loadButtonAction() {
		String filePath = skimView.getFileDialogSelection();
		String username = skimView.getUsernameDialogSelection();
		int numLoaded = skimLib.loadOperations(filePath, username);
		if (numLoaded < 0) {
			skimView.showPopupMessage("Wrong format. Could not load operations from file: "
					+ filePath);
		}

		skimView.setUserOperations(skimLib.getCurrentOperationRange());
		skimView.setUsernames(skimLib.getUsernames());
		updateEnabledViewButtons();
	}

	/**
	 * Completely resets the tool. All operations and filters are removed and
	 * the workspace is fully cleared.
	 */
	public void resetButtonAction() {
		initialReset = true;
		JavaProjectsUpkeeper.clearWorkspace();
		skimLib.resetAll();
		updateView();
	}

	/**
	 * Creates a listener which checks for when the code filter button is
	 * pressed. The action checks the current editor for which file is being
	 * edited, the range of text highlighted, then sends this information to the
	 * model which creates and adds a new filter.
	 * 
	 * @return the listener object
	 */
	private Listener createCaptureCodeFilterListener() {
		Listener captureSelectionListener = new Listener() {
			public void handleEvent(Event e) {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					ITextEditor editor = (ITextEditor) page.getActiveEditor();
					TextSelection sel = (TextSelection) editor
							.getSelectionProvider().getSelection();

					String filePath = ((FileEditorInput) editor
							.getEditorInput()).getPath().toString();

					filePath = filePath.replaceFirst(ResourcesPlugin
							.getWorkspace().getRoot().getLocation().toString(),
							"");

					int selectionStart = sel.getOffset();
					int selectionEnd = selectionStart + sel.getLength();

					skimLib.addNewCodeFilter(filePath, selectionStart,
							selectionEnd);

					skimView.setFilters(skimLib.getFilters());

				} catch (Exception ex) {
					skimView.showPopupMessage("Could not capture a selection.");
				}
			}
		};

		return captureSelectionListener;
	}

	private Listener createClearCodeFiltersListener() {
		Listener clearSelectionListener = new Listener() {
			public void handleEvent(Event e) {
				skimLib.removeAllCodeFilters();
				skimView.setFilters(skimLib.getFilters());
			}
		};

		return clearSelectionListener;
	}

	private Listener createAddUsernameFilterListener() {
		Listener captureUserListener = new Listener() {
			public void handleEvent(Event e) {
				String username = skimView.getSelectedUsername();
				skimLib.addNewUsernameFilter(username);
				skimView.setFilters(skimLib.getFilters());
			}
		};

		return captureUserListener;
	}

	private Listener createClearUsernameFiltersListener() {
		Listener clearUsernameListener = new Listener() {
			public void handleEvent(Event e) {
				skimLib.removeAllUsernameFilters();
				skimView.setFilters(skimLib.getFilters());
			}
		};

		return clearUsernameListener;
	}

	private MouseListener createVisualizationMouseListener() {
		return new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				skimView.handleMouseClickEvent(e);
				skimLib.setSelectedOperation(skimView.getSelectedOperation());
				updateEnabledViewButtons();
			}

			public void mouseUp(MouseEvent e) {
			}

		};
	}

	public SelectionAdapter createVisualizationSelectListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createVisualization();
			}
		};
	}

	private Listener createApplyOperationRangeListner() {
		Listener applyOperationRangeListener = new Listener() {
			public void handleEvent(Event e) {
				skimLib.setCurrentRangeStart(skimView
						.getIntensityFirstSelection());
				skimLib.setCurrentRangeEnd(skimView
						.getIntensitySecondSelection());
				skimLib.setSelectedOperation(skimLib.getCurrentRangeStart());
				skimView.clearSelection();
				updateViewVisualization();
			}
		};

		return applyOperationRangeListener;
	}

	private Listener createResetOperationRangeListner() {
		Listener resetOperationRangeListner = new Listener() {
			public void handleEvent(Event e) {
				skimLib.setCurrentRangeStart(-1);
				skimLib.setCurrentRangeEnd(-1);
				skimView.clearSelection();
				updateViewVisualization();
			}
		};

		return resetOperationRangeListner;
	}

	private void createVisualization() {
		switch (skimView.getVisualizationSelection()) {
		case SEQUENCE:
			skimView.createSequenceVisualization();
			break;
		case INTENSITY:
			skimView.createIntensityVisualization();
			break;
		}
		addVisualizationActions();
		updateViewVisualization();
	}

	public void updateView() {
		updateEnabledViewButtons();
		updateViewVisualization();
	}

	private void updateViewVisualization() {
		skimView.setUserOperations(skimLib.getCurrentOperationRange());
		skimView.setFilters(skimLib.getFilters());
		skimView.setCurrentOperation(skimLib.getCurrentOperation());
	}

	private void updateEnabledViewButtons() {

		// playback buttons
		boolean enableRewindButton = false;
		boolean enablePlayButton = false;
		boolean enablePauseButton = false;
		boolean enableStepButton = false;
		boolean enableFastForwardButton = false;
		boolean enableLoadButton = false;
		boolean enableResetButton = true;

		// filter controls
		boolean enableCaptureCodeFilter = false;
		boolean enableRemoveCodeFilters = false;
		boolean enableUsernameCombo = false;
		boolean enableAddUsernameFilter = false;
		boolean enableRemoveUsernameFilters = false;

		// range controls
		boolean enableApplyOperationRangeButton = false;
		boolean enableResetOperationRangeButton = false;

		if (initialReset) {
			enableLoadButton = true;

			// if there have been operations loaded
			if (skimLib.getNumOperationsLoaded() > 0) {
				enablePlayButton = true;
				enableStepButton = true;
				// if we are in the middle of a current replay
				if (skimLib.isCurrentReplay()) {
					skimView.setPlayButtonIconToStop();
					enablePauseButton = true;
					enableLoadButton = false;
					enableResetButton = false;
					// if this replay is currently paused
					if (!skimLib.isCurrentReplayPaused()) {
						enableStepButton = false;
					}
					// we are not in the middle of a current replay
				} else {
					skimView.setPlayButtonIconToPlay();
					enableRewindButton = true;

					if (skimLib.getCurrentOperation() == null) {
						enablePlayButton = false;
						enableStepButton = false;
					} else {
						enableFastForwardButton = true;
					}

					enableCaptureCodeFilter = true;
					enableRemoveCodeFilters = true;
					enableUsernameCombo = true;
					enableAddUsernameFilter = true;
					enableRemoveUsernameFilters = true;

					enableResetOperationRangeButton = true;
					if (skimView.getIntensityFirstSelection() != -1
							&& skimView.getIntensitySecondSelection() != -1) {
						enableApplyOperationRangeButton = true;
					}
				}
			}
		}

		skimView.enableRewindButton(enableRewindButton);
		skimView.enablePlayButton(enablePlayButton);
		skimView.enablePauseButton(enablePauseButton);
		skimView.enableStepButton(enableStepButton);
		skimView.enableFastForwardButton(enableFastForwardButton);
		skimView.enableLoadButton(enableLoadButton);
		skimView.enableResetButton(enableResetButton);

		skimView.enableCaptureCodeFilterButton(enableCaptureCodeFilter);
		skimView.enableClearCodeFiltersButton(enableRemoveCodeFilters);
		skimView.enableUsernameFilterCombo(enableUsernameCombo);
		skimView.enableAddUsernameFilterButton(enableAddUsernameFilter);
		skimView.enableClearUsernameFiltersButton(enableRemoveUsernameFilters);

		skimView.enableApplyOperationRangeButton(enableApplyOperationRangeButton);
		skimView.enableResetOperationRangeButton(enableResetOperationRangeButton);
	}

	private class ReplayExecutionThread extends Thread {

		private boolean stoppedDueToException;

		ReplayExecutionThread() {
			stoppedDueToException = false;
		}

		@Override
		public void run() {
			try {
				while (true) {
					long startTime = System.currentTimeMillis();
					executeNextUserOperationInUIThread();
					updateViewInUIThread();
					long endTime = System.currentTimeMillis();

					lock.lock();
					try {
						if (!skimLib.isCurrentReplay()) {
							break;
						} else if (skimLib.isCurrentReplayPaused()) {
							if (skimLib.isCurrentReplayPaused()) {
								cvar.await();
							}
						} else {
							simulateDelay(skimLib.getDelayPeriod(), endTime
									- startTime);
						}
					} finally {
						lock.unlock();
					}

				}
			} catch (InterruptedException e) { // execution was interrupted
				skimView.showPopupMessage("THREAD INTERRUPTED");
			} finally {
				if (stoppedDueToException) {
					skimView.showPopupMessage("Replay has stopped due to an exception.");
				}
				updateEnabledViewButtonsInUIThread();
			}
		}

		private void executeNextUserOperationInUIThread() {
			skimView.getViewDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					int retVal = skimLib.executeNextReplayOperation();
					if (retVal > 0) {
						stoppedDueToException = true;
					}
				}
			});
		}

		private void updateViewInUIThread() {
			skimView.getViewDisplay().syncExec(new Runnable() {
				public void run() {
					skimView.setCurrentOperation(skimLib.getCurrentOperation());
				}
			});
		}

		private void updateEnabledViewButtonsInUIThread() {
			skimView.getViewDisplay().syncExec(new Runnable() {
				public void run() {
					updateEnabledViewButtons();
				}
			});
		}

		private void simulateDelay(long delayTime, long executionTime) {
			long sleepTime = delayTime - executionTime;
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}
}
