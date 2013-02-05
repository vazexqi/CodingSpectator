/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	private static final boolean shouldConsiderChangeOperations= true;

	private static final boolean shouldConsiderStructurallyTrivialNodes= true;

	private static final boolean shouldSubsumeOperationsOnChildren= false;

	private static List<UserOperation> userOperations;

	private static long transformationKindID= 1;

	//Should exceed the largest known transformation ID in *all* processed sequences, therefore it is faster just to start
	//with some really big number.
	private static long transformationID= 1000000; //1 million

	private static final List<ASTOperation> operationsCache= new LinkedList<ASTOperation>();

	private static final Map<UnknownTransformationDescriptor, Long> transformationDescriptorIDs= new HashMap<UnknownTransformationDescriptor, Long>();


	/**
	 * Should be called before processing a sequence.
	 */
	public static void setUserOperations(List<UserOperation> userOperations) {
		InferredUnknownTransformationFactory.userOperations= userOperations;
	}

	/**
	 * Serialization and deserialization of transformation descriptors is done through (fake)
	 * InferredUnknownTransformationOperations.
	 * 
	 * @param inferredUnknownTransformations
	 */
	public static void setTransformationDescriptors(List<UserOperation> inferredUnknownTransformations) {
		transformationDescriptorIDs.clear();
		for (UserOperation userOperation : inferredUnknownTransformations) {
			InferredUnknownTransformationOperation transformation= (InferredUnknownTransformationOperation)userOperation;
			transformationDescriptorIDs.put(transformation.getDescriptor(), transformation.getTransformationKindID());
			transformationID= transformation.getTransformationID();
		}
	}

	public static List<UserOperation> getInferredUnknownTransformationRepresentatives() {
		List<UserOperation> transformations= new LinkedList<UserOperation>();
		for (Entry<UnknownTransformationDescriptor, Long> entry : transformationDescriptorIDs.entrySet()) {
			transformations.add(new InferredUnknownTransformationOperation(entry.getValue(), transformationID, entry.getKey(), 1l));
		}
		return transformations;
	}

	@SuppressWarnings("unused")
	public static void handleASTOperation(ASTOperation newOperation) {
		if (shouldConsiderChangeOperations || !newOperation.isChange()) {
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

	@SuppressWarnings("unused")
	public static void processCachedOperations() {
		for (ASTOperation operation : operationsCache) {
			ASTNode affectedNode= InferenceHelper.getAffectedNode(operation);
			if (affectedNode == null) {
				throw new RuntimeException("Could not retrieve the affected node!");
			}
			if (shouldConsiderStructurallyTrivialNodes || !isStructurallyTrivialNode(affectedNode)) {
				processOperation(operation, affectedNode);
			}
		}
		operationsCache.clear();
	}

	public static void processOperation(ASTOperation operation, ASTNode affectedNode) {
		UnknownTransformationDescriptor transformationDescriptor= affectedNode == null
				? UnknownTransformationDescriptorFactory.createDescriptor(operation.getOperationKind(), operation.getNodeType())
				: UnknownTransformationDescriptorFactory.createDescriptor(operation.getOperationKind(), affectedNode);
		long transformationKindID= getTransformationKindID(transformationDescriptor);
		InferredUnknownTransformationOperation transformationOperation=
				new InferredUnknownTransformationOperation(transformationKindID, transformationID, transformationDescriptor, operation.getTime());
		operation.setTransformationID(transformationID);
		int insertIndex= userOperations.indexOf(operation) + 1;
		userOperations.add(insertIndex, transformationOperation);
		transformationID++;
	}

	private static boolean isStructurallyTrivialNode(ASTNode node) {
		//Note that children include the affected node as well.
		return ASTHelper.getAllChildren(node).size() < 2;
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
