/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.util.List;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.operations.refactorings.PerformedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.RefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.UndoneRefactoringOperation;
import edu.illinois.codingtracker.operations.starts.StartedRefactoringOperation;
import edu.illinois.codingtracker.recording.TextRecorder;

/**
 * This class upgrades an operation sequence from the old format to the new one by deserializing it
 * in OLD_CODINGTRACKER_FORMAT mode and then serializing it in the new format. Also, it upgrades
 * refactoring operations to the new API.
 * 
 * Note that to use this functionality properly, the environment variable OLD_CODINGTRACKER_FORMAT
 * has to be set.
 * 
 * @author Stas Negara
 * 
 */
public class UpdateOperationSequenceFormatPostprocessor extends CodingTrackerPostprocessor {

	private final static String FIRST_VERSION_WITH_NEW_FORMAT= "1.0.0.201105172309";


	@Override
	protected void checkPostprocessingPreconditions() {
		if (!Configuration.isOldFormat) {
			throw new RuntimeException("Set environment variable OLD_CODINGTRACKER_FORMAT to perform the format update correctly");
		}
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return folderName.startsWith(VERSION_FOLDER_COMMON_PREFIX) && folderName.compareTo(FIRST_VERSION_WITH_NEW_FORMAT) < 0;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges_manual.txt";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		updateRefactoringOperations(userOperations);
		for (UserOperation userOperation : userOperations) {
			TextRecorder.record(userOperation);
		}
	}

	private void updateRefactoringOperations(List<UserOperation> userOperations) {
		int lastStartedRefactoringIndex= -1;
		long lastStartedRefactoringTimestamp= -1;
		for (int i= 0; i < userOperations.size(); i++) {
			UserOperation userOperation= userOperations.get(i);
			if (userOperation instanceof StartedRefactoringOperation) {
				lastStartedRefactoringIndex= i;
				lastStartedRefactoringTimestamp= userOperation.getTime();
				continue;
			}
			if (userOperation instanceof RefactoringOperation) {
				RefactoringOperation refactoringOperation= (RefactoringOperation)userOperation;
				if (lastStartedRefactoringIndex == -1) {
					throw new RuntimeException("Found RefactoringOperation without preceding StartedRefactoringOperation: " + refactoringOperation);
				}
				long newStartedRefactoringTimestamp= lastStartedRefactoringTimestamp;
				long finishedRefactoringTimestamp= refactoringOperation.getTime();
				if (newStartedRefactoringTimestamp > finishedRefactoringTimestamp) {
					//This could happen when a refactoring is undone or redone. To keep consistent with the timestamps of the 
					//new refactoring operations for undo or redo, adjust timestamps accordingly.
					newStartedRefactoringTimestamp= finishedRefactoringTimestamp; //this is timestamp of the originally performed refactoring
					finishedRefactoringTimestamp= userOperations.get(i - 1).getTime();
				}
				NewStartedRefactoringOperation newStartedRefactoringOperation= new NewStartedRefactoringOperation(true, getRefactoringMode(refactoringOperation), refactoringOperation.getID(),
						refactoringOperation.getProject(), refactoringOperation.getFlags(), refactoringOperation.getArguments(), newStartedRefactoringTimestamp);
				FinishedRefactoringOperation finishedRefactoringOperation= new FinishedRefactoringOperation(true, finishedRefactoringTimestamp);
				userOperations.set(lastStartedRefactoringIndex, newStartedRefactoringOperation);
				userOperations.set(i, finishedRefactoringOperation);
				lastStartedRefactoringIndex= -1;
			}
		}
	}

	private RefactoringMode getRefactoringMode(RefactoringOperation refactoringOperation) {
		if (refactoringOperation instanceof PerformedRefactoringOperation) {
			return RefactoringMode.PERFORM;
		}
		if (refactoringOperation instanceof UndoneRefactoringOperation) {
			return RefactoringMode.UNDO;
		}
		if (refactoringOperation instanceof RedoneRefactoringOperation) {
			return RefactoringMode.REDO;
		}
		throw new RuntimeException("Can not establish refactoring mode for refactoring operation: " + refactoringOperation);
	}

	@Override
	protected String getResultFilePostfix() {
		return ".fixed_old_format";
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(mainRecordFile);
	}

}
