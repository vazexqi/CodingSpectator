/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;


/**
 * This class estimates the number of hours a developer works on his code in Eclipse with our tool
 * recording his actions.
 * 
 * @author Stas Negara
 * 
 */
public class UsageTimeAnalyzer extends CSVProducingAnalyzer {

	private static final String EARLIEST_VERSION_FOR_ANALYSIS = "1.0.0.201104162211";
	
	private static final int threshold= 30 * 60 * 1000; // 30 minutes expressed in milliseconds


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,usage time (ms)\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return folderName.startsWith(VERSION_FOLDER_COMMON_PREFIX) && folderName.compareTo(EARLIEST_VERSION_FOR_ANALYSIS) > 0; // if folderName is a greater version than 1.0.0.201104162211
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		result= new StringBuffer();
		long usageTime= 0;
		if (userOperations.size() > 0) {
			long previousTimestamp= userOperations.get(0).getTime();
			for (UserOperation userOperation : userOperations) {
				long currentTimestamp= userOperation.getTime();
				long deltaTimestamp= currentTimestamp - previousTimestamp;
				if (deltaTimestamp > 0 && deltaTimestamp < threshold) {
					usageTime+= deltaTimestamp;
				}
				previousTimestamp= currentTimestamp;
			}
		}
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, usageTime);
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
