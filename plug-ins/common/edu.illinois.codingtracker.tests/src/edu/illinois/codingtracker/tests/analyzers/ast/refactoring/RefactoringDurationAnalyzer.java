/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoring;


/**
 * This analyzer calculates for each manual inferred refactoring the time it took to complete the
 * refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringDurationAnalyzer extends InferredRefactoringAnalyzer {

	private final List<RefactoringDescriptor> refactoringDescriptors= new LinkedList<RefactoringDescriptor>();

	private final Map<Long, Long> refactoringDurations= new HashMap<Long, Long>();

	private final Map<RefactoringKind, TotalDuration> totalRefactoringDurations= new HashMap<RefactoringKind, TotalDuration>();

	private long lastTimestamp= -1;

	private final boolean shouldIgnoreInitialInterval= false;

	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,timestamp,refactoring kind,duration (ms)\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof ASTOperation) {
			handleASTOperation((ASTOperation)userOperation);
			//For delta intervals, consider only AST operations.
			lastTimestamp= userOperation.getTime();
		}
		if (userOperation instanceof InferredRefactoringOperation) {
			handleInferredRefactoring((InferredRefactoringOperation)userOperation);
		}
	}

	private void handleASTOperation(ASTOperation operation) {
		long refactoringID= operation.getTransformationID();
		if (refactoringID != -1) {
			Long duration= refactoringDurations.get(refactoringID);
			if (duration == null) {
				duration= 0l;
				if (shouldIgnoreInitialInterval) {
					//This is the first interval, so ignore it.
					refactoringDurations.put(refactoringID, 0l);
					return;
				}
			}
			long delta= operation.getTime() - lastTimestamp;
			//Consider delta only if it fits within a single refactoring dynamic timespan.
			if (delta < InferredRefactoring.oldAgeTimeThreshold) {
				refactoringDurations.put(refactoringID, duration + delta);
			}
		}
	}

	private void handleInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		if (!shouldIgnoreInferredRefactoring(inferredRefactoring)) {
			refactoringDescriptors.add(new RefactoringDescriptor(inferredRefactoring));
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		refactoringDescriptors.clear();
		refactoringDurations.clear();
	}

	@Override
	protected void populateResults() {
		for (RefactoringDescriptor refactoringDescriptor : refactoringDescriptors) {
			updateTotalRefactoringDurations(refactoringDescriptor);
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion,
					refactoringDescriptor.timestamp, refactoringDescriptor.refactoringKind, refactoringDescriptor.duration);
		}
	}

	private void updateTotalRefactoringDurations(RefactoringDescriptor refactoringDescriptor) {
		TotalDuration totalDuration= totalRefactoringDurations.get(refactoringDescriptor.refactoringKind);
		if (totalDuration == null) {
			totalDuration= new TotalDuration();
			totalRefactoringDurations.put(refactoringDescriptor.refactoringKind, totalDuration);
		}
		if (refactoringDescriptor.duration != 0) {
			totalDuration.addDuration(refactoringDescriptor.duration);
		}
	}

	@Override
	protected void finishedProcessingAllSequences() {
		System.out.println("Total average durations:");
		for (Entry<RefactoringKind, TotalDuration> entry : totalRefactoringDurations.entrySet()) {
			TotalDuration totalDuration= entry.getValue();
			System.out.println(entry.getKey() + "," + totalDuration.getCount() + "," + totalDuration.getMean() + "," +
								totalDuration.getStDev());
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_duration";
	}

	private class RefactoringDescriptor {

		private final long timestamp;

		private final RefactoringKind refactoringKind;

		private final long duration;

		RefactoringDescriptor(InferredRefactoringOperation inferredRefactoring) {
			timestamp= inferredRefactoring.getTime();
			refactoringKind= inferredRefactoring.getRefactoringKind();
			Long currentDuration= refactoringDurations.get(inferredRefactoring.getRefactoringID());
			duration= currentDuration == null ? 0 : currentDuration;
		}

	}

	private class TotalDuration {

		private List<Long> durations= new LinkedList<Long>();


		void addDuration(long duration) {
			durations.add(duration);
		}

		int getCount() {
			return durations.size();
		}

		double getMean() {
			if (durations.size() == 0) {
				return 0;
			}
			long totalDuration= 0;
			for (long duration : durations) {
				totalDuration+= duration;
			}
			return (double)totalDuration / durations.size();
		}

		double getStDev() {
			if (durations.size() == 0) {
				return 0;
			}
			double mean= getMean();
			double squares= 0;
			for (long duration : durations) {
				squares+= (mean - duration) * (mean - duration);
			}
			return Math.sqrt(squares / durations.size());
		}

	}

}
