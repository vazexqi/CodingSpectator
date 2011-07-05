/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
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

	private final String updatedFilePath= "C:/Documents and Settings/Stanislav/CodingSpectator/edu.illinois.codingtracker.tests/test-files/11/codechanges.txt";

	@Ignore
	@Test
	public void update() {
		if (!UserOperation.isOldFormat) {
			throw new RuntimeException("Set environment variable OLD_CODINGTRACKER_FORMAT to perform the format update correctly");
		}
		String originalSequence= ResourceHelper.readFileContent(new File(updatedFilePath));
		for (UserOperation userOperation : OperationDeserializer.getUserOperations(originalSequence)) {
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

}
