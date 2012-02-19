/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.commits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates per commit: how many changes are performed and how many of them are
 * performed after running tests but before committing. Thus, commits without preceding tests are
 * ignored. Also, it computes how many of untested changes (i.e. changes after tests before commits)
 * are commenting/uncommenting and undoing.
 * 
 * TODO: This class has some similarity with ChangesReachingCommitAnalyzer and
 * RefactoringsAndChangesMixReachingCommitAnalyzer. Consider factoring out common parts.
 * 
 * @author Stas Negara
 * 
 */
public class ChangesAfterTestBeforeCommitAnalyzer extends CSVProducingAnalyzer {

	private final Map<String, Integer> commitChangesCounter= new HashMap<String, Integer>();

	private final Map<String, Set<ASTOperation>> commitAfterTestChanges= new HashMap<String, Set<ASTOperation>>();

	private final Set<String> filesCommittedAfterTest= new HashSet<String>();

	private boolean isNeverTested= true;

	private String currentASTFilePath;

	private int totalCommitChangesCount;

	private int totalCommitAfterTestChangesCount;

	private int totalCommitAfterTestCommentingOrUncommentingChangesCount;

	private int totalCommitAfterTestUndoingChangesCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,commit timestamp,changes count,after test changes count,after test commenting/uncommenting changes count,after test undoing changes count\n";
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
		initialize();
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTFileOperation) {
				currentASTFilePath= ((ASTFileOperation)userOperation).getResourcePath();
			} else if (userOperation instanceof TestSessionStartedOperation) {
				handleTestSessionStartedOperation();
			} else if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			} else if (userOperation instanceof CommittedFileOperation) {
				handleCommittedFileOperation((CommittedFileOperation)userOperation);
			} else if (userOperation instanceof MovedResourceOperation) {
				handleMovedResourceOperation((MovedResourceOperation)userOperation);
			}
		}
		System.out.println("Total commit changes count: " + totalCommitChangesCount);
		System.out.println("Total commit after test changes count: " + totalCommitAfterTestChangesCount);
		System.out.println("Total commit after test commenting/uncommenting changes count: " + totalCommitAfterTestCommentingOrUncommentingChangesCount);
		System.out.println("Total commit after test undoing changes count: " + totalCommitAfterTestUndoingChangesCount);
	}

	private void handleTestSessionStartedOperation() {
		isNeverTested= false;
		filesCommittedAfterTest.clear();
		commitAfterTestChanges.clear();
	}

	private void handleASTOperation(ASTOperation astOperation) {
		incrementCounter(commitChangesCounter);
		getFileCommitAfterTestChanges(currentASTFilePath).add(astOperation);
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		if (isNeverTested || filesCommittedAfterTest.contains(committedFilePath)) {
			//Skip this commit as there is no preceding test run. But first, reset statistics for the committed file.
			commitChangesCounter.remove(committedFilePath);
			return;
		}
		ChangeCounters changeCounters= new ChangeCounters(committedFilePath);
		updateTotalCounters(changeCounters);
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(), changeCounters.commitChangesCount,
				changeCounters.commitAfterTestChangesCount, changeCounters.commitAfterTestCommentingOrUncommentingChangesCount,
				changeCounters.commitAfterTestUndoingChangesCount);

		filesCommittedAfterTest.add(committedFilePath);
		//Reset statistics for the committed file.
		commitChangesCounter.remove(committedFilePath);
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, commitChangesCounter.keySet())) {
			String newFilePath= StringHelper.replacePrefix(filePath, oldPrefix, newPrefix);
			Integer commitChangesCount= commitChangesCounter.remove(filePath);
			commitChangesCounter.put(newFilePath, commitChangesCount);
			Set<ASTOperation> fileCommitAfterTestChanges= commitAfterTestChanges.remove(filePath);
			commitAfterTestChanges.put(newFilePath, fileCommitAfterTestChanges);
			if (filesCommittedAfterTest.remove(filePath)) {
				filesCommittedAfterTest.add(newFilePath);
			}
		}
	}

	private void initialize() {
		result= new StringBuffer();
		commitChangesCounter.clear();
		commitAfterTestChanges.clear();
		filesCommittedAfterTest.clear();
		isNeverTested= true;
		currentASTFilePath= null;
		totalCommitChangesCount= 0;
		totalCommitAfterTestChangesCount= 0;
		totalCommitAfterTestCommentingOrUncommentingChangesCount= 0;
		totalCommitAfterTestUndoingChangesCount= 0;
	}

	private void updateTotalCounters(ChangeCounters changeCounters) {
		totalCommitChangesCount+= changeCounters.commitChangesCount;
		totalCommitAfterTestChangesCount+= changeCounters.commitAfterTestChangesCount;
		totalCommitAfterTestCommentingOrUncommentingChangesCount+= changeCounters.commitAfterTestCommentingOrUncommentingChangesCount;
		totalCommitAfterTestUndoingChangesCount+= changeCounters.commitAfterTestUndoingChangesCount;
	}


	private Set<ASTOperation> getFileCommitAfterTestChanges(String filePath) {
		Set<ASTOperation> fileCommitAfterTestChanges= commitAfterTestChanges.get(filePath);
		if (fileCommitAfterTestChanges == null) {
			fileCommitAfterTestChanges= new HashSet<ASTOperation>();
			commitAfterTestChanges.put(filePath, fileCommitAfterTestChanges);
		}
		return fileCommitAfterTestChanges;
	}

	private void incrementCounter(Map<String, Integer> counter) {
		int count= getCount(counter, currentASTFilePath);
		count++;
		counter.put(currentASTFilePath, count);
	}

	private int getCount(Map<String, Integer> counter, String entry) {
		Integer count= counter.get(entry);
		if (count == null) {
			count= 0;
		}
		return count;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".changes_after_test_before_commit";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	class ChangeCounters {

		int commitChangesCount= 0;

		int commitAfterTestChangesCount= 0;

		int commitAfterTestCommentingOrUncommentingChangesCount= 0;

		int commitAfterTestUndoingChangesCount= 0;


		ChangeCounters(String filePath) {
			commitChangesCount= getCount(commitChangesCounter, filePath);
			Set<ASTOperation> fileCommitAfterTestChanges= getFileCommitAfterTestChanges(filePath);
			commitAfterTestChangesCount= fileCommitAfterTestChanges.size();
			for (ASTOperation astOperation : fileCommitAfterTestChanges) {
				if (astOperation.isCommentingOrUncommenting()) {
					commitAfterTestCommentingOrUncommentingChangesCount++;
				} else if (astOperation.isUndoing()) {
					commitAfterTestUndoingChangesCount++;
				}
			}
		}

	}

}
