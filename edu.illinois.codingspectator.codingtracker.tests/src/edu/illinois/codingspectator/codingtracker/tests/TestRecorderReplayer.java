/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class TestRecorderReplayer extends TestCodingTracker {

	private static final File operationsRecordFile= new File("test-files/02/codechanges.txt");

	@Test
	public void shouldReplayAndRecord() {
		List<UserOperation> predefinedUserOperations= loadUserOperationsFromFile(operationsRecordFile);
		replayUserOperations(predefinedUserOperations);
		List<UserOperation> generatedUserOperations= loadUserOperationsFromFile(mainRecordFile);
		checkEquivalencyOfUserOperations(predefinedUserOperations, generatedUserOperations);
		checkFinalCode();
	}

	private void checkEquivalencyOfUserOperations(List<UserOperation> predefinedUserOperations, List<UserOperation> generatedUserOperations) {
		Iterator<UserOperation> generatedUserOperationsIterator= generatedUserOperations.iterator();
		for (UserOperation predefinedUserOperation : predefinedUserOperations) {
			//Skip those operations that are not recorded by the test
			if (!predefinedUserOperation.isTestReplayRecorded()) {
				continue;
			}
			assertTrue(generatedUserOperationsIterator.hasNext());
			UserOperation generatedUserOperation= generatedUserOperationsIterator.next();
			assertTrue(predefinedUserOperation.getClass() == generatedUserOperation.getClass());
			assertEquals(removeTimestamp(predefinedUserOperation), removeTimestamp(generatedUserOperation));
		}
		assertFalse(generatedUserOperationsIterator.hasNext()); //there should be no other generated operations
	}

	private void checkFinalCode() {
		File predefinedTest1= new File("test-files/02/Test1.java");
		File predefinedTest2= new File("test-files/02/Test2.java");
		File generatedTest1= getGeneratedFile("Test1.java");
		File generatedTest2= getGeneratedFile("Test2.java");
		checkFilesAreEqual(predefinedTest1, generatedTest1);
		checkFilesAreEqual(predefinedTest2, generatedTest2);
	}

	private File getGeneratedFile(String fileName) {
		String workspaceRelativeFilePath= "/edu.illinois.testproject/src/edu/illinois/test/" + fileName;
		return ResourcesPlugin.getWorkspace().getRoot().findMember(workspaceRelativeFilePath).getLocation().toFile();
	}

	private void checkFilesAreEqual(File file1, File file2) {
		assertEquals(FileHelper.getFileContent(file1), FileHelper.getFileContent(file2));
	}

	private void replayUserOperations(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			try {
				userOperation.replay();
			} catch (Exception e) {
				throw new RuntimeException("Could not replay operation: " + userOperation, e);
			}
		}
	}

	private List<UserOperation> loadUserOperationsFromFile(File recordFile) {
		String operationsRecord= FileHelper.getFileContent(recordFile);
		return OperationDeserializer.getUserOperations(operationsRecord);
	}

	private String removeTimestamp(UserOperation userOperation) {
		String userOperationString= userOperation.toString();
		int timestampIndex= userOperationString.lastIndexOf("Timestamp: ");
		return userOperationString.substring(0, timestampIndex);
	}

}
