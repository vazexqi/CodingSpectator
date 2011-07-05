/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class RecorderReplayerTest extends CodingTrackerTest {

	private static final String TEST_FILES_FOLDER= "test-files";

	private static final String CODECHANGES_FILE_NAME= "codechanges.txt";

	protected abstract String getTestNumber();

	protected abstract String[] getTestFileNames();

	protected abstract String[] getGeneratedFilePaths();

	@Test
	public void shouldReplayAndRecord() {
		List<UserOperation> predefinedUserOperations= loadTestUserOperations();
		replayUserOperations(predefinedUserOperations);
		List<UserOperation> generatedUserOperations= loadGeneratedUserOperations();
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
			assertEquals(removeVolatileParts(predefinedUserOperation), removeVolatileParts(generatedUserOperation));
		}
		assertFalse(generatedUserOperationsIterator.hasNext()); //there should be no other generated operations
	}

	private void checkFinalCode() {
		String[] testFileNames= getTestFileNames();
		String[] generatedFilePaths= getGeneratedFilePaths();
		for (int i= 0; i < testFileNames.length; i++) {
			File predefinedFile= getTestFile(testFileNames[i]);
			File generatedFile= getGeneratedFile(generatedFilePaths[i]);
			checkFilesAreEqual(predefinedFile, generatedFile);
		}
	}

	private void checkFilesAreEqual(File file1, File file2) {
		assertEquals(ResourceHelper.readFileContent(file1), ResourceHelper.readFileContent(file2));
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

	private List<UserOperation> loadTestUserOperations() {
		return loadUserOperationsFromFile(getTestFile(CODECHANGES_FILE_NAME));
	}

	private List<UserOperation> loadGeneratedUserOperations() {
		return loadUserOperationsFromFile(mainRecordFile);
	}

	private List<UserOperation> loadUserOperationsFromFile(File recordFile) {
		String operationsRecord= ResourceHelper.readFileContent(recordFile);
		return OperationDeserializer.getUserOperations(operationsRecord);
	}

	private File getTestFile(String fileName) {
		String testFileName= TEST_FILES_FOLDER + "/" + getTestNumber() + "/" + fileName;
		return new File(testFileName);
	}

	private File getGeneratedFile(String workspaceRelativeFilePath) {
		return ResourceHelper.getFileForResource(ResourceHelper.findWorkspaceMember(workspaceRelativeFilePath));
	}

	private String removeVolatileParts(UserOperation userOperation) {
		String userOperationString= userOperation.toString();
		int timestampIndex= userOperationString.lastIndexOf("Timestamp: ");
		String result= userOperationString.substring(0, timestampIndex);
		int editorIDIndex= result.indexOf("Editor ID: ");
		if (editorIDIndex != -1) {
			int newLineIndex= result.indexOf("\n", editorIDIndex);
			result= result.substring(0, editorIDIndex) + result.substring(newLineIndex);
		}
		return result;
	}

}
