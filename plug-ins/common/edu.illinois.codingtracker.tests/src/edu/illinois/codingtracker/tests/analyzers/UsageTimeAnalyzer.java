/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.List;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;


/**
 * This class estimates the number of hours a developer works on his code in Eclipse with our tool
 * recording his actions.
 * 
 * @author Stas Negara
 * 
 */
public class UsageTimeAnalyzer extends CSVProducingAnalyzer {

	private static final String EARLIEST_VERSION_FOR_ANALYSIS= "1.0.0.201104162211";

	private static final int threshold= 30 * 60 * 1000; // 30 minutes expressed in milliseconds

	private StringBuffer auxiliaryResult;

	private long totalUsageTime= 0;

	private long startIntervalTimestamp;

	private long sequenceUsageTime;

	private long participantUsageTime;


	@Override
	protected String getTableHeader() {
		return "USERNAME,WORKSPACE_ID,VERSION,USAGE_TIME_IN_MILLI_SECS\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return folderName.startsWith(VERSION_FOLDER_COMMON_PREFIX) && folderName.compareTo(EARLIEST_VERSION_FOR_ANALYSIS) >= 0; // if folderName is a greater version than 1.0.0.201104162211
	}

	@Override
	protected boolean hasAuxiliaryResult() {
		return true;
	}

	@Override
	protected String getAuxiliaryResult() {
		return auxiliaryResult.toString();
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		if (userOperations.size() > 0) {
			long previousTimestamp= userOperations.get(0).getTime();
			for (UserOperation userOperation : userOperations) {
				if (!shouldIgnore(userOperation)) {
					long currentTimestamp= userOperation.getTime();
					handleConsecutiveTimestamps(previousTimestamp, currentTimestamp);
					previousTimestamp= currentTimestamp;
				}
			}
			//Record the final gap, which represents the boundary between sequences.
			recordGap(previousTimestamp);
		}
		totalUsageTime+= sequenceUsageTime;
		participantUsageTime+= sequenceUsageTime;
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, sequenceUsageTime);
	}

	private void handleConsecutiveTimestamps(long previousTimestamp, long currentTimestamp) {
		if (areWithinTimeBoundary(previousTimestamp, currentTimestamp)) {
			long deltaTimestamp= currentTimestamp - previousTimestamp;
			//Note that delta -1 does not mark a gap since it is used to handle snapshot effects.
			if (deltaTimestamp >= threshold || deltaTimestamp < -1) {
				recordGap(previousTimestamp);
			} else if (deltaTimestamp > 0 && startIntervalTimestamp == -1) {
				startIntervalTimestamp= previousTimestamp;
			}
		}
	}

	private void recordGap(long stopIntervalTimestamp) {
		if (startIntervalTimestamp != -1) {
			long usageInterval= stopIntervalTimestamp - startIntervalTimestamp;
			if (usageInterval < 0) {
				throw new RuntimeException("Usage interval should not be negative: " + usageInterval);
			}
			long startUsageTime= totalUsageTime + sequenceUsageTime;
			UsageTimeInterval usageTimeInterval= new UsageTimeInterval(postprocessedFileRelativePath, startUsageTime, startUsageTime + usageInterval, startIntervalTimestamp, stopIntervalTimestamp);
			auxiliaryResult.append(usageTimeInterval.serialize());
			sequenceUsageTime+= usageInterval;
			startIntervalTimestamp= -1;
		}
	}

	private boolean shouldIgnore(UserOperation userOperation) {
		//Ignore UNDONE and REDONE refactorings due to old timestamps (borrowed from the earlier PERFORMED refactorings).
		return userOperation instanceof NewStartedRefactoringOperation &&
				((NewStartedRefactoringOperation)userOperation).getRefactoringMode() != RefactoringMode.PERFORM;
	}

	private boolean areWithinTimeBoundary(long previousTimestamp, long currentTimestamp) {
		return isWithinTimeBoundary(previousTimestamp) && isWithinTimeBoundary(currentTimestamp);
	}

	private boolean isWithinTimeBoundary(long timestamp) {
		return timestamp > Configuration.usageTimeStart && timestamp < Configuration.usageTimeStop;
	}

	private void initialize() {
		result= new StringBuffer();
		auxiliaryResult= new StringBuffer();
		startIntervalTimestamp= -1;
		sequenceUsageTime= 0;
	}

	@Override
	protected void finishedProcessingParticipant() {
		System.out.println("Participant time: " + participantUsageTime);
		participantUsageTime= 0;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".usage_time";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldOutputIndividualResults() {
		return false;
	}

}
