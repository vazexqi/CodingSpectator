/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;


/**
 * This analyzer calculates how many changes are performed in between commits and how many of them
 * are actually committed.
 * 
 * @author Stas Negara
 * 
 */
public class ChangesReachingCommitAnalyzer extends CSVProducingAnalyzer {

	private final Map<String, Set<Long>> touchedIDs= new HashMap<String, Set<Long>>();

	private final Map<String, Set<Long>> addedIDs= new HashMap<String, Set<Long>>();

	private final Map<String, Integer> shadowedIDCounters= new HashMap<String, Integer>();

	private String currentASTFilePath;

	private int overallTotalChangesCount, overallCommittedChangesCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,commit timestamp,total changes,committed changes\n";
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
		System.out.println("Total changes count: " + overallTotalChangesCount);
		System.out.println("Committed changes count: " + overallCommittedChangesCount);
	}

	private void handleASTOperation(ASTOperation astOperation) {
		Set<Long> fileTouchedIDs= getFileTouchedIDs(currentASTFilePath);
		Set<Long> fileAddedIDs= getFileAddedIDs(currentASTFilePath);
		long nodeID= astOperation.getNodeID();
		boolean isNewTouchedNode= fileTouchedIDs.add(nodeID);
		if (astOperation.isAdd()) {
			if (!isNewTouchedNode) {
				throw new RuntimeException("Add AST operation for an already touched ID: " + astOperation);
			}
			fileAddedIDs.add(nodeID);
		} else if (!isNewTouchedNode) {
			int increment= 1;
			if (astOperation.isDelete() && fileAddedIDs.contains(nodeID)) {
				fileTouchedIDs.remove(nodeID); //Node is added and deleted in the same commit, so nothing reaches commit.
				increment= 2;
			}
			incrementCurrentFileShadowedIDCounter(increment);
		}
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		Set<Long> fileTouchedIDs= getFileTouchedIDs(committedFilePath);
		Integer committedFileShadowedIDCounter= shadowedIDCounters.get(committedFilePath);
		int committedChangesCount= fileTouchedIDs.size();
		int shadowedChangesCount= committedFileShadowedIDCounter == null ? 0 : committedFileShadowedIDCounter;
		int totalChangesCount= committedChangesCount + shadowedChangesCount;
		overallTotalChangesCount+= totalChangesCount;
		overallCommittedChangesCount+= committedChangesCount;
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(), totalChangesCount, committedChangesCount);
		//Reset the statistics for the committed file.
		fileTouchedIDs.clear();
		getFileAddedIDs(committedFilePath).clear();
		shadowedIDCounters.put(committedFilePath, 0);
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, touchedIDs.keySet())) {
			String newFilePath= filePath.replaceFirst(oldPrefix, newPrefix);
			Set<Long> fileTouchedIDs= touchedIDs.remove(filePath);
			touchedIDs.put(newFilePath, fileTouchedIDs);
			Set<Long> fileAddedIDs= addedIDs.remove(filePath);
			addedIDs.put(newFilePath, fileAddedIDs);
			Integer fileShadowedIDCounter= shadowedIDCounters.remove(filePath);
			shadowedIDCounters.put(newFilePath, fileShadowedIDCounter);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		touchedIDs.clear();
		addedIDs.clear();
		shadowedIDCounters.clear();
		currentASTFilePath= null;
		overallTotalChangesCount= 0;
		overallCommittedChangesCount= 0;
	}

	private Set<Long> getFileTouchedIDs(String filePath) {
		return getFileIDs(touchedIDs, filePath);
	}

	private Set<Long> getFileAddedIDs(String filePath) {
		return getFileIDs(addedIDs, filePath);
	}

	private Set<Long> getFileIDs(Map<String, Set<Long>> idMap, String filePath) {
		Set<Long> fileIDs= idMap.get(filePath);
		if (fileIDs == null) {
			fileIDs= new HashSet<Long>();
			idMap.put(filePath, fileIDs);
		}
		return fileIDs;
	}

	private void incrementCurrentFileShadowedIDCounter(int increment) {
		Integer currentFileShadowedIDCounter= shadowedIDCounters.get(currentASTFilePath);
		int newCounter;
		if (currentFileShadowedIDCounter == null) {
			newCounter= increment;
		} else {
			newCounter= currentFileShadowedIDCounter + increment;
		}
		shadowedIDCounters.put(currentASTFilePath, newCounter);
	}

	@Override
	protected String getResultFilePostfix() {
		return ".changes_reaching_commit";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
