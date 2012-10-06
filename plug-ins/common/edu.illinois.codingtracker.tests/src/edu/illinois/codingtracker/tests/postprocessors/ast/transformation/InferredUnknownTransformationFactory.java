/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.tests.postprocessors.ast.helpers.InferenceHelper;



/**
 * This class handles instances of the inferred unknown transformations.
 * 
 * @author Stas Negara
 * 
 */
public class InferredUnknownTransformationFactory {

	private static final boolean shouldSubsumeOperationsOnChildren= false;

	private static List<UserOperation> userOperations;

	private static long transformationKindID= 1;

	private static long transformationID= 1;

	private static final List<ASTOperation> operationsCache= new LinkedList<ASTOperation>();

	private static final Map<UnknownTransformationDescriptor, Long> transformationDescriptorIDs= new HashMap<UnknownTransformationDescriptor, Long>();


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

	public static void handleASTOperation(ASTOperation newOperation) {
		if (!newOperation.isChange()) { //So far, only add and delete operations can contribute to patterns.
			if (shouldSubsumeOperationsOnChildren) {
				ASTNode newAffectedNode= InferenceHelper.getAffectedNode(newOperation);
				Iterator<ASTOperation> operationsCacheIterator= operationsCache.iterator();
				while (operationsCacheIterator.hasNext()) {
					ASTOperation operation= operationsCacheIterator.next();
					if (newOperation.getOperationKind() == operation.getOperationKind()) {
						ASTNode affectedNode= InferenceHelper.getAffectedNode(operation);
						if (ASTHelper.isChild(newAffectedNode, affectedNode)) {
							return; //The new operation is subsumed by the existing operations.
						}
						if (ASTHelper.isChild(affectedNode, newAffectedNode)) {
							operationsCacheIterator.remove(); //The existing operation is subsumed by the new operation.
						}
					}
				}
			}
			operationsCache.add(newOperation);
		}
	}

	public static void processCachedOperations() {
		for (ASTOperation operation : operationsCache) {
			ASTNode affectedNode= InferenceHelper.getAffectedNode(operation);
			if (ASTHelper.getAllChildren(affectedNode).size() > 1) { //Note that children include the affected node as well.
				//So far, only structurally non-trivial nodes contribute to patterns.
				UnknownTransformationDescriptor transformationDescriptor=
						UnknownTransformationDescriptorFactory.createDescriptor(operation.getOperationKind(), affectedNode);
				long transformationKindID= getTransformationKindID(transformationDescriptor);
				InferredUnknownTransformationOperation transformationOperation=
						new InferredUnknownTransformationOperation(transformationKindID, transformationID, transformationDescriptor, operation.getTime());
				operation.setTransformationID(transformationID);
				int insertIndex= userOperations.indexOf(operation) + 1;
				userOperations.add(insertIndex, transformationOperation);
				transformationID++;
			}
		}
		operationsCache.clear();
	}

	private static long getTransformationKindID(UnknownTransformationDescriptor transformationDescriptor) {
		Long transformationKindID= transformationDescriptorIDs.get(transformationDescriptor);
		if (transformationKindID == null) {
			transformationKindID= InferredUnknownTransformationFactory.transformationKindID;
			transformationDescriptorIDs.put(transformationDescriptor, transformationKindID);
			InferredUnknownTransformationFactory.transformationKindID++;
		}
		return transformationKindID;
	}

}
