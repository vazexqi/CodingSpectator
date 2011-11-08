/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.methods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates how many changes are performed for methods at predefined time intervals
 * before and after a method is refactored.
 * 
 * TODO: The underlying algorithm is quite inefficient since to count changes before and after a
 * refactoring, it traverses user operations for each refactoring separately.
 * 
 * @author Stas Negara
 * 
 */
public class BeforeAndAfterRefactoringMethodChangesAnalyzer extends CSVProducingAnalyzer {

	private final List<UserOperation> userOperations= new LinkedList<UserOperation>();

	private final Map<Long, TimeIntervalsChangeCounter> beforeChangeCounters= new HashMap<Long, TimeIntervalsChangeCounter>();

	private final Map<Long, TimeIntervalsChangeCounter> afterChangeCounters= new HashMap<Long, TimeIntervalsChangeCounter>();

	private final int[] totalChangesCountBefore= new int[3];

	private final int[] totalChangesCountAfter= new int[3];


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,refactored method ID,refactoring timestamp,3 hours changes before,30 minutes changes before,5 minutes changes before,5 minutes changes after,30 minutes changes after,3 hours changes after\n";
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
		initializeAnalyzer(userOperations);
		for (int i= 0; i < userOperations.size(); i++) {
			if (userOperations.get(i) instanceof NewStartedRefactoringOperation) {
				handleStartedRefactoringOperation(i);
			}
		}
		System.out.println("Total: " + totalChangesCountBefore[2] + ", " + totalChangesCountBefore[1] + ", " +
				totalChangesCountBefore[0] + ", " + totalChangesCountAfter[0] + ", " + totalChangesCountAfter[1] +
				", " + totalChangesCountAfter[2]);
	}

	private void handleStartedRefactoringOperation(int startedRefactoringOperationIndex) {
		Set<Long> refactoredMethodIDs= new HashSet<Long>();
		for (int i= startedRefactoringOperationIndex + 1; i < userOperations.size(); i++) {
			UserOperation userOperation= userOperations.get(i);
			if (userOperation instanceof ASTOperation) {
				long methodID= ((ASTOperation)userOperation).getMethodID();
				if (methodID != -1) { //Check if there is a containing method.
					refactoredMethodIDs.add(methodID);
				}
			}
			if (userOperation instanceof FinishedRefactoringOperation) {
				handleRefactoredMethods(startedRefactoringOperationIndex, i, refactoredMethodIDs);
				return;
			}
		}
	}

	private void handleRefactoredMethods(int startedRefactoringOperationIndex, int finishedRefactoringOperationIndex, Set<Long> refactoredMethodIDs) {
		initializeChangeCounters(startedRefactoringOperationIndex, finishedRefactoringOperationIndex, refactoredMethodIDs);
		countBeforeChanges(startedRefactoringOperationIndex);
		countAfterChanges(finishedRefactoringOperationIndex);
		populateResults();
	}

	private void initializeChangeCounters(int startedRefactoringOperationIndex, int finishedRefactoringOperationIndex, Set<Long> refactoredMethodIDs) {
		beforeChangeCounters.clear();
		afterChangeCounters.clear();
		long startTimestamp= userOperations.get(startedRefactoringOperationIndex).getTime();
		long finishTimestamp= userOperations.get(finishedRefactoringOperationIndex).getTime();
		for (long refactoredMethodID : refactoredMethodIDs) {
			beforeChangeCounters.put(refactoredMethodID, new TimeIntervalsChangeCounter(startTimestamp));
			afterChangeCounters.put(refactoredMethodID, new TimeIntervalsChangeCounter(finishTimestamp));
		}
	}

	private void countBeforeChanges(int startedRefactoringOperationIndex) {
		countChanges(startedRefactoringOperationIndex, beforeChangeCounters, false);
	}

	private void countAfterChanges(int finishedRefactoringOperationIndex) {
		countChanges(finishedRefactoringOperationIndex, afterChangeCounters, true);
	}

	private void countChanges(int startIndex, Map<Long, TimeIntervalsChangeCounter> changeCounters, boolean isForward) {
		long startTimestamp= userOperations.get(startIndex).getTime();
		int index= isForward ? startIndex + 1 : startIndex - 1;
		while (isForward ? index < userOperations.size() : index >= 0) {
			UserOperation userOperation= userOperations.get(index);
			if (!TimeIntervalsChangeCounter.isInsideTimeIntervals(startTimestamp, userOperation.getTime())) {
				break;
			}
			if (userOperation instanceof ASTOperation) {
				long methodID= ((ASTOperation)userOperation).getMethodID();
				TimeIntervalsChangeCounter changeCounter= changeCounters.get(methodID);
				if (changeCounter != null) {
					changeCounter.countTimestamp(userOperation.getTime());
				}
			}
			index+= isForward ? 1 : -1;
		}
	}

	private void populateResults() {
		for (Entry<Long, TimeIntervalsChangeCounter> mapEntry : beforeChangeCounters.entrySet()) {
			long methodID= mapEntry.getKey();
			TimeIntervalsChangeCounter beforeChangeCounter= mapEntry.getValue();
			TimeIntervalsChangeCounter afterChangeCounter= afterChangeCounters.get(methodID);
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, methodID, beforeChangeCounter.getReferenceTimestamp(),
					beforeChangeCounter.getCount(2), beforeChangeCounter.getCount(1), beforeChangeCounter.getCount(0),
					afterChangeCounter.getCount(0), afterChangeCounter.getCount(1), afterChangeCounter.getCount(2));
			for (int i= 0; i < 3; i++) {
				totalChangesCountBefore[i]+= beforeChangeCounter.getCount(i);
				totalChangesCountAfter[i]+= afterChangeCounter.getCount(i);
			}
		}
	}

	private void initializeAnalyzer(List<UserOperation> userOperations) {
		this.userOperations.clear();
		this.userOperations.addAll(userOperations);
		result= new StringBuffer();
		for (int i= 0; i < 3; i++) {
			totalChangesCountBefore[i]= 0;
			totalChangesCountAfter[i]= 0;
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".before_after_refactoring_method_changes";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}

class TimeIntervalsChangeCounter {

	//5 minutes, 30 minutes, and 3 hours expressed in milliseconds.
	private final static int[] timeIntervals= { 5 * 60 * 1000, 30 * 60 * 1000, 3 * 60 * 60 * 1000 };

	private final int[] changeCounters= { 0, 0, 0 };

	private final long referenceTimestamp;


	TimeIntervalsChangeCounter(long referenceTimestamp) {
		this.referenceTimestamp= referenceTimestamp;
	}

	public long getReferenceTimestamp() {
		return referenceTimestamp;
	}

	public static boolean isInsideTimeIntervals(long referenceTimestamp, long timestamp) {
		//It is inside time intervals if the delta timestamp is less than the biggest (last) time interval.
		return getDeltaTimestamp(referenceTimestamp, timestamp) < timeIntervals[timeIntervals.length - 1];
	}

	public void countTimestamp(long timestamp) {
		if (!isInsideTimeIntervals(referenceTimestamp, timestamp)) {
			throw new RuntimeException("Can not count a timestamp that is not inside the time intervals: " + timestamp);
		}
		long deltaTimestamp= getDeltaTimestamp(referenceTimestamp, timestamp);
		for (int i= 0; i < timeIntervals.length; i++) {
			if (deltaTimestamp < timeIntervals[i]) {
				changeCounters[i]++;
			}
		}
	}

	public int getCount(int index) {
		return changeCounters[index];
	}

	private static long getDeltaTimestamp(long referenceTimestamp, long timestamp) {
		return Math.abs(referenceTimestamp - timestamp);
	}

}
