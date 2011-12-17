/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.commits;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates how many changes are performed in between commits and how many of them
 * are shadowed by other changes in the same commit (i.e. how many of them do NOT reach a commit).
 * 
 * @author Stas Negara
 * 
 */
public class ChangesReachingCommitAnalyzer extends CSVProducingAnalyzer {

	private final Map<String, Map<Long, List<ASTOperation>>> astOperations= new HashMap<String, Map<Long, List<ASTOperation>>>();

	private String currentASTFilePath;

	private int overallChangesCount;

	private int overallShadowedChangesCount;

	private int overallShadowedCommentingOrUncommentingChangesCount;

	private int overallShadowedUndoingChangesCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,commit timestamp,total changes,shadowed changes,shadowed commenting or uncommenting changes,shadowed undoing changes\n";
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
			} else if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			} else if (userOperation instanceof CommittedFileOperation) {
				handleCommittedFileOperation((CommittedFileOperation)userOperation);
			} else if (userOperation instanceof MovedResourceOperation) {
				handleMovedResourceOperation((MovedResourceOperation)userOperation);
			}
		}
		System.out.println("Overall changes count: " + overallChangesCount);
		System.out.println("Overall shadowed changes count: " + overallShadowedChangesCount);
		System.out.println("Overall shadowed commenting or uncommenting changes count: " + overallShadowedCommentingOrUncommentingChangesCount);
		System.out.println("Overall shadowed undoing changes count: " + overallShadowedUndoingChangesCount);
	}

	private void handleASTOperation(ASTOperation astOperation) {
		Map<Long, List<ASTOperation>> fileASTOperations= getFileASTOperations(currentASTFilePath);
		List<ASTOperation> nodeASTOperations= getNodeASTOperations(fileASTOperations, astOperation.getNodeID());
		nodeASTOperations.add(astOperation);
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		Map<Long, List<ASTOperation>> fileASTOperations= getFileASTOperations(committedFilePath);
		ChangeCounters changeCounters= new ChangeCounters(fileASTOperations);
		updateOverallCounters(changeCounters);
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(),
				changeCounters.changesCount, changeCounters.shadowedChangesCount,
				changeCounters.shadowedCommentingOrUncommentingChangesCount, changeCounters.shadowedUndoingChangesCount);
		//Reset the statistics for the committed file.
		fileASTOperations.clear();
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, astOperations.keySet())) {
			String newFilePath= StringHelper.replacePrefix(filePath, oldPrefix, newPrefix);
			Map<Long, List<ASTOperation>> fileASTOperations= astOperations.remove(filePath);
			astOperations.put(newFilePath, fileASTOperations);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		astOperations.clear();
		currentASTFilePath= null;
		overallChangesCount= 0;
		overallShadowedChangesCount= 0;
		overallShadowedCommentingOrUncommentingChangesCount= 0;
		overallShadowedUndoingChangesCount= 0;
	}

	private void updateOverallCounters(ChangeCounters changeCounters) {
		overallChangesCount+= changeCounters.changesCount;
		overallShadowedChangesCount+= changeCounters.shadowedChangesCount;
		overallShadowedCommentingOrUncommentingChangesCount+= changeCounters.shadowedCommentingOrUncommentingChangesCount;
		overallShadowedUndoingChangesCount+= changeCounters.shadowedUndoingChangesCount;
	}

	private List<ASTOperation> getNodeASTOperations(Map<Long, List<ASTOperation>> fileASTOperations, long nodeID) {
		List<ASTOperation> nodeASTOperations= fileASTOperations.get(nodeID);
		if (nodeASTOperations == null) {
			nodeASTOperations= new LinkedList<ASTOperation>();
			fileASTOperations.put(nodeID, nodeASTOperations);
		}
		return nodeASTOperations;
	}

	private Map<Long, List<ASTOperation>> getFileASTOperations(String filePath) {
		if (filePath == null) { //A sanity check.
			throw new RuntimeException("An AST operation's filePath is null");
		}
		Map<Long, List<ASTOperation>> fileASTOperations= astOperations.get(filePath);
		if (fileASTOperations == null) {
			fileASTOperations= new HashMap<Long, List<ASTOperation>>();
			astOperations.put(filePath, fileASTOperations);
		}
		return fileASTOperations;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".changes_reaching_commit";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	class ChangeCounters {

		int changesCount= 0;

		int shadowedChangesCount= 0;

		int shadowedCommentingOrUncommentingChangesCount= 0;

		int shadowedUndoingChangesCount= 0;


		ChangeCounters(Map<Long, List<ASTOperation>> fileASTOperations) {
			for (List<ASTOperation> nodeOperations : fileASTOperations.values()) {
				int nodeOperationsCount= nodeOperations.size();
				changesCount+= nodeOperationsCount;
				for (int i= 0; i < nodeOperationsCount - 1; i++) {
					processShadowedOperation(nodeOperations.get(i));
				}
				if (nodeOperationsCount > 1) {
					//If the first operation is add and the last operation is delete, then the last operation is also shadowed.
					ASTOperation lastASTOperation= nodeOperations.get(nodeOperationsCount - 1);
					if (nodeOperations.get(0).isAdd() && lastASTOperation.isDelete()) {
						processShadowedOperation(lastASTOperation);
					}
				}
			}
		}

		void processShadowedOperation(ASTOperation astOperation) {
			shadowedChangesCount++;
			//Note that an operation that is both commenting (or uncommenting) and undoing is counted as 
			//commenting or uncommenting only to ensure that counts are disjoint.
			if (astOperation.isCommentingOrUncommenting()) {
				shadowedCommentingOrUncommentingChangesCount++;
			} else if (astOperation.isUndoing()) {
				shadowedUndoingChangesCount++;
			}
		}

	}

}
