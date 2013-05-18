package edu.illinois.codingtracker.codeskimmer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public class CodeSkimmerLib {

	private ArrayList<UserOperation> operations;
	private ArrayList<Filter> filters;
	private ArrayList<String> usernames;

	private OperationReplay currentReplay;

	private int currentOperationIndex;
	private UserOperation currentRangeStart;
	private UserOperation currentRangeEnd;

	private UserOperation selectedOperation;

	public CodeSkimmerLib() {
		resetAll();
	}

	public void resetAll() {
		operations = new ArrayList<UserOperation>();
		filters = new ArrayList<Filter>();
		usernames = new ArrayList<String>();
		currentReplay = null;
		currentOperationIndex = -1;
		currentRangeStart = null;
		currentRangeEnd = null;
	}

	/**
	 * Loads the file and operations given in filename and applies the given
	 * username to each. Returns the number of operations successfully loaded or
	 * -1 if an error occurs.
	 */
	public int loadOperations(String filename, String username) {
		int numLoaded = 0;
		if (filename != null) {
			String operationsRecord = ResourceHelper.readFileContent(new File(
					filename));
			try {
				List<UserOperation> addedOps = OperationDeserializer
						.getUserOperations(operationsRecord);

				for (UserOperation op : addedOps) {
					op.setUsername(username);
				}

				inOrderOperationInsert(addedOps);
				numLoaded = addedOps.size();

				if (usernames.indexOf(username) == -1) {
					usernames.add(username);
				}

				// must re-initialize filters
				initAllFilters();

			} catch (RuntimeException e) {
				return -1;
			}
		}
		return numLoaded;
	}

	private void initAllFilters() {
		for (Filter f : filters) {
			f.init(operations);
		}
	}

	/**
	 * Inserts operations into our full list in order based on timestamp
	 */
	private void inOrderOperationInsert(List<UserOperation> addedOps) {
		int toInsertIndex = 0;
		for (int i = 0; i < operations.size(); i++) {
			if (toInsertIndex >= addedOps.size()) {
				break;
			} else {
				UserOperation cur = addedOps.get(toInsertIndex);
				if (cur.getTime() < operations.get(i).getTime()) {
					operations.add(i, cur);
					toInsertIndex++;
				}
			}
		}
		for (int i = toInsertIndex; i < addedOps.size(); i++) {
			operations.add(addedOps.get(i));
		}
	}

	public void addNewCodeFilter(String filePath, int selectionStart,
			int selectionEnd) {
		CodeFilter newFilter = new CodeFilter(selectionStart, selectionEnd,
				filePath, getCurrentOperation());
		if (!filters.contains(newFilter)) {
			filters.add(newFilter);
			newFilter.init(operations);
		}
	}

	public void removeAllCodeFilters() {
		for (int i = 0; i < filters.size(); i++) {
			if (filters.get(i) instanceof CodeFilter) {
				filters.remove(i);
				i--;
			}
		}
	}

	public void addNewUsernameFilter(String username) {
		UsernameFilter newFilter = new UsernameFilter(username);
		if (!filters.contains(newFilter)) {
			filters.add(newFilter);
			newFilter.init(operations);
		}
	}

	public void removeAllUsernameFilters() {
		for (int i = 0; i < filters.size(); i++) {
			if (filters.get(i) instanceof UsernameFilter) {
				filters.remove(i);
				i--;
			}
		}
	}

	public ArrayList<Filter> getFilters() {
		return this.filters;
	}

	public ArrayList<String> getUsernames() {
		return this.usernames;
	}

	/*
	 * These will ultimately reflect the current range of interest
	 */
	public UserOperation getCurrentRangeStart() {
		if (operations != null && operations.size() > 0) {
			if (currentRangeStart != null) {
				return currentRangeStart;
			} else {
				return operations.get(0);
			}
		} else {
			return null;
		}

	}
	
	public void setCurrentRangeStart(long startTimestamp) {
		currentRangeStart = this.getOperationNearestTimestamp(startTimestamp);
	}

	public UserOperation getCurrentRangeEnd() {
		if (operations != null && operations.size() > 0) {
			if (currentRangeEnd != null) {
				return currentRangeEnd;
			} else {
				return operations.get(operations.size() - 1);
			}
		} else {
			return null;
		}
	}
	
	public void setCurrentRangeEnd(long startTimestamp) {
		currentRangeEnd = this.getOperationNearestTimestamp(startTimestamp);
	}

	public ArrayList<UserOperation> getCurrentOperationRange() {
		ArrayList<UserOperation> currentOps = new ArrayList<UserOperation>();

		if (operations != null && operations.size() > 0) {
			int startIndex = operations.indexOf(getCurrentRangeStart());
			int endIndex = operations.indexOf(getCurrentRangeEnd());

			currentOps = new ArrayList<UserOperation>(operations.subList(
					startIndex, endIndex + 1));
		}

		return currentOps;
	}

	public UserOperation getCurrentOperation() {
		if (currentOperationIndex == -1) {
			currentOperationIndex = 0;
		}

		if (currentOperationIndex >= operations.size()
				|| currentOperationIndex < 0) {
			return null;
		}

		return operations.get(currentOperationIndex);
	}
	
	public void resetCurrentOperation() {
		currentOperationIndex = 0;
	}

	public void setCurrentOperation(UserOperation op) {
		currentOperationIndex = operations.indexOf(op);
	}

	public int getNumOperationsLoaded() {
		return operations.size();
	}

	public void setSelectedOperation(UserOperation op) {
		selectedOperation = op;
	}

	public UserOperation getSelectedOperation() {
		if (selectedOperation == null) {
			return getCurrentRangeEnd();
		} else {
			return selectedOperation;
		}
	}

	public UserOperation getOperationNearestTimestamp(long timestamp) {
		if (timestamp == -1) {
			return null;
		}
		
		long min_diff = Long.MAX_VALUE;
		UserOperation returnOp = null;
		for (UserOperation op : operations) {
			long diff = Math.abs(timestamp - op.getTime());
			if (diff <= min_diff) {
				min_diff = diff;
				returnOp = op;
			} else {
				break;
			}
		}
		return returnOp;
	}

	/*
	 * REPLAY METHODS - most of these methods are synchronized so they can be
	 * called by different execution threads
	 */

	public void initReplay(UserOperation startOperation,
			UserOperation endOperation, long maxDelayTime) {
		// create a new replay based on the current viewed range
		int startIndex = operations.indexOf(startOperation);
		int endIndex = operations.indexOf(endOperation);
		if (endIndex < startIndex) {
			endIndex = operations.indexOf(getCurrentRangeEnd());
		}

		ArrayList<UserOperation> replayOperations = new ArrayList<UserOperation>(
				operations.subList(startIndex, endIndex + 1));

		currentReplay = new OperationReplay(replayOperations, filters,
				maxDelayTime);
		currentOperationIndex = startIndex;
	}

	public synchronized boolean isCurrentReplay() {
		return getCurrentReplay() != null;
	}

	public synchronized OperationReplay getCurrentReplay() {
		return this.currentReplay;
	}

	public synchronized void endCurrentReplay() {
		this.currentReplay = null;
	}

	public synchronized void pauseReplay(boolean isPaused) {
		if (isCurrentReplay()) {
			currentReplay.pauseReplay(isPaused);
		}
	}

	public synchronized boolean isCurrentReplayPaused() {
		if (isCurrentReplay()) {
			return currentReplay.isPaused();
		}

		return false; // replay not initialized properly
	}

	/**
	 * Returns -1 if there are no more operations to replay, 0 for Success, 1
	 * for bad Editor, 2 for other
	 */
	public synchronized int executeNextReplayOperation() {
		int retVal = -1;
		if (isCurrentReplay()) {
			retVal = currentReplay.replayCurrentOperation();
			currentOperationIndex++;

			// end if there was an error or no next operation to replay
			if (retVal != 0 || currentReplay.advanceCurrentOperation() == null) {
				endCurrentReplay();
			}
		}
		return retVal;
	}

	/**
	 * Returns the time to delay
	 */
	public synchronized long getDelayPeriod() {
		if (isCurrentReplay()) {
			return currentReplay.getCurrentDelayPeriod();
		} else {
			return -1;
		}
	}
}
