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

	private final Map<String, Integer> shadowedIDCounters= new HashMap<String, Integer>();

	private String currentASTFilePath;


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
			//TODO: Discard operations of adding and then deleting a node that happen inside a refactoring, since these are
			//signs of spurious operations.
		}
	}

	private void handleASTOperation(ASTOperation astOperation) {
		Set<Long> fileTouchedIDs= getFileTouchedIDs(currentASTFilePath);
		boolean isNewTouchedNode= fileTouchedIDs.add(astOperation.getNodeID());
		if (!isNewTouchedNode) {
			incrementCurrentFileShadowedIDCounter();
		}
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		Set<Long> fileTouchedIDs= getFileTouchedIDs(committedFilePath);
		Integer committedFileShadowedIDCounter= shadowedIDCounters.get(committedFilePath);
		//TODO: This is a conservative estimate that does not account for disappearing changes, e.g. adding and then deleting a node.
		int committedChangesCount= fileTouchedIDs.size();
		int shadowedChangesCount= committedFileShadowedIDCounter == null ? 0 : committedFileShadowedIDCounter;
		int totalChangesCount= committedChangesCount + shadowedChangesCount;
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(), totalChangesCount, committedChangesCount);
		//Reset the statistics for the committed file.
		fileTouchedIDs.clear();
		shadowedIDCounters.put(committedFilePath, 0);
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, touchedIDs.keySet())) {
			String newFilePath= filePath.replaceFirst(oldPrefix, newPrefix);
			Set<Long> fileTouchedIDs= touchedIDs.remove(filePath);
			touchedIDs.put(newFilePath, fileTouchedIDs);
			Integer fileShadowedIDCounter= shadowedIDCounters.remove(filePath);
			shadowedIDCounters.put(newFilePath, fileShadowedIDCounter);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		touchedIDs.clear();
		shadowedIDCounters.clear();
		currentASTFilePath= null;
	}

	private Set<Long> getFileTouchedIDs(String filePath) {
		Set<Long> fileTouchedIDs= touchedIDs.get(filePath);
		if (fileTouchedIDs == null) {
			fileTouchedIDs= new HashSet<Long>();
			touchedIDs.put(filePath, fileTouchedIDs);
		}
		return fileTouchedIDs;
	}

	private void incrementCurrentFileShadowedIDCounter() {
		Integer currentFileShadowedIDCounter= shadowedIDCounters.get(currentASTFilePath);
		int newCounter;
		if (currentFileShadowedIDCounter == null) {
			newCounter= 1;
		} else {
			newCounter= currentFileShadowedIDCounter + 1;
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
