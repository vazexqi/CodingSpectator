/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;


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

	private long lastTimestamp= -1;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,timestamp,refactoring kind,duration (ms)\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof ASTOperation) {
			handleASTOperation((ASTOperation)userOperation);
			lastTimestamp= userOperation.getTime();
		}
		if (userOperation instanceof InferredRefactoringOperation) {
			handleInferredRefactoring((InferredRefactoringOperation)userOperation);
		}
	}

	private void handleASTOperation(ASTOperation operation) {
		long refactoringID= operation.getRefactoringID();
		if (refactoringID != -1) {
			long currentDuration= getDurationForRefactoring(refactoringID);
			//TODO: !!!Either find a more objective way to measure duration or drop this research question.
			refactoringDurations.put(refactoringID, currentDuration + operation.getTime() - lastTimestamp);
		}
	}

	private long getDurationForRefactoring(long refactoringID) {
		Long duration= refactoringDurations.get(refactoringID);
		if (duration == null) {
			return 0;
		}
		return duration;
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
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion,
					refactoringDescriptor.timestamp, refactoringDescriptor.refactoringKind, refactoringDescriptor.duration);
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
			duration= getDurationForRefactoring(inferredRefactoring.getRefactoringID());
		}

	}

}
