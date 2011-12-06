/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.junit.TestCaseStartedOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionLaunchedOperation;


/**
 * This analyzer calculates several general characteristics of the analyzed data: the number of test
 * sessions, the number of commit events, and the number of file commits.
 * 
 * @author Stas Negara
 * 
 */
public class GeneralStatisticsAnalyzer extends CSVProducingAnalyzer {

	@Override
	protected String getTableHeader() {
		return "username,workspace ID,test sessions count,test case runs count,commit events count,file commits count\n";
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
	protected void postprocess(List<UserOperation> userOperations) {
		result= new StringBuffer();
		int testSessionsCount= 0;
		int testCaseRunsCount= 0;
		int commitEventsCount= 0;
		int fileCommitsCount= 0;
		boolean isInsideCommit= false;
		for (UserOperation userOperation : userOperations) {
			//TODO: The current implementation assumes that any uninterrupted sequence of CommittedFileOperations
			//belongs to a single commit event. Should we additionally consider some time threshold as well?
			if (userOperation instanceof CommittedFileOperation) {
				isInsideCommit= true;
				fileCommitsCount++;
			} else {
				if (isInsideCommit) {
					//A subsequence of commit operations is over => a commit event is over and can be counted.
					isInsideCommit= false;
					commitEventsCount++;
				}
				if (userOperation instanceof TestSessionLaunchedOperation) {
					testSessionsCount++;
				}
				if (userOperation instanceof TestCaseStartedOperation) {
					testCaseRunsCount++;
				}
			}
		}
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, testSessionsCount, testCaseRunsCount, commitEventsCount, fileCommitsCount);
		System.out.println("Test sessions count: " + testSessionsCount);
		System.out.println("Test case runs count: " + testCaseRunsCount);
		System.out.println("Commit events count: " + commitEventsCount);
		System.out.println("File commits count: " + fileCommitsCount);
	}

	@Override
	protected String getResultFilePostfix() {
		return ".general_statistics";
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
