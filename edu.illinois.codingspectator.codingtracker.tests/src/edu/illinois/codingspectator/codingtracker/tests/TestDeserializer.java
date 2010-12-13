/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;
import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * Should be executed without monitor.ui (such that the recording of the user actions does not
 * interfere with the test).
 * 
 * @author Stas Negara
 * 
 */
public class TestDeserializer {

	private static final String operationsRecordPath= "test-files/01/codechanges.txt";

	private static final TextRecorder textRecorder= TextRecorder.getInstance();

	@Test
	public void shouldDeserializeAndSerialize() {
		String operationsRecord= FileHelper.getFileContent(new File(operationsRecordPath));
		List<UserOperation> userOperations= OperationDeserializer.getUserOperations(operationsRecord);
		for (UserOperation userOperation : userOperations) {
			textRecorder.record(userOperation);
		}
		String generatedOperationsRecord= FileHelper.getFileContent(new File(TextRecorder.MAIN_RECORD_FILE_PATH.toOSString()));
		assertEquals(operationsRecord, generatedOperationsRecord);
	}

}
