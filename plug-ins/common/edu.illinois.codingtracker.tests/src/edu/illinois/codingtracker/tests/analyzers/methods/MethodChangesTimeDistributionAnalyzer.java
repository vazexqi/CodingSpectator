/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates how many changes are performed in a method at predefined time intervals
 * before and after every change to this method.
 * 
 * @author Stas Negara
 * 
 */
public class MethodChangesTimeDistributionAnalyzer extends CSVProducingAnalyzer {

	private final List<UserOperation> userOperations= new ArrayList<UserOperation>();

	private UserOperation indexOperation;

	private final int timeIntervalsCount= 3;

	// 7.5, 15, and 30 minutes intervals.
	private final int[] timeIntervals= { (7 * 60 + 30) * 1000, 15 * 60 * 1000, 30 * 60 * 1000 };

	private final int[] beforeIndexes= new int[timeIntervalsCount];

	private final int[] afterIndexes= new int[timeIntervalsCount];

	//Can not create arrays for generics, so use a List instead.
	private final List<Map<Long, Integer>> beforeCounters= new ArrayList<Map<Long, Integer>>();

	private final List<Map<Long, Integer>> afterCounters= new ArrayList<Map<Long, Integer>>();

	private long[] totalBeforeCount= new long[timeIntervalsCount];

	private long[] totalAfterCount= new long[timeIntervalsCount];


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,30 minutes changes before,15 minutes changes before,7.5 minutes changes before,7.5 minutes changes after,15 minutes changes after,30 minutes changes after\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		if (userOperations.isEmpty()) {
			return;
		}

		long startTime= System.currentTimeMillis();
		initializeAnalyzer(userOperations);
		indexOperation= userOperations.get(0);
		initializeAfterCounters();

		computeCounters();

