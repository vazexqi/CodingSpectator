/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.inferencing;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
class CoveringNodesFinder extends ASTVisitor {

	private final List<CoherentTextChange> coherentTextChanges= new LinkedList<CoherentTextChange>();

	private final ASTNode newRootNode;

	private final ASTNode oldRootNode;

	private ASTNode newCoveringNode;

	private ASTNode oldCoveringNode;

	private int totalDelta;


	public CoveringNodesFinder(List<CoherentTextChange> coherentTextChanges) {
		super(true);
		this.coherentTextChanges.addAll(coherentTextChanges);
		oldRootNode= ASTHelper.getRootNode(getFirstTextChange().getInitialDocumentText());
		newRootNode= ASTHelper.getRootNode(getLastTextChange().getFinalDocumentText());
		oldCoveringNode= findCoveringNode(true);
		newCoveringNode= findCoveringNode(false);
	}

	public ASTNode getNewRootNode() {
		return newRootNode;
	}

	public ASTNode getOldRootNode() {
		return oldRootNode;
	}

	public ASTNode getNewCoveringNode() {
		return newCoveringNode;
	}

	public ASTNode getOldCoveringNode() {
		return oldCoveringNode;
	}

	public int getTotalDelta() {
		return totalDelta;
	}

	private CoherentTextChange getFirstTextChange() {
		return coherentTextChanges.get(0);
	}

	private CoherentTextChange getLastTextChange() {
		return coherentTextChanges.get(coherentTextChanges.size() - 1);
	}

	private ASTNode findCoveringNode(boolean isOldAST) {
		Stack<ASTNode> coveringNodes= findInitialCoveringNodes(isOldAST);
		int accumulatedCoveringDelta= 0;
		TextChangesIterator textChangesIterator= new TextChangesIterator(isOldAST);
		while (textChangesIterator.hasNext()) {
			CoherentTextChange currentTextChange= textChangesIterator.getNext();
			while (!isNodeCoveringTextChange(coveringNodes.peek(), currentTextChange, accumulatedCoveringDelta, isOldAST)) {
				coveringNodes.pop();
			}
			accumulatedCoveringDelta+= currentTextChange.getDeltaTextLength();
		}
		totalDelta= accumulatedCoveringDelta;
		return coveringNodes.peek();
	}

	private Stack<ASTNode> findInitialCoveringNodes(final boolean isOldAST) {
		final Stack<ASTNode> initialCoveringNodes= new Stack<ASTNode>();
		final CoherentTextChange initialTextChange= isOldAST ? getFirstTextChange() : getLastTextChange();
		ASTNode rootNode= isOldAST ? oldRootNode : newRootNode;
		rootNode.accept(new ASTVisitor() {
			@Override
			public boolean preVisit2(ASTNode node) {
				if (isNodeCoveringTextChange(node, initialTextChange, 0, isOldAST)) {
					initialCoveringNodes.push(node);
					return true;
				}
				return false;
			}
		});
		return initialCoveringNodes;
	}

	private boolean isNodeCoveringTextChange(ASTNode node, CoherentTextChange textChange, int oldCoveringDelta,
												boolean isOldAST) {
		//[textChangeStart, textChangeEnd) is intersected with [nodeStart, nodeEnd)
		return getTextChangeStart(textChange) >= getNodeStart(node) &&
				getTextChangeEnd(textChange, isOldAST) <= getNodeEnd(node) + getActualDelta(oldCoveringDelta, isOldAST);
	}

	public Integer getOutlierDelta(ASTNode node, boolean isOldAST) {
		int accumulatedDelta= 0;
		TextChangesIterator textChangesIterator= new TextChangesIterator(isOldAST);
		while (textChangesIterator.hasNext()) {
			CoherentTextChange textChange= textChangesIterator.getNext();
			if (getNodeEnd(node) + accumulatedDelta <= getTextChangeStart(textChange)) {
				//no change to delta
			} else if (getNodeStart(node) + accumulatedDelta >= getTextChangeEnd(textChange, isOldAST)) {
				accumulatedDelta+= getActualDelta(textChange.getDeltaTextLength(), isOldAST);
			} else {
				return null; //The given node is not an outlier, so there is no outlier delta to return.
			}
		}
		return accumulatedDelta;
	}

	private int getActualDelta(int oldASTDelta, boolean isOldAST) {
		return isOldAST ? oldASTDelta : -oldASTDelta;
	}

	private int getTextChangeStart(CoherentTextChange textChange) {
		return textChange.getOffset();
	}

	private int getTextChangeEnd(CoherentTextChange textChange, boolean isOldAST) {
		int textChangeLength= isOldAST ? textChange.getRemovedTextLength() : textChange.getAddedTextLength();
		return textChange.getOffset() + textChangeLength;
	}

	private int getNodeStart(ASTNode node) {
		return node.getStartPosition();
	}

	private int getNodeEnd(ASTNode node) {
		return node.getStartPosition() + node.getLength();
	}

	private class TextChangesIterator {

		private final boolean isForwardIteration;

		private final ListIterator<CoherentTextChange> listIterator;

		private TextChangesIterator(boolean isForwardIteration) {
			this.isForwardIteration= isForwardIteration;
			int startIteratorIndex= isForwardIteration ? 0 : coherentTextChanges.size();
			listIterator= coherentTextChanges.listIterator(startIteratorIndex);
		}

		private boolean hasNext() {
			if (isForwardIteration) {
				return listIterator.hasNext();
			} else {
				return listIterator.hasPrevious();
			}
		}

		private CoherentTextChange getNext() {
			if (isForwardIteration) {
				return listIterator.next();
			} else {
				return listIterator.previous();
			}
		}
	}

}
