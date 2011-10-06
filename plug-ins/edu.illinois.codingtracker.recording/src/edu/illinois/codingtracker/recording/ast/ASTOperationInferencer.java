/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperationInferencer {

	CoveringNodesFinder affectedNodesFinder;

	private int batchSize;

	private ASTNode newCommonCoveringNode;

	private ASTNode oldCommonCoveringNode;

	private Map<ASTNode, ASTNode> matchedNodes= new HashMap<ASTNode, ASTNode>();

	private Map<ASTNode, ASTNode> changedNodes= new HashMap<ASTNode, ASTNode>(); //Is a subset of matchedNodes.

	private Set<ASTNode> deletedNodes= new HashSet<ASTNode>();

	private Set<ASTNode> addedNodes= new HashSet<ASTNode>();


	public ASTOperationInferencer(int batchSize, CoherentTextChange coherentTextChange) {
		this.batchSize= batchSize;
		if (batchSize > 1) {
			//Multi batch - simulate that the whole document's content is replaced.
			Document editedDocument= new Document(coherentTextChange.getInitialDocumentText());
			String newText= coherentTextChange.getFinalDocumentText();
			DocumentEvent documentEvent= new DocumentEvent(editedDocument, 0, editedDocument.getLength(), newText);
			initializeInferencer(new CoherentTextChange(documentEvent, coherentTextChange.getTimestamp()));
		} else {
			initializeInferencer(coherentTextChange);
		}
	}

	public ASTOperationInferencer(List<CoherentTextChange> coherentTextChanges) {
		batchSize= 1;
		initializeInferencer(coherentTextChanges);
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

	private void initializeInferencer(CoherentTextChange coherentTextChange) {
		List<CoherentTextChange> coherentTextChanges= new LinkedList<CoherentTextChange>();
		coherentTextChanges.add(coherentTextChange);
		initializeInferencer(coherentTextChanges);
	}

	private void initializeInferencer(List<CoherentTextChange> coherentTextChanges) {
		affectedNodesFinder= new CoveringNodesFinder(coherentTextChanges);

		ASTNode oldRootNode= affectedNodesFinder.getOldRootNode();
		ASTNode oldCoveringNode= affectedNodesFinder.getOldCoveringNode();

		ASTNode newRootNode= affectedNodesFinder.getNewRootNode();
		ASTNode newCoveringNode= affectedNodesFinder.getNewCoveringNode();

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
				oldCoveringNode.getLength() + affectedNodesFinder.getTotalDelta() != newCoveringNode.getLength() ||
				oldCoveringNode.getNodeType() != newCoveringNode.getNodeType();
	}

	//TODO: Consider that the old AST could be problematic as well.
	public boolean isProblematicInference() {
		ASTNode newCoveringNode= affectedNodesFinder.getNewCoveringNode();
		if (ASTHelper.isRecoveredOrMalformed(newCoveringNode) || ASTHelper.isRecoveredOrMalformed(newCommonCoveringNode)) {
			return true;
		}
		MethodDeclaration coveringMethodDeclaration= ASTHelper.getContainingMethod(newCoveringNode);
		if (coveringMethodDeclaration != null && ASTHelper.isRecoveredOrMalformed(coveringMethodDeclaration)) {
			return true;
		}
		return hasProblematicAffectedNodes();
	}

	private boolean hasProblematicAffectedNodes() {
		Set<ASTNode> oldChildren= ASTHelper.getAllChildren(oldCommonCoveringNode);
		for (ASTNode newChildNode : ASTHelper.getAllChildren(affectedNodesFinder.getNewCoveringNode())) {
			if (ASTHelper.isRecoveredOrMalformed(newChildNode)) {
				Integer outlierDelta= affectedNodesFinder.getOutlierDelta(newChildNode, false);
				if (outlierDelta == null) {
					//If the node is not outlier (i.e. it is affected), then it should be well formed.
					return true;
				} else {
					//If an outlier is not well formed, there should exist the matching old node that is also not well formed.
					ASTNode oldNode= findMatchingNode(newChildNode, oldChildren, outlierDelta);
					if (oldNode == null || !ASTHelper.isRecoveredOrMalformed(oldNode)) {
						return true;
					}
				}
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
		for (ASTNode oldNode : oldNodes) {
			Integer outlierDelta= affectedNodesFinder.getOutlierDelta(oldNode, true);
			if (outlierDelta != null) {
				ASTNode newNode= findMatchingNode(oldNode, newNodes, outlierDelta);
				if (newNode != null) {
					matchedNodes.put(oldNode, newNode);
				}
			}
		}
	}

	private ASTNode findMatchingNode(ASTNode nodeToMatch, Set<ASTNode> candidateNodes, int deltaTextLength) {
		for (ASTNode node : candidateNodes) {
			if (nodeToMatch.getStartPosition() + deltaTextLength == node.getStartPosition() &&
					nodeToMatch.getLength() == node.getLength() && nodeToMatch.getNodeType() == node.getNodeType()) {
				return node;
			}
		}
		return null;
	}

	private void matchNodesStructurally(Set<ASTNode> oldNodes) {
		ASTNode newRootNode= affectedNodesFinder.getNewRootNode();
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
