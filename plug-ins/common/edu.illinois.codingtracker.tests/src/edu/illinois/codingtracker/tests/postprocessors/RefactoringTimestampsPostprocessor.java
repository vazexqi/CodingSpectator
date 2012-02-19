/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.util.List;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.recording.TextRecorder;


/**
 * This class fixes negative timestamps of NewStartedRefactoringOperation.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringTimestampsPostprocessor extends CodingTrackerPostprocessor {

	private final static String BUGGY_VERSION= "1.0.0.201105242245";


	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return folderName.equals(BUGGY_VERSION);
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		for (int i= 0; i < userOperations.size(); i++) {
			UserOperation userOperation= userOperations.get(i);
			if (userOperation instanceof NewStartedRefactoringOperation && userOperation.getTime() == -1) {
				NewStartedRefactoringOperation originalOperation= (NewStartedRefactoringOperation)userOperation;
				long newTimestamp= userOperations.get(i + 1).getTime();
				NewStartedRefactoringOperation replacementOperation= new NewStartedRefactoringOperation(originalOperation.getShouldAlwaysReplay(), originalOperation.getRefactoringMode(),
						originalOperation.getID(), originalOperation.getProject(), originalOperation.getFlags(), originalOperation.getArguments(), newTimestamp);
				TextRecorder.record(replacementOperation);
			} else {
				TextRecorder.record(userOperation);
			}
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".fixed_refactoring_timestamps";
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(mainRecordFile);
	}

}
