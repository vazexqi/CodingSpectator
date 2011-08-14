/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.tests.CodingTrackerTest;

/**
 * This is a base class for all CodingTracker postprocessors.
 * 
 * This class is implemented as a plugin test to ensure the proper functionality of the text
 * recorder (which requires loading of particular plugins).
 * 
 * @author Stas Negara
 * 
 */
public abstract class CodingTrackerPostprocessor extends CodingTrackerTest {

	private final String updatedFilePath= "C:/Users/Stas/Desktop/old format update test/codechanges_manual.txt";

	//@Ignore
	@Test
	public void execute() {
		checkPostprocessingPreconditions();
		UserOperation.isPostprocessing= true;
		String originalSequence= ResourceHelper.readFileContent(new File(updatedFilePath));
		postprocess(OperationDeserializer.getUserOperations(originalSequence));
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

	protected abstract void checkPostprocessingPreconditions();

	protected abstract void postprocess(List<UserOperation> userOperations);

}
