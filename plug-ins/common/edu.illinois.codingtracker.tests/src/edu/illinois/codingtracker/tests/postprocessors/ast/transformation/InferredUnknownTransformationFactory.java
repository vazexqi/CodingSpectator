/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;



/**
 * This class handles instances of the inferred unknown transformations.
 * 
 * @author Stas Negara
 * 
 */
public class InferredUnknownTransformationFactory {

	private static List<UserOperation> userOperations;

	private static long transformationKindID= 1;

	private static long transformationID= 1;


	/**
	 * Should be called before processing a sequence.
	 */
	public static void resetCurrentState(List<UserOperation> userOperations) {
		InferredUnknownTransformationFactory.userOperations= userOperations;
		transformationID= 1;
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTOperation) {
				long operationTransformationID= ((ASTOperation)userOperation).getTransformationID();
				if (operationTransformationID >= transformationID) {
					transformationID= operationTransformationID + 1;
				}
			}
		}
	}

	public static void handleASTOperation(ASTOperation operation) {
		UnknownTransformationPattern transformationPattern= UnknownTransformationPatternsFactory.retrieveTransformationPattern(operation);
		if (transformationPattern != null) {
			InferredUnknownTransformationOperation transformationOperation= new InferredUnknownTransformationOperation(transformationKindID, transformationID,
					transformationPattern.getTransformationDescriptor(), operation.getTime());
			operation.setTransformationID(transformationID);
			int insertIndex= userOperations.indexOf(operation) + 1;
			userOperations.add(insertIndex, transformationOperation);
			transformationKindID++;
			transformationID++;
		}
	}

}
