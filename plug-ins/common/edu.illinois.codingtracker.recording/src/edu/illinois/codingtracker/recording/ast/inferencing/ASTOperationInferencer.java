/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.inferencing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;

import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperationInferencer {

	private final List<CoherentTextChange> coherentTextChanges= new LinkedList<CoherentTextChange>();

	private CoveringNodesFinder affectedNodesFinder;

	private ASTNode newCommonCoveringNode;

	private ASTNode oldCommonCoveringNode;

	private Map<ASTNode, ASTNode> matchedNodes= new HashMap<ASTNode, ASTNode>();

	private Map<ASTNode, ASTNode> changedNodes= new HashMap<ASTNode, ASTNode>(); //Is a subset of matchedNodes.

	private Set<ASTNode> deletedNodes= new HashSet<ASTNode>();

	private Set<ASTNode> addedNodes= new HashSet<ASTNode>();

	private boolean isPossiblyCommentingOrUncommentingChange;

	private boolean isUndoing;

	//Persist some information required for refactoring inference
	private ASTNode oldRootNode;

	private ASTNode newRootNode;


	public ASTOperationInferencer(CoherentTextChange coherentTextChange) {
		if (coherentTextChange == null) {
			throw new RuntimeException("Initialized AST operation inferencer with a null text change!");
		}
		coherentTextChanges.add(coherentTextChange);
		initializeInferencer();
	}

	public ASTOperationInferencer(List<CoherentTextChange> coherentTextChanges) {
		this.coherentTextChanges.addAll(coherentTextChanges);
		initializeInferencer();
	}

	public ASTNode getNewCommonCoveringNode() {
		return newCommonCoveringNode;
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

	public boolean isCommentingOrUncommenting() {
		//A commenting or uncommenting change should either delete or add nodes. This check allows to filter out some 
		//scenarios, in which comment markers are added or deleted in strings.
		return isPossiblyCommentingOrUncommentingChange && (!deletedNodes.isEmpty() || !addedNodes.isEmpty());
	}

	public boolean isUndoing() {
		return isUndoing;
	}

	public ASTNode getOldRootNode() {
		return oldRootNode;
	}

	public ASTNode getNewRootNode() {
		return newRootNode;
	}

	private void initializeInferencer() {
		initializeOperationState();

		affectedNodesFinder= new CoveringNodesFinder(coherentTextChanges);

		oldRootNode= affectedNodesFinder.getOldRootNode();
		ASTNode oldCoveringNode= affectedNodesFinder.getOldCoveringNode();

		newRootNode= affectedNodesFinder.getNewRootNode();
		ASTNode newCoveringNode= affectedNodesFinder.getNewCoveringNode();

		String initialCommonCoveringNodeID= ASTNodesIdentifier.getCommonPositonalNodeID(oldCoveringNode, newCoveringNode);
		oldCommonCoveringNode= ASTNodesIdentifier.getASTNodeFromPositonalID(oldRootNode, initialCommonCoveringNodeID);
		newCommonCoveringNode= ASTNodesIdentifier.getASTNodeFromPositonalID(newRootNode, initialCommonCoveringNodeID);
		while (areUnmatchingCoveringNodes(oldCommonCoveringNode, newCommonCoveringNode)) {
			oldCommonCoveringNode= oldCommonCoveringNode.getParent();
			newCommonCoveringNode= newCommonCoveringNode.getParent();
		}
	}

	private void initializeOperationState() {
		isPossiblyCommentingOrUncommentingChange= false;
		isUndoing= true;
		for (CoherentTextChange textChange : coherentTextChanges) {
			//We conservatively estimate that a single commenting or uncommenting change is sufficient to consider the
			//whole change as being possibly commenting or uncommenting. In other words, we err in favor of 
			//commenting/uncommenting actions.
			if (textChange.isCommentingOrUncommenting()) {
				isPossiblyCommentingOrUncommentingChange= true;
			}

			//All changes have to be undone edits to consider the inferenced AST operations the result of undoing.
			if (!textChange.isUndoing()) {
				isUndoing= false;
			}
		}
	}

	private boolean areUnmatchingCoveringNodes(ASTNode oldCoveringNode, ASTNode newCoveringNode) {
		return oldCoveringNode.getStartPosition() != newCoveringNode.getStartPosition() ||
				oldCoveringNode.getLength() + affectedNodesFinder.getTotalDelta() != newCoveringNode.getLength() ||
				oldCoveringNode.getNodeType() != newCoveringNode.getNodeType();
	}

	//TODO: Consider that the old AST could be problematic as well.
	public boolean isProblematicInference() {
		if (isNewCoveringNodeProblematic()) {
			return true;
		}
		return hasProblematicAffectedNodes();
	}

	private boolean isNewCoveringNodeProblematic() {
		//Note that covering nodes sometimes could be outliers (i.e. not affected), e.g. when text is added at the last offset
		//in a file. Therefore, covering nodes correctness should be checked separately.
		ASTNode newCoveringNode= affectedNodesFinder.getNewCoveringNode();
		if (ASTHelper.isRecoveredOrMalformed(newCoveringNode) || ASTHelper.isRecoveredOrMalformed(newCommonCoveringNode)) {
			return true;
		}
		//If the new common covering node is a block that represents a malformed method, then the block itself would not
		//be marked as malformed. Therefore, check whether its containing method declaration is well formed.
		//TODO: Note that a method might be malformed due to problems in the signature rather than the body, while the checks
		//below would consider the body to be malformed in such a case.
		if (newCommonCoveringNode instanceof Block && newCommonCoveringNode.getParent() instanceof MethodDeclaration &&
				ASTHelper.isRecoveredOrMalformed(newCommonCoveringNode.getParent())) {
			return true;
		}
		return false;
	}

	private boolean hasProblematicAffectedNodes() {
		Set<ASTNode> oldChildren= ASTHelper.getAllChildren(oldCommonCoveringNode);
		for (ASTNode newChildNode : ASTHelper.getAllChildren(newCommonCoveringNode)) {
			if (ASTHelper.isRecoveredOrMalformed(newChildNode)) {
				MatchDelta matchDelta= affectedNodesFinder.getMatchDelta(newChildNode, false);
				if (matchDelta == null) {
					//If the node is directly affected, then it should be well formed.
					return true;
				} else {
					//If the node is not directly affected and is not well formed, there should exist the matching 
					//old node that is also not well formed.
					ASTNode oldNode= findMatchingNode(newChildNode, oldChildren, matchDelta);
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

		//First, match nodes that are completely before or completely after the changed range
		//and nodes that have a child completely covering the changed range.
		matchNodesTextually(oldChildren, newChildren);

		//Next, match yet unmatched nodes with the same structural positions and types.
		matchNodesStructurally(oldChildren);

		//Finally, collect the remaining unmatched nodes as deleted and added nodes. Also, collect the changed matched nodes.
		collectDeletedNodes(oldChildren);
		collectAddedNodes(newChildren);
		collectChangedNodes();
	}

	private void matchNodesTextually(Set<ASTNode> oldNodes, Set<ASTNode> newNodes) {
		for (ASTNode oldNode : oldNodes) {
			MatchDelta matchDelta= affectedNodesFinder.getMatchDelta(oldNode, true);
			if (matchDelta != null) {
				ASTNode newNode= findMatchingNode(oldNode, newNodes, matchDelta);
				if (newNode != null) {
					matchedNodes.put(oldNode, newNode);
				}
			}
		}
	}

	private ASTNode findMatchingNode(ASTNode nodeToMatch, Set<ASTNode> candidateNodes, MatchDelta matchDelta) {
		for (ASTNode node : candidateNodes) {
			if (nodeToMatch.getStartPosition() + matchDelta.outlierDelta == node.getStartPosition() &&
					nodeToMatch.getLength() + matchDelta.coveringDelta == node.getLength() &&
					nodeToMatch.getNodeType() == node.getNodeType()) {
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

}
