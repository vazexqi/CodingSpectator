/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.util.List;

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

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
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

}
