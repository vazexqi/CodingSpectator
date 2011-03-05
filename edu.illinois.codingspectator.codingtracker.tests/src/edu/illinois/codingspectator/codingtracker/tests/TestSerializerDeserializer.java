/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;
import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public class TestSerializerDeserializer extends TestCodingTracker {

	private static final File operationsRecordFile= new File("test-files/01/codechanges.txt");

	@Test
	public void shouldDeserializeAndSerialize() {
		String operationsRecord= FileHelper.getFileContent(operationsRecordFile);
		for (UserOperation userOperation : OperationDeserializer.getUserOperations(operationsRecord)) {
			TextRecorder.record(userOperation);
		}
		String generatedOperationsRecord= FileHelper.getFileContent(mainRecordFile);
		assertEquals(operationsRecord, generatedOperationsRecord);
	}

}
