/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperationInferencer {

	private int batchSize;

	private int offset;

	private int removedTextLength;

	private int addedTextLength;

	private ASTNode newRootNode;

	private ASTNode newCoveringNode;

	private List<ASTNode> newCoveredNodes;

	private ASTNode newCommonCoveringNode;

	private ASTNode oldCommonCoveringNode;

	private Map<ASTNode, ASTNode> matchedNodes= new HashMap<ASTNode, ASTNode>();

	private Map<ASTNode, ASTNode> changedNodes= new HashMap<ASTNode, ASTNode>(); //Is a subset of matchedNodes.

	private Set<ASTNode> deletedNodes= new HashSet<ASTNode>();

	private Set<ASTNode> addedNodes= new HashSet<ASTNode>();


	public ASTOperationInferencer(int batchSize, CoherentTextChange coherentTextChange) {
		this.batchSize= batchSize;
		String oldText= coherentTextChange.getInitialDocumentText();
		String newText= coherentTextChange.getFinalDocumentText();
		boolean isMultiBatch= batchSize > 1;
		this.offset= isMultiBatch ? 0 : coherentTextChange.getOffset();
		this.removedTextLength= isMultiBatch ? oldText.length() : coherentTextChange.getRemovedTextLength();
		this.addedTextLength= isMultiBatch ? newText.length() : coherentTextChange.getAddedTextLength();
		initializeInferencer(oldText, newText);
	}

	public Map<ASTNode, ASTNode> getMatchedNodes() {
		return matchedNodes;
	}

	public Map<ASTNode, ASTNode> getChangedNodes() {
		return changedNodes;
	}

	public Set<ASTNode> getDeletedNodes() {
		return deletedNodes;
	}

	public Set<ASTNode> getAddedNodes() {
		return addedNodes;
	}

	private void initializeInferencer(String oldText, String newText) {
		AffectedNodesFinder oldAffectedNodesFinder= ASTHelper.getAffectedNodesFinder(oldText, offset, removedTextLength);
		ASTNode oldRootNode= oldAffectedNodesFinder.getRootNode();
		ASTNode oldCoveringNode= oldAffectedNodesFinder.getCoveringNode();

		AffectedNodesFinder newAffectedNodesFinder= ASTHelper.getAffectedNodesFinder(newText, offset, addedTextLength);
		newRootNode= newAffectedNodesFinder.getRootNode();
		newCoveringNode= newAffectedNodesFinder.getCoveringNode();
		newCoveredNodes= newAffectedNodesFinder.getCoveredNodes();

		String initialCommonCoveringNodeID= ASTNodesIdentifier.getCommonPositonalNodeID(oldCoveringNode, newCoveringNode);
		oldCommonCoveringNode= ASTNodesIdentifier.getASTNodeFromPositonalID(oldRootNode, initialCommonCoveringNodeID);
		newCommonCoveringNode= ASTNodesIdentifier.getASTNodeFromPositonalID(newRootNode, initialCommonCoveringNodeID);
		while (areUnmatchingCoveringNodes(oldCommonCoveringNode, newCommonCoveringNode)) {
			oldCommonCoveringNode= oldCommonCoveringNode.getParent();
			newCommonCoveringNode= newCommonCoveringNode.getParent();
		}
	}

	private boolean areUnmatchingCoveringNodes(ASTNode oldCoveringNode, ASTNode newCoveringNode) {
		return oldCoveringNode.getStartPosition() != newCoveringNode.getStartPosition() ||
				oldCoveringNode.getLength() - removedTextLength + addedTextLength != newCoveringNode.getLength() ||
				oldCoveringNode.getNodeType() != newCoveringNode.getNodeType();
	}

	//TODO: Consider that the old AST could be problematic as well.
	public boolean isProblematicInference() {
		if (ASTHelper.isRecoveredOrMalformed(newCoveringNode) || ASTHelper.isRecoveredOrMalformed(newCommonCoveringNode)) {
			return true;
		}
		MethodDeclaration coveringMethodDeclaration= ASTHelper.getContainingMethod(newCoveringNode);
		if (coveringMethodDeclaration != null && ASTHelper.isRecoveredOrMalformed(coveringMethodDeclaration)) {
			return true;
		}
		for (ASTNode coveredNode : newCoveredNodes) {
			if (ASTHelper.isRecoveredOrMalformed(coveredNode)) {
				return true;
			}
		}
		return false;
	}

	public void inferASTOperations() {
		matchedNodes.put(oldCommonCoveringNode, newCommonCoveringNode);
		Set<ASTNode> oldChildren= ASTHelper.getAllChildren(oldCommonCoveringNode);
		Set<ASTNode> newChildren= ASTHelper.getAllChildren(newCommonCoveringNode);

		//First, match nodes that are completely before or completely after the changed range.
		matchNodesOutsideOfChangedRange(oldChildren, newChildren);

		//Next, match yet unmatched nodes with the same structural positions and types.
		matchNodesStructurally(oldChildren);

		//Finally, collect the remaining unmatched nodes as deleted and added nodes. Also, collect the changed matched nodes.
		collectDeletedNodes(oldChildren);
		collectAddedNodes(newChildren);
		collectChangedNodes();

		checkMultiBatchCorrectness();
	}

	private void matchNodesOutsideOfChangedRange(Set<ASTNode> oldNodes, Set<ASTNode> newNodes) {
		int deltaTextLength= addedTextLength - removedTextLength;
		for (ASTNode oldNode : oldNodes) {
			if (oldNode.getStartPosition() + oldNode.getLength() <= offset) {
				matchOldNodeOutsideOfChangedRange(oldNode, newNodes, 0);
			} else if (oldNode.getStartPosition() >= offset + removedTextLength) {
				matchOldNodeOutsideOfChangedRange(oldNode, newNodes, deltaTextLength);
			}
		}
	}

	private void matchOldNodeOutsideOfChangedRange(ASTNode oldNode, Set<ASTNode> newNodes, int deltaTextLength) {
		for (ASTNode newNode : newNodes) {
			if (oldNode.getStartPosition() + deltaTextLength == newNode.getStartPosition() &&
					oldNode.getLength() == newNode.getLength() && oldNode.getNodeType() == newNode.getNodeType()) {
				matchedNodes.put(oldNode, newNode);
				break;
			}
		}
	}

	private void matchNodesStructurally(Set<ASTNode> oldNodes) {
		for (ASTNode oldNode : oldNodes) {
			if (!matchedNodes.containsKey(oldNode)) {
				String oldNodePositionalID= ASTNodesIdentifier.getPositionalNodeID(oldNode);
				ASTNode tentativeNewMatchingNode= ASTNodesIdentifier.getASTNodeFromPositonalID(newRootNode, oldNodePositionalID);
				if (tentativeNewMatchingNode != null && !matchedNodes.containsValue(tentativeNewMatchingNode) &&
						oldNode.getNodeType() == tentativeNewMatchingNode.getNodeType()) {
					matchedNodes.put(oldNode, tentativeNewMatchingNode);
				}
			}
		}
	}

	private void collectDeletedNodes(Set<ASTNode> oldNodes) {
		for (ASTNode oldNode : oldNodes) {
			if (!matchedNodes.containsKey(oldNode)) {
				deletedNodes.add(oldNode);
			}
		}
	}

	private void collectAddedNodes(Set<ASTNode> newNodes) {
		for (ASTNode newNode : newNodes) {
			if (!matchedNodes.containsValue(newNode)) {
				addedNodes.add(newNode);
			}
		}
	}

	private void collectChangedNodes() {
		for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			for (SimplePropertyDescriptor simplePropertyDescriptor : ASTHelper.getSimplePropertyDescriptors(oldNode)) {
				Object oldProperty= oldNode.getStructuralProperty(simplePropertyDescriptor);
				Object newProperty= newNode.getStructuralProperty(simplePropertyDescriptor);
				if (oldProperty == null && newProperty != null || oldProperty != null && !oldProperty.equals(newProperty)) {
					//Matched node is changed.
					changedNodes.put(oldNode, newNode);
					break;
				}
			}
		}
	}

	/**
	 * More sanity checks to ensure that multi batches are created only for boxed renames (can
	 * happen before rename and after extract method refactorings).
	 * 
	 */
	private void checkMultiBatchCorrectness() {
		if (batchSize <= 1) {
			return; //Not a multi batch, so nothing to check.
		}
		if (changedNodes.size() != batchSize || !deletedNodes.isEmpty() || !addedNodes.isEmpty()) {
			throw new RuntimeException("Multi batched node collections have wrong sizes!");
		}
		String oldText= null;
		String newText= null;
		Set<ASTNode> oldNodes= new HashSet<ASTNode>();
		Set<ASTNode> newNodes= new HashSet<ASTNode>();
		for (Entry<ASTNode, ASTNode> mapEntry : changedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			if (oldNodes.contains(oldNode) || newNodes.contains(newNode)) {
				throw new RuntimeException("Multi batched update changed same nodes more than ones!");
			}
			oldNodes.add(oldNode);
			newNodes.add(newNode);
			if (oldText == null) {
				oldText= oldNode.toString();
			}
			if (newText == null) {
				newText= newNode.toString();
			}
			if (!oldText.equals(oldNode.toString()) || !newText.equals(newNode.toString())) {
				throw new RuntimeException("Multi batch update contains different old or new texts!");
			}
		}
	}

}
