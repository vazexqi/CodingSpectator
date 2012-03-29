/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.resources.ExternallyModifiedResourceOperation;
import edu.illinois.codingtracker.operations.resources.UpdatedResourceOperation;
import edu.illinois.codingtracker.operations.textchanges.ConflictEditorTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ASTInferenceTextRecorder;
import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;


/**
 * This is the base class for all AST-related postprocessors.
 * 
 * @author Stas Negara
 * 
 */
public abstract class ASTPostprocessor extends CodingTrackerPostprocessor {

	private boolean isUpdatedResource= false;


	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(astMainRecordFile);
	}

	protected void record(UserOperation userOperation) {
		record(userOperation, false);
	}

	protected void record(UserOperation userOperation, boolean isSimulatedRecord) {
		ASTInferenceTextRecorder.record(userOperation, isSimulatedRecord);
	}

	protected void replay(UserOperation userOperation) {
		System.out.println("Replaying operation: " + userOperation.generateSerializationText());
		try {
			userOperation.replay();
		} catch (Exception e) {
			throw new RuntimeException("Could not replay user operation: " + userOperation, e);
		}
	}

	protected void replayAndRecord(UserOperation userOperation) {
		replayAndRecord(userOperation, false);
	}

	protected void replayAndRecord(UserOperation userOperation, boolean isSimulatedRecord) {
		//Do not record TextChangeOperations (except those that happen in conflict editors)	since instead of them 
		//we record the corresponding CoherentTextChanges.
		//For all other operations, first record and then replay in order to preserve the right ordering 
		//(i.e. ASTOperation follow operation(s) that caused it) and the right timestamp 
		//(i.e. ASTOperation has the timestamp of the last operation that caused it).
		if (userOperation instanceof TextChangeOperation && !(userOperation instanceof ConflictEditorTextChangeOperation)) {
			replay(userOperation);
			//record(userOperation, isSimulatedRecord);
		} else {
			record(userOperation, isSimulatedRecord);
			replay(userOperation);
		}
	}

	protected boolean shouldProcess(UserOperation userOperation) {
		if (userOperation instanceof UpdatedResourceOperation || userOperation instanceof ExternallyModifiedResourceOperation) {
			isUpdatedResource= true;
		} else if (!(userOperation instanceof ASTOperation) && !(userOperation instanceof ASTFileOperation)) {
			isUpdatedResource= false;
		}
		return userOperation instanceof ASTOperation && !isUpdatedResource; //Ignore AST operations for updated resources.
	}

}
