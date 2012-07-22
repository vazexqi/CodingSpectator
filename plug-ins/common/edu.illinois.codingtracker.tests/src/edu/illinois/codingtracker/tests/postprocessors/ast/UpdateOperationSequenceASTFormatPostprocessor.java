/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast;

import java.util.List;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.recording.TextRecorder;
import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;

/**
 * This class upgrades an operation sequence from the old AST format to the new one by deserializing
 * it in OLD_AST_CODINGTRACKER_FORMAT mode and then serializing it in the new format.
 * 
 * Note that to use this functionality properly, the environment variable
 * OLD_AST_CODINGTRACKER_FORMAT has to be set.
 * 
 * !!!This postprocessor updates the original files and thus, it can be applied just once on a
 * particular set of files.
 * 
 * @author Stas Negara
 * 
 */
public class UpdateOperationSequenceASTFormatPostprocessor extends CodingTrackerPostprocessor {

	@Override
	protected void checkPostprocessingPreconditions() {
		if (!Configuration.isOldASTFormat) {
			throw new RuntimeException("Set environment variable OLD_AST_CODINGTRACKER_FORMAT to perform the AST format update correctly");
		}
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			TextRecorder.record(userOperation);
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ""; //Write back to the source file.
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(mainRecordFile);
	}

}
