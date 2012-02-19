/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SerializerDeserializerTest extends CodingTrackerTest {

	private static final File operationsRecordFile= new File("test-files/01/codechanges.txt");

	@Test
	public void shouldDeserializeAndSerialize() {
		String operationsRecord= ResourceHelper.readFileContent(operationsRecordFile);
		for (UserOperation userOperation : OperationDeserializer.getUserOperations(operationsRecord)) {
			TextRecorder.record(userOperation);
		}
		String generatedOperationsRecord= ResourceHelper.readFileContent(mainRecordFile);
		assertEquals(operationsRecord, generatedOperationsRecord);
	}

}
