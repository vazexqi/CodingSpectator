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

	protected final static String VERSION_FOLDER_COMMON_PREFIX= "1.0.0.201";

	protected final static String FIRST_VERSION_WITH_NEW_FORMAT= "1.0.0.201105242245";

	private final boolean shouldOverwriteOutputFiles= true;

	private final String rootFolder= "C:/Users/Stas/Desktop/Old CodingTracker format data";

	//@Ignore
	@Test
	public void execute() {
		checkPostprocessingPreconditions();
		UserOperation.isPostprocessing= true;
		visitLocation(new File(rootFolder));
	}

	private void visitLocation(File file) {
		if (file.isDirectory()) {
			for (File childFile : file.listFiles()) {
				visitLocation(childFile);
			}
		} else if (shouldPostprocessFile(file)) {
			System.out.println("Postprocessing file: " + file.getAbsolutePath());
			String originalSequence= ResourceHelper.readFileContent(file);
			postprocess(OperationDeserializer.getUserOperations(originalSequence));
			String updatedSequence= ResourceHelper.readFileContent(mainRecordFile);
			try {
				File outputFile= new File(file.getAbsolutePath() + ".postprocessed");
				if (outputFile.exists() && !shouldOverwriteOutputFiles) {
					throw new RuntimeException("Output file already exists: " + outputFile.getName());
				}
				ResourceHelper.writeFileContent(outputFile, updatedSequence, false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			System.out.println("DONE");
			before(); //After a file is postprocessed, reset the main record file.
		}
	}

	private boolean shouldPostprocessFile(File file) {
		String versionFolderName= file.getParentFile().getParentFile().getName();
		if (shouldPostprocessVersionFolder(versionFolderName) && isRecordFile(file)) {
			return true;
		}
		return false;
	}

	private boolean isRecordFile(File file) {
		return file.getName().equals(getRecordFileName());
	}

	protected abstract boolean shouldPostprocessVersionFolder(String folderName);

	protected abstract String getRecordFileName();

	protected abstract void checkPostprocessingPreconditions();

	protected abstract void postprocess(List<UserOperation> userOperations);

}
