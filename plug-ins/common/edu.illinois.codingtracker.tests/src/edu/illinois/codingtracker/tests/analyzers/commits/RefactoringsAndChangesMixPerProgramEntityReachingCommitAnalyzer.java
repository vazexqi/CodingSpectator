/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.commits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates per commit: how many program entities are refactored at least once, how
 * many of the refactored entities are also changed manually (or by a different refactoring) and
 * what fraction they constitute from the number of refactored program entities.
 * 
 * TODO: This class has some similarity with ChangesReachingCommitAnalyzer (and other classes in
 * this package). Consider factoring out common parts.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringsAndChangesMixPerProgramEntityReachingCommitAnalyzer extends CSVProducingAnalyzer {

	//Note that changedEntitiesClusterIDs contains not just the IDs of AST nodes that are directly affected, 
	//but rather all the cluster nodes IDs of the directly affected AST nodes.
	private final Map<String, Set<Long>> changedEntitiesClusterIDs= new HashMap<String, Set<Long>>();

	private final Map<String, Map<Long, Set<Long>>> refactoredEntitiesClusterIDs= new HashMap<String, Map<Long, Set<Long>>>();

	//Keeps track of the cluster node IDs for each program entity that is affected by the current (single) refactoring.
	private final Map<String, Map<Long, Set<Long>>> currentRefactoredEntitiesClusterIDs= new HashMap<String, Map<Long, Set<Long>>>();

	private final Map<String, Set<Long>> refactoredEntitiesIDs= new HashMap<String, Set<Long>>();

	private final Map<String, Set<Long>> refactoredAndChangedEntitiesIDs= new HashMap<String, Set<Long>>();

	private String currentASTFilePath;

	private boolean isInsideRefactoring= false;

	private int totalRefactoredEntitiesCount, totalRefactoredAndChangedEntitiesCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,commit timestamp,refactored program entities count,refactored and changed program entities count,ratio (%)\n";
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
			if (userOperation instanceof NewStartedRefactoringOperation) {
				handleStartedRefactoringOperation();
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				handleFinishedRefactoringOperation();
			} else if (userOperation instanceof ASTFileOperation) {
				currentASTFilePath= ((ASTFileOperation)userOperation).getResourcePath();
			} else if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			} else if (userOperation instanceof CommittedFileOperation) {
				handleCommittedFileOperation((CommittedFileOperation)userOperation);
			} else if (userOperation instanceof MovedResourceOperation) {
				handleMovedResourceOperation((MovedResourceOperation)userOperation);
			}
		}
		System.out.println("Total refactored program entities count: " + totalRefactoredEntitiesCount);
		System.out.println("Total refactored and changed program entities count: " + totalRefactoredAndChangedEntitiesCount);
	}

	private void handleStartedRefactoringOperation() {
		isInsideRefactoring= true;
		currentRefactoredEntitiesClusterIDs.clear();
	}

	private void handleFinishedRefactoringOperation() {
		isInsideRefactoring= false;
		//Keep in mind that a single refactoring may affect more than one file, so consider all modified files.
		for (String astFilePath : currentRefactoredEntitiesClusterIDs.keySet()) {
			Map<Long, Set<Long>> fileCurrentRefactoredEntitiesClusterIDs= getFileCurrentRefactoredEntitiesClusterIDs(astFilePath);
			Set<Long> fileChangedEntitiesClusterIDs= getFileChangedEntitiesClusterIDs(astFilePath);
			Map<Long, Set<Long>> fileRefactoredEntitiesClusterIDs= getFileRefactoredEntitiesClusterIDs(astFilePath);
			Set<Long> fileRefactoredEntitiesIDs= getFileRefactoredEntitiesIDs(astFilePath);
			Set<Long> fileRefactoredAndChangedEntitiesIDs= getFileRefactoredAndChangedEntitiesIDs(astFilePath);
			for (Entry<Long, Set<Long>> currentMapEntry : fileCurrentRefactoredEntitiesClusterIDs.entrySet()) {
				Long affectedEntityID= currentMapEntry.getKey();
				Set<Long> affectedEntityClusterIDs= currentMapEntry.getValue();
				fileRefactoredEntitiesIDs.add(affectedEntityID);
				if (doesIntersect(affectedEntityClusterIDs, fileChangedEntitiesClusterIDs)) {
					fileRefactoredAndChangedEntitiesIDs.add(affectedEntityID);
				}
				for (Entry<Long, Set<Long>> mapEntry : fileRefactoredEntitiesClusterIDs.entrySet()) {
					if (doesIntersect(affectedEntityClusterIDs, mapEntry.getValue())) {
						fileRefactoredAndChangedEntitiesIDs.add(affectedEntityID);
						fileRefactoredAndChangedEntitiesIDs.add(mapEntry.getKey());
					}
				}
			}
			//Add cluster IDs in a separate loop to avoid counting intersections among entities affected by the same refactoring.
			for (Entry<Long, Set<Long>> currentMapEntry : fileCurrentRefactoredEntitiesClusterIDs.entrySet()) {
				addClusterIDs(fileRefactoredEntitiesClusterIDs, currentMapEntry.getKey(), currentMapEntry.getValue());
			}
		}
	}

	private void handleASTOperation(ASTOperation astOperation) {
		long nodeID= astOperation.getNodeID();
		Set<Long> clusterNodesIDs= astOperation.getClusterNodeIDs();
		if (isInsideRefactoring) {
			addClusterIDs(getFileCurrentRefactoredEntitiesClusterIDs(currentASTFilePath), nodeID, clusterNodesIDs);
		} else {
			Set<Long> fileRefactoredAndChangedEntitiesIDs= getFileRefactoredAndChangedEntitiesIDs(currentASTFilePath);
			Map<Long, Set<Long>> fileRefactoredEntitiesClusterIDs= getFileRefactoredEntitiesClusterIDs(currentASTFilePath);
			getFileChangedEntitiesClusterIDs(currentASTFilePath).addAll(clusterNodesIDs);
			for (Entry<Long, Set<Long>> mapEntry : fileRefactoredEntitiesClusterIDs.entrySet()) {
				if (doesIntersect(clusterNodesIDs, mapEntry.getValue())) {
					fileRefactoredAndChangedEntitiesIDs.add(mapEntry.getKey());
				}
			}
		}
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		Set<Long> fileRefactoredEntitiesIDs= getFileRefactoredEntitiesIDs(committedFilePath);
		int refactoredEntitiesCount= fileRefactoredEntitiesIDs.size();
		Set<Long> fileRefactoredAndChangedEntitiesIDs= getFileRefactoredAndChangedEntitiesIDs(committedFilePath);
		int refactoredAndChangedEntitiesCount= fileRefactoredAndChangedEntitiesIDs.size();
		double ratio= (double)refactoredAndChangedEntitiesCount * 100 / refactoredEntitiesCount;
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(),
						refactoredEntitiesCount, refactoredAndChangedEntitiesCount, Math.round(ratio));
		totalRefactoredEntitiesCount+= refactoredEntitiesCount;
		totalRefactoredAndChangedEntitiesCount+= refactoredAndChangedEntitiesCount;
		//Reset the statistics for the committed file.
		fileRefactoredEntitiesIDs.clear();
		fileRefactoredAndChangedEntitiesIDs.clear();
		getFileRefactoredEntitiesClusterIDs(committedFilePath).clear();
		getFileChangedEntitiesClusterIDs(committedFilePath).clear();
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, getAllFilePaths())) {
			String newFilePath= StringHelper.replacePrefix(filePath, oldPrefix, newPrefix);
			Set<Long> fileChangedEntitiesClusterIDs= changedEntitiesClusterIDs.remove(filePath);
			changedEntitiesClusterIDs.put(newFilePath, fileChangedEntitiesClusterIDs);
			Map<Long, Set<Long>> fileRefactoredEntitiesClusterIDs= refactoredEntitiesClusterIDs.remove(filePath);
			refactoredEntitiesClusterIDs.put(newFilePath, fileRefactoredEntitiesClusterIDs);
			Map<Long, Set<Long>> fileCurrentRefactoredEntitiesClusterIDs= currentRefactoredEntitiesClusterIDs.remove(filePath);
			currentRefactoredEntitiesClusterIDs.put(newFilePath, fileCurrentRefactoredEntitiesClusterIDs);
			Set<Long> fileRefactoredEntitiesIDs= refactoredEntitiesIDs.remove(filePath);
			refactoredEntitiesIDs.put(newFilePath, fileRefactoredEntitiesIDs);
			Set<Long> fileRefactoredAndChangedEntitiesIDs= refactoredAndChangedEntitiesIDs.remove(filePath);
			refactoredAndChangedEntitiesIDs.put(newFilePath, fileRefactoredAndChangedEntitiesIDs);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		changedEntitiesClusterIDs.clear();
		refactoredEntitiesClusterIDs.clear();
		refactoredEntitiesIDs.clear();
		refactoredAndChangedEntitiesIDs.clear();
		currentASTFilePath= null;
		isInsideRefactoring= false;
		totalRefactoredEntitiesCount= 0;
		totalRefactoredAndChangedEntitiesCount= 0;
	}

	private Set<String> getAllFilePaths() {
		Set<String> allFilePaths= new HashSet<String>();
		allFilePaths.addAll(changedEntitiesClusterIDs.keySet());
		allFilePaths.addAll(refactoredEntitiesClusterIDs.keySet());
		allFilePaths.addAll(currentRefactoredEntitiesClusterIDs.keySet());
		return allFilePaths;
	}

	private Set<Long> getFileRefactoredAndChangedEntitiesIDs(String filePath) {
		return getFileIDs(refactoredAndChangedEntitiesIDs, filePath);
	}

	private Set<Long> getFileRefactoredEntitiesIDs(String filePath) {
		return getFileIDs(refactoredEntitiesIDs, filePath);
	}

	private Set<Long> getFileChangedEntitiesClusterIDs(String filePath) {
		return getFileIDs(changedEntitiesClusterIDs, filePath);
	}

	private Map<Long, Set<Long>> getFileRefactoredEntitiesClusterIDs(String filePath) {
		return getFileMapIDs(refactoredEntitiesClusterIDs, filePath);
	}

	private Map<Long, Set<Long>> getFileCurrentRefactoredEntitiesClusterIDs(String filePath) {
		return getFileMapIDs(currentRefactoredEntitiesClusterIDs, filePath);
	}

	private Map<Long, Set<Long>> getFileMapIDs(Map<String, Map<Long, Set<Long>>> idMap, String filePath) {
		Map<Long, Set<Long>> fileMapIDs= idMap.get(filePath);
		if (fileMapIDs == null) {
			fileMapIDs= new HashMap<Long, Set<Long>>();
			idMap.put(filePath, fileMapIDs);
		}
		return fileMapIDs;
	}

	private Set<Long> getFileIDs(Map<String, Set<Long>> idMap, String filePath) {
		Set<Long> fileIDs= idMap.get(filePath);
		if (fileIDs == null) {
			fileIDs= new HashSet<Long>();
			idMap.put(filePath, fileIDs);
		}
		return fileIDs;
	}

	private void addClusterIDs(Map<Long, Set<Long>> fileClusterIDsMap, long nodeID, Set<Long> clusterNodesIDs) {
		Set<Long> currentClusterNodeIDs= fileClusterIDsMap.get(nodeID);
		if (currentClusterNodeIDs == null) {
			currentClusterNodeIDs= new HashSet<Long>();
			fileClusterIDsMap.put(nodeID, currentClusterNodeIDs);
		}
		currentClusterNodeIDs.addAll(clusterNodesIDs);
	}

	private boolean doesIntersect(Set<Long> set1, Set<Long> set2) {
		Set<Long> tempSet= new HashSet<Long>();
		tempSet.addAll(set1);
		tempSet.retainAll(set2);
		return !tempSet.isEmpty();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactorings_changes_mix_entity";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
