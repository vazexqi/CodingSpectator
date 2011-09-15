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
 * This is a base class for all CodingTracker postprocessors and analyzers.
 * 
 * This class is implemented as a plugin test to ensure the proper functionality of the text
 * recorder (which requires loading of particular plugins).
 * 
 * @author Stas Negara
 * 
 */
public abstract class CodingTrackerPostprocessor extends CodingTrackerTest {

	protected final static String VERSION_FOLDER_COMMON_PREFIX= "1.0.0.201";

	private final boolean shouldOverwriteOutputFiles= true;

	private final String rootFolder= System.getenv("POSTPROCESSOR_ROOT_FOLDER");

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
			String inputSequence= ResourceHelper.readFileContent(file);
			postprocess(OperationDeserializer.getUserOperations(inputSequence));
			try {
				File outputFile= new File(file.getAbsolutePath() + getResultFilePostfix());
				if (outputFile.exists() && !shouldOverwriteOutputFiles) {
					throw new RuntimeException("Output file already exists: " + outputFile.getName());
				}
				ResourceHelper.writeFileContent(outputFile, getResult(), false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			System.out.println("DONE");
			before(); //After a file is postprocessed, reset the main record files.
		}
	}

	private boolean shouldPostprocessFile(File file) {
		String versionFolderName= file.getParentFile().getParentFile().getName();
		return shouldPostprocessVersionFolder(versionFolderName) && isRecordFile(file);
	}

	private boolean isRecordFile(File file) {
		return file.getName().equals(getRecordFileName());
	}

	protected abstract boolean shouldPostprocessVersionFolder(String folderName);

	protected abstract String getRecordFileName();

	protected abstract void checkPostprocessingPreconditions();

	protected abstract void postprocess(List<UserOperation> userOperations);

	protected abstract String getResultFilePostfix();

	protected abstract String getResult();

}
