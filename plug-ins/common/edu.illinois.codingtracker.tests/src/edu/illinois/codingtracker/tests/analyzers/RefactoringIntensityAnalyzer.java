/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;


/**
 * This class calculates the number of affected files and lines of code for each performed, undone,
 * or redone refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringIntensityAnalyzer extends CSVProducingAnalyzer {

	private long refactoringTimestamp;

	private String refactoringID;

	private int affectedFilesCount;

	private int affectedLinesCount;

	private final Set<String> countedFiles= new HashSet<String>();

	private final Map<String, Set<Integer>> countedLineNumbers= new HashMap<String, Set<Integer>>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,timestamp,refactoring ID,number of affected files,number of affected lines\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		//Although the implementation of RefactoringIntensityAnalyzer accepts CodingTracker's sequences of any version, 
		//the analysis of any sequence from the version 1.0.0.201104162211 will produce a correct result only for 
		//the refactorings that edit a single file, while sequences from the versions that precede 1.0.0.201104162211 
		//would not give any good analysis results at all. 
		return true;
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof NewStartedRefactoringOperation) {
				refactoringTimestamp= userOperation.getTime();
				refactoringID= ((NewStartedRefactoringOperation)userOperation).getID();
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				appendAndResetCurrentState();
			} else if (refactoringTimestamp != -1) { //Inside a refactoring.
				if (userOperation instanceof TextChangeOperation) {
					handleTextChangeOperation((TextChangeOperation)userOperation);
				} else if (userOperation instanceof MovedResourceOperation) {
					handleMovedResourceOperation((MovedResourceOperation)userOperation);
				}
				//TODO: Should we count deleted files as affected files and all their line numbers as affected line numbers?
			}
			try {
				userOperation.replay();
			} catch (Exception e) {
				throw new RuntimeException("Could not replay user operation: " + userOperation, e);
			}
		}
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String movedResourcePath= movedResourceOperation.getResourcePath();
		IResource movedResource= ResourceHelper.findWorkspaceMember(movedResourcePath);
		if (movedResource instanceof IFile) {
			addAffectedFile(movedResourcePath);
			//The destination file is the same as the source file, so it should not be counted separately.
			String destinationResourcePath= movedResourceOperation.getDestinationPath();
			countedFiles.add(destinationResourcePath);
			remapLineNumbersForFiles(movedResourcePath, destinationResourcePath);
		}
	}

	private void handleTextChangeOperation(TextChangeOperation textChangeOperation) {
		String editedFilePath= textChangeOperation.getEditedFilePath();
		addAffectedFile(editedFilePath);
		Set<Integer> countedFileLineNumbers= getCountedLineNumbersForFile(editedFilePath);
		int[] affectedLineNumbers= textChangeOperation.getAffectedLineNumbers();
		for (int affectedLineNumber : affectedLineNumbers) {
			//TODO: The line numbers may change during a refactoring, which could cause different line numbers represent
			//the same line or different lines be represented with the same line number. Thus, the current implementation is
			//an approximation.
			if (!countedFileLineNumbers.contains(affectedLineNumber)) {
				affectedLinesCount++;
				countedFileLineNumbers.add(affectedLineNumber);
			}
		}
	}

	private void addAffectedFile(String filePath) {
		if (!countedFiles.contains(filePath)) {
			countedFiles.add(filePath);
			affectedFilesCount++;
		}
	}

	private Set<Integer> getCountedLineNumbersForFile(String filePath) {
		Set<Integer> countedFileLineNumbers= countedLineNumbers.get(filePath);
		if (countedFileLineNumbers == null) {
			countedFileLineNumbers= new HashSet<Integer>();
			countedLineNumbers.put(filePath, countedFileLineNumbers);
		}
		return countedFileLineNumbers;
	}

	private void remapLineNumbersForFiles(String oldFilePath, String newFilePath) {
		Set<Integer> countedFileLineNumbers= countedLineNumbers.remove(oldFilePath);
		if (countedFileLineNumbers != null) {
			countedLineNumbers.put(newFilePath, countedFileLineNumbers);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		resetCurrentState();
	}

	private void resetCurrentState() {
		refactoringTimestamp= -1;
		refactoringID= "";
		affectedFilesCount= 0;
		affectedLinesCount= 0;
		countedFiles.clear();
		countedLineNumbers.clear();
	}

	private void appendAndResetCurrentState() {
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, refactoringTimestamp,
						refactoringID, affectedFilesCount, affectedLinesCount);
		resetCurrentState();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_intensity";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldStopAfterPostprocessingFailed() {
		return false;
	}

}