		System.out.println("Total before counts: ");
		for (int i= 0; i < timeIntervalsCount; i++) {
			System.out.print(totalBeforeCount[i] + " | ");
		}
		System.out.println("\nTotal after counts: ");
		for (int i= 0; i < timeIntervalsCount; i++) {
			System.out.print(totalAfterCount[i] + " | ");
		}
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, totalBeforeCount[2],
				totalBeforeCount[1], totalBeforeCount[0], totalAfterCount[0], totalAfterCount[1], totalAfterCount[2]);

		System.out.println("\nAnalysis time: " + (System.currentTimeMillis() - startTime));
	}

	private void initializeAfterCounters() {
		boolean[] isIndexComputed= new boolean[timeIntervalsCount];
		for (int i= 1; i < userOperations.size(); i++) {
			UserOperation currentOperation= userOperations.get(i);
			for (int j= 0; j < timeIntervalsCount; j++) {
				if (!isIndexComputed[j]) {
					if (isOutsideAfterBoundary(currentOperation, j)) {
						afterIndexes[j]= i - 1; //afterIndex points to the last operation inside the range
						isIndexComputed[j]= true;
					} else {
						incrementAfterCounter(currentOperation, j);
					}
				}
			}
			boolean areAllIndexesComputed= true;
			for (int j= 0; j < timeIntervalsCount; j++) {
				areAllIndexesComputed= areAllIndexesComputed && isIndexComputed[j];
			}
			if (areAllIndexesComputed) {
				break;
			}
		}
	}

	private void computeCounters() {
		for (int i= 1; i < userOperations.size(); i++) {
			updateTotalCounters(indexOperation);
			UserOperation newIndexOperation= userOperations.get(i);
			for (int j= 0; j < timeIntervalsCount; j++) {
				incrementBeforeCounter(indexOperation, j);
				decrementAfterCounter(newIndexOperation, j);
			}
			indexOperation= newIndexOperation;
			//Adjust the boundaries for the new indexOperation.
			for (int j= 0; j < timeIntervalsCount; j++) {
				adjustBeforeBoundary(j);
				adjustAfterBoundary(j);
			}
		}
		updateTotalCounters(indexOperation);
	}

	private void adjustBeforeBoundary(int timeIntervalIndex) {
		while (true) {
			UserOperation beforeOperation= userOperations.get(beforeIndexes[timeIntervalIndex]);
			if (isOutsideBeforeBoundary(beforeOperation, timeIntervalIndex)) {
				decrementBeforeCounter(beforeOperation, timeIntervalIndex);
				beforeIndexes[timeIntervalIndex]++;
			} else {
				break;
			}
		}
	}

	private void adjustAfterBoundary(int timeIntervalIndex) {
		for (int i= afterIndexes[timeIntervalIndex] + 1; i < userOperations.size(); i++) {
			UserOperation nextOperation= userOperations.get(i);
			if (isOutsideAfterBoundary(nextOperation, timeIntervalIndex)) {
				break;
			}
			incrementAfterCounter(nextOperation, timeIntervalIndex);
			afterIndexes[timeIntervalIndex]++;
		}
	}

	private boolean isOutsideAfterBoundary(UserOperation userOperation, int timeIntervalIndex) {
		return userOperation.getTime() - indexOperation.getTime() > timeIntervals[timeIntervalIndex];
	}

	private boolean isOutsideBeforeBoundary(UserOperation userOperation, int timeIntervalIndex) {
		return indexOperation.getTime() - userOperation.getTime() > timeIntervals[timeIntervalIndex];
	}

	private void updateTotalCounters(UserOperation userOperation) {
		long methodID= getMethodID(userOperation);
		if (methodID != -1) {
			for (int i= 0; i < timeIntervalsCount; i++) {
				Integer beforeCount= beforeCounters.get(i).get(methodID);
				Integer afterCount= afterCounters.get(i).get(methodID);
				totalBeforeCount[i]+= beforeCount == null ? 0 : beforeCount;
				totalAfterCount[i]+= afterCount == null ? 0 : afterCount;
			}
		}
	}

	private void incrementBeforeCounter(UserOperation userOperation, int timeIntervalIndex) {
		incrementCounter(userOperation, beforeCounters, timeIntervalIndex);
	}

	private void incrementAfterCounter(UserOperation userOperation, int timeIntervalIndex) {
		incrementCounter(userOperation, afterCounters, timeIntervalIndex);
	}

	private void incrementCounter(UserOperation userOperation, List<Map<Long, Integer>> counters, int timeIntervalIndex) {
		long methodID= getMethodID(userOperation);
		if (methodID != -1) {
			Map<Long, Integer> intervalCounters= counters.get(timeIntervalIndex);
			Integer currentCounter= intervalCounters.get(methodID);
			int newCounter= currentCounter == null ? 1 : currentCounter + 1;
			intervalCounters.put(methodID, newCounter);
		}
	}

	private void decrementBeforeCounter(UserOperation userOperation, int timeIntervalIndex) {
		decrementCounter(userOperation, beforeCounters, timeIntervalIndex);
	}

	private void decrementAfterCounter(UserOperation userOperation, int timeIntervalIndex) {
		decrementCounter(userOperation, afterCounters, timeIntervalIndex);
	}

	private void decrementCounter(UserOperation userOperation, List<Map<Long, Integer>> counters, int timeIntervalIndex) {
		long methodID= getMethodID(userOperation);
		if (methodID != -1) {
			Map<Long, Integer> intervalCounters= counters.get(timeIntervalIndex);
			Integer currentCounter= intervalCounters.get(methodID);
			int newCounter= currentCounter == null || currentCounter == 0 ? 0 : currentCounter - 1;
			if (newCounter == 0) {
				intervalCounters.remove(methodID); //For better performance.
			} else {
				intervalCounters.put(methodID, newCounter);
			}
		}
	}


	private long getMethodID(UserOperation userOperation) {
		if (userOperation instanceof ASTOperation) {
			return ((ASTOperation)userOperation).getMethodID();
		}
		//If userOperation is not ASTOperation, the containing method ID is considered to be -1.
		return -1;
	}

	private void initializeAnalyzer(List<UserOperation> userOperations) {
		result= new StringBuffer();

		//Copying userOperations, which is a LinkedList, into an ArrayList field takes more memory, 
		//but drastically improves performance.
		this.userOperations.clear();
		this.userOperations.addAll(userOperations);

		beforeCounters.clear();
		afterCounters.clear();
		for (int i= 0; i < timeIntervalsCount; i++) {
			beforeIndexes[i]= 0;
			afterIndexes[i]= 0;
			beforeCounters.add(new HashMap<Long, Integer>());
			afterCounters.add(new HashMap<Long, Integer>());
			totalBeforeCount[i]= 0;
			totalAfterCount[i]= 0;
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".time_distribution_method_changes";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
