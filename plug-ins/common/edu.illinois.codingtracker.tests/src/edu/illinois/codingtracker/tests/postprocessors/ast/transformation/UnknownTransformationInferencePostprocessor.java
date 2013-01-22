/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.ASTPostprocessor;


/**
 * This class infers unknown transformations and inserts them in the sequence with AST operations
 * and known transformations (including refactorings).
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationInferencePostprocessor extends ASTPostprocessor {

	private static final String UNKNOWN_PATTERNS_FILE_NAME= "unknownPatterns.txt";

	private File unknownPatternsFile= new File(Configuration.postprocessorRootFolderName, UNKNOWN_PATTERNS_FILE_NAME);

	private boolean isFirstSequence= true;

	private long lastSnapshotTimestamp= -1;

	private boolean isInsideAutomatedRefactoring;


	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations.with_move.with_inferred_refactorings";
	}

	@Override
	protected String getResultFilePostfix() {
		return ".with_inferred_unknown_transformations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize(userOperations);
		//Create a copy for iterating to avoid concurrent modification errors that appear when the unknown transformation
		//factory adds inferred transformations to the list.
		List<UserOperation> copyUserOperations= new LinkedList<UserOperation>();
		copyUserOperations.addAll(userOperations);
		for (UserOperation userOperation : copyUserOperations) {
			if (userOperation instanceof NewStartedRefactoringOperation) {
				isInsideAutomatedRefactoring= true;
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				isInsideAutomatedRefactoring= false;
			}
			postprocessUserOperation(userOperation);
		}
		//Process the remaining cache and record the resulting sequence.
		InferredUnknownTransformationFactory.processCachedOperations();
		for (UserOperation userOperation : userOperations) {
			record(userOperation);
		}
		persistUnknownTransformationDescriptors();
	}

	private void persistUnknownTransformationDescriptors() {
		List<UserOperation> operations= InferredUnknownTransformationFactory.getInferredUnknownTransformationRepresentatives();
		if (!operations.isEmpty()) {
			try {
				ResourceHelper.ensureFileExists(unknownPatternsFile);
			} catch (IOException e) {
				throw new RuntimeException("Could not create a file for the unknown transformation representatives!", e);
			}
			StringBuffer sb= new StringBuffer();
			for (UserOperation operation : operations) {
				sb.append(operation.generateSerializationText());
			}
			writeToFile(unknownPatternsFile, sb, false, "Could not write the unknown transformation representatives");
		}
	}

	private void postprocessUserOperation(UserOperation userOperation) {
		if (!(userOperation instanceof ASTOperation)) {
			InferredUnknownTransformationFactory.processCachedOperations();
		}
		if (shouldReplay(userOperation)) {
			replayAndRecord(userOperation, true);
			if (!isInsideAutomatedRefactoring && shouldProcess(userOperation)) {
				InferredUnknownTransformationFactory.handleASTOperation((ASTOperation)userOperation);
			}
		} else {
			record(userOperation, true);
		}
	}

	private void initialize(List<UserOperation> userOperations) {
		InferredUnknownTransformationFactory.setUserOperations(userOperations);
		isInsideAutomatedRefactoring= false;
		if (isFirstSequence) {
			if (unknownPatternsFile.exists()) {
				String inputSequence= ResourceHelper.readFileContent(unknownPatternsFile);
				List<UserOperation> inferredUnknownTransformations= OperationDeserializer.getUserOperations(inputSequence);
				InferredUnknownTransformationFactory.setTransformationDescriptors(inferredUnknownTransformations);
			}
			isFirstSequence= false;
		}
	}

	private boolean shouldReplay(UserOperation userOperation) {
		if (userOperation instanceof SnapshotedFileOperation) {
			lastSnapshotTimestamp= userOperation.getTime();
		}
		return userOperation.getTime() != lastSnapshotTimestamp - 1;
	}

}
