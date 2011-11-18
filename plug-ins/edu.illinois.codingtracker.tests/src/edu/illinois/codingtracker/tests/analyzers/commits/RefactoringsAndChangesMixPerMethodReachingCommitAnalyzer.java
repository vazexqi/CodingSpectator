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
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates per commit: how many methods are changed manually or refactored, how
 * many methods are refactored at least once, how many of the refactored methods are also changed
 * manually (or by a different refactoring) and what fraction they constitute from the number of
 * refactored methods.
 * 
 * TODO: This class has some similarity with ChangesReachingCommitAnalyzer. Consider factoring out
 * common parts.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringsAndChangesMixPerMethodReachingCommitAnalyzer extends CSVProducingAnalyzer {

	private static final int ratioNormalizationFactor= 100;

	private final Map<String, Set<Long>> changedMethodsIDs= new HashMap<String, Set<Long>>();

	private final Map<String, Set<Long>> refactoredMethodsIDs= new HashMap<String, Set<Long>>();

	//Keeps track of methods that are both refactored and manually changed or are refactored more than once for the same commit.
	private final Map<String, Set<Long>> refactoredAndChangedMethodsIDs= new HashMap<String, Set<Long>>();

	//Keeps track of methods that are affected by the current (single) refactoring.
	private final Map<String, Set<Long>> currentRefactoredMethodsIDs= new HashMap<String, Set<Long>>();

	private String currentASTFilePath;

	private boolean isInsideRefactoring= false;

	private int totalRefactoredMethodsCount, totalRefactoredAndChangedMethodsCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,commit timestamp,changed methods count,refactored methods count,refactored and changed methods count,ratio (%)\n";
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
		System.out.println("Total refactored methods count: " + totalRefactoredMethodsCount);
		System.out.println("Total refactored and changed methods count: " + totalRefactoredAndChangedMethodsCount);
	}

	private void handleStartedRefactoringOperation() {
		isInsideRefactoring= true;
		currentRefactoredMethodsIDs.clear();
	}

	private void handleFinishedRefactoringOperation() {
		isInsideRefactoring= false;
		//Keep in mind that a single refactoring may affect more than one file, so add modified methods for all modified files.
		for (String astFilePath : currentRefactoredMethodsIDs.keySet()) {
			Set<Long> fileCurrentRefactoredMethodsIDs= getFileCurrentRefactoredMethodsIDs(astFilePath);
			Set<Long> fileRefactoredMethodsIDs= getFileRefactoredMethodsIDs(astFilePath);
			Set<Long> fileChangedMethodsIDs= getFileChangedMethodsIDs(astFilePath);
			Set<Long> fileRefactoredAndChangedMethodsIDs= getFileRefactoredAndChangedMethodsIDs(astFilePath);
			for (long methodID : fileCurrentRefactoredMethodsIDs) {
				if (fileRefactoredMethodsIDs.contains(methodID) || fileChangedMethodsIDs.contains(methodID)) {
					fileRefactoredAndChangedMethodsIDs.add(methodID);
				}
			}
			fileRefactoredMethodsIDs.addAll(fileCurrentRefactoredMethodsIDs);
		}
	}

	private void handleASTOperation(ASTOperation astOperation) {
		long methodID= astOperation.getMethodID();
		if (methodID != -1) {
			if (isInsideRefactoring) {
				Set<Long> fileCurrentRefactoredMethodsIDs= getFileCurrentRefactoredMethodsIDs(currentASTFilePath);
				fileCurrentRefactoredMethodsIDs.add(methodID);
			} else {
				Set<Long> fileChangedMethodsIDs= getFileChangedMethodsIDs(currentASTFilePath);
				fileChangedMethodsIDs.add(methodID);
				if (getFileRefactoredMethodsIDs(currentASTFilePath).contains(methodID)) {
					getFileRefactoredAndChangedMethodsIDs(currentASTFilePath).add(methodID);
				}
			}
		}
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		Set<Long> fileRefactoredMethodsIDs= getFileRefactoredMethodsIDs(committedFilePath);
		int refactoredMethodsCount= fileRefactoredMethodsIDs.size();
		Set<Long> fileChangedMethodsIDs= getFileChangedMethodsIDs(committedFilePath);
		fileChangedMethodsIDs.addAll(fileRefactoredMethodsIDs); //Add refactored methods to the manually changed methods.
		int changedOrRefactoredMethodsCount= fileChangedMethodsIDs.size();
		Set<Long> fileRefactoredAndChangedMethodsIDs= getFileRefactoredAndChangedMethodsIDs(committedFilePath);
		int refactoredAndChangedMethodsCount= fileRefactoredAndChangedMethodsIDs.size();
		double ratio= (double)refactoredAndChangedMethodsCount * ratioNormalizationFactor / refactoredMethodsCount;
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(),
				changedOrRefactoredMethodsCount, refactoredMethodsCount, refactoredAndChangedMethodsCount,
				Math.round(ratio));
		totalRefactoredMethodsCount+= refactoredMethodsCount;
		totalRefactoredAndChangedMethodsCount+= refactoredAndChangedMethodsCount;
		//Reset the statistics for the committed file.
		fileChangedMethodsIDs.clear();
		fileRefactoredMethodsIDs.clear();
		fileRefactoredAndChangedMethodsIDs.clear();
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, getAllFilePaths())) {
			String newFilePath= StringHelper.replacePrefix(filePath, oldPrefix, newPrefix);
			Set<Long> fileChangedMethodsIDs= changedMethodsIDs.remove(filePath);
			changedMethodsIDs.put(newFilePath, fileChangedMethodsIDs);
			Set<Long> fileRefactoredMethodsIDs= refactoredMethodsIDs.remove(filePath);
			refactoredMethodsIDs.put(newFilePath, fileRefactoredMethodsIDs);
			Set<Long> fileCurrentRefactoredMethodsIDs= currentRefactoredMethodsIDs.remove(filePath);
			currentRefactoredMethodsIDs.put(newFilePath, fileCurrentRefactoredMethodsIDs);
			Set<Long> fileRefactoredAndChangedMethodsIDs= refactoredAndChangedMethodsIDs.remove(filePath);
			refactoredAndChangedMethodsIDs.put(newFilePath, fileRefactoredAndChangedMethodsIDs);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		changedMethodsIDs.clear();
		refactoredMethodsIDs.clear();
		refactoredAndChangedMethodsIDs.clear();
		currentASTFilePath= null;
		isInsideRefactoring= false;
		totalRefactoredMethodsCount= 0;
		totalRefactoredAndChangedMethodsCount= 0;
	}

	private Set<String> getAllFilePaths() {
		Set<String> allFilePaths= new HashSet<String>();
		allFilePaths.addAll(changedMethodsIDs.keySet());
		allFilePaths.addAll(refactoredMethodsIDs.keySet());
		allFilePaths.addAll(currentRefactoredMethodsIDs.keySet());
		return allFilePaths;
	}

	private Set<Long> getFileChangedMethodsIDs(String filePath) {
		return getFileIDs(changedMethodsIDs, filePath);
	}

	private Set<Long> getFileRefactoredMethodsIDs(String filePath) {
		return getFileIDs(refactoredMethodsIDs, filePath);
	}

	private Set<Long> getFileCurrentRefactoredMethodsIDs(String filePath) {
		return getFileIDs(currentRefactoredMethodsIDs, filePath);
	}

	private Set<Long> getFileRefactoredAndChangedMethodsIDs(String filePath) {
		return getFileIDs(refactoredAndChangedMethodsIDs, filePath);
	}

	private Set<Long> getFileIDs(Map<String, Set<Long>> idMap, String filePath) {
		Set<Long> fileIDs= idMap.get(filePath);
		if (fileIDs == null) {
			fileIDs= new HashSet<Long>();
			idMap.put(filePath, fileIDs);
		}
		return fileIDs;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactorings_changes_mix_method";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
