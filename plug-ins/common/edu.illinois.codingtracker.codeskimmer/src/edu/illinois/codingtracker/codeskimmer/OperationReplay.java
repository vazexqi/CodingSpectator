package edu.illinois.codingtracker.codeskimmer;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ui.IEditorPart;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public class OperationReplay {

	private ArrayList<Filter> filters;
	private Iterator<UserOperation> operationIterator;

	private UserOperation currentOperation;
	private UserOperation lastOperation;

	private long lastSnapshotTimestamp;
	private IEditorPart currentEditor;

	private boolean isPaused;
	private long maxDelayTime;

	public OperationReplay(ArrayList<UserOperation> operations,
			ArrayList<Filter> filters, long maxDelayTime) {
		this.filters = filters;

		this.operationIterator = operations.iterator();
		advanceCurrentOperation(); // initialize to first operation

		this.lastSnapshotTimestamp = -1;
		this.currentEditor = null;
		this.isPaused = false;

		this.maxDelayTime = maxDelayTime;
	}

	public UserOperation advanceCurrentOperation() {
		lastOperation = currentOperation;
		currentOperation = null;

		if (operationIterator.hasNext()) {
			UserOperation nextUserOperation = (UserOperation) operationIterator
					.next();
			if (nextUserOperation instanceof SnapshotedFileOperation) {
				lastSnapshotTimestamp = nextUserOperation.getTime();
			}
			if (!(nextUserOperation instanceof ASTOperation)
					&& !(nextUserOperation instanceof ASTFileOperation)
					&& !(nextUserOperation instanceof InferredUnknownTransformationOperation)
					&& nextUserOperation.getTime() != lastSnapshotTimestamp - 1) {
				currentOperation = nextUserOperation;
			}
		}

		return currentOperation;
	}

	/*
	 * Returns -1 if no operation to replay, 0 for Success, 1 for bad Editor, 2
	 * for other
	 */
	public int replayCurrentOperation() {
		try {
			if (!Configuration.isInTestMode && currentEditor != null
					&& currentEditor != EditorHelper.getActiveEditor()) {
				return 1;
			}
			if (currentOperation != null) {
				currentOperation.replay();
				currentEditor = EditorHelper.getActiveEditor();
			} else {
				return -1;
			}
		} catch (Exception e) {
			return 2;
		}

		return 0;
	}

	public long getCurrentDelayPeriod() {
		long delayPeriod = 0;
		if (lastOperation != null && currentOperation != null
				&& Filter.checkFiltersForMatch(filters, currentOperation)) {
			delayPeriod = currentOperation.getTime() - lastOperation.getTime();
		}
		return Math.min(delayPeriod, maxDelayTime);
	}

	public void pauseReplay(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public boolean isPaused() {
		return this.isPaused;
	}
}
