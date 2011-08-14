/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
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
 * in OLD_CODINGTRACKER_FORMAT mode and then serializing it in the new format. Note that to use this
 * functionality properly, the environment variable OLD_CODINGTRACKER_FORMAT has to be set.
 * 
 * This class is implemented as a plugin test to ensure the proper functionality of the text
 * recorder (which requires loading of particular plugins).
 * 
 * @author Stas Negara
 * 
 */
public class UpdateOperationSequenceFormat extends CodingTrackerTest {

	private final String updatedFilePath= "C:/Users/Stas/Desktop/old format update test/codechanges_manual.txt";

	//@Ignore
	@Test
	public void update() {
		if (!UserOperation.isOldFormat) {
			throw new RuntimeException("Set environment variable OLD_CODINGTRACKER_FORMAT to perform the format update correctly");
		}
		String originalSequence= ResourceHelper.readFileContent(new File(updatedFilePath));
		List<UserOperation> userOperations= OperationDeserializer.getUserOperations(originalSequence);
		updateRefactoringOperations(userOperations);
		for (UserOperation userOperation : userOperations) {
			TextRecorder.record(userOperation);
		}
		String updatedSequence= ResourceHelper.readFileContent(mainRecordFile);
		try {
			File outputFile= new File(updatedFilePath + ".updated");
			if (outputFile.exists()) {
				throw new RuntimeException("Output file already exists: " + outputFile.getName());
			}
			ResourceHelper.writeFileContent(outputFile, updatedSequence, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
				NewStartedRefactoringOperation newStartedRefactoringOperation= new NewStartedRefactoringOperation(getRefactoringMode(refactoringOperation), refactoringOperation.getID(),
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
}
