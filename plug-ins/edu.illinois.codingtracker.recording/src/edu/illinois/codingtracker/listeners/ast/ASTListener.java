/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

import edu.illinois.codingtracker.listeners.BasicListener;
import edu.illinois.codingtracker.operations.ast.ASTOperation.OperationKind;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTListener extends BasicListener {

	private static volatile ASTListener astListenerInstance= null;

	private CoherentTextChange currentTextChange;

	private IDocument currentDocument;

	public static ASTListener getInstance() {
		if (astListenerInstance == null) {
			astListenerInstance= new ASTListener();
		}
		return astListenerInstance;
	}

	private ASTListener() { //hide the constructor
		//do nothing
	}

	public void beforeDocumentChange(DocumentEvent event) {
		//If the edited document has changed, flush the accumulated changes.
		if (currentDocument != null && currentDocument != event.getDocument()) {
			flushCurrentTextChange();
		}
		currentDocument= event.getDocument();
		if (currentTextChange == null) {
			currentTextChange= new CoherentTextChange(event.getDocument().get(), event.getOffset(), event.getLength(), event.getText().length());
		} else {
			if (currentTextChange.shouldGlueNewTextChange(event.getOffset(), event.getLength())) {
				currentTextChange.glueNewTextChange(event.getOffset(), event.getLength(), event.getText().length());
			} else {
				flushCurrentTextChange();
				currentTextChange= new CoherentTextChange(event.getDocument().get(), event.getOffset(), event.getLength(), event.getText().length());
			}
		}
	}

	public void flushCurrentTextChange() {
		if (currentTextChange != null && currentTextChange.isActualChange()) {
			int changeOffset= currentTextChange.getOffset();
			int addedTextLength= currentTextChange.getAddedTextLength();
			int removedTextLength= currentTextChange.getRemovedTextLength();
			int deltaTextLength= addedTextLength - removedTextLength;
			System.out.println("deltaTextLength=" + deltaTextLength);
			CoveredNodesFinder oldAffectedNodesFinder= getAffectedNodesFinder("BEFORE:\n", currentTextChange.getOldDocumentText(), changeOffset, removedTextLength);
			CoveredNodesFinder newAffectedNodesFinder= getAffectedNodesFinder("AFTER:\n", currentTextChange.getNewDocumentText(), changeOffset, addedTextLength);
			ASTNode oldRootNode= oldAffectedNodesFinder.getRootNode();
			ASTNode oldCoveringNode= oldAffectedNodesFinder.getCoveringNode();
			ASTNode newRootNode= newAffectedNodesFinder.getRootNode();
			ASTNode newCoveringNode= newAffectedNodesFinder.getCoveringNode();
			String oldCoveringNodeID= ASTHelper.getPositionalNodeID(oldCoveringNode);
			System.out.println("oldCoveringNodeID: " + oldCoveringNodeID);
			String newCoveringNodeID= ASTHelper.getPositionalNodeID(newCoveringNode);
			System.out.println("newCoveringNodeID: " + newCoveringNodeID);
			String initialCommonCoveringNodeID;
			if (oldCoveringNodeID.startsWith(newCoveringNodeID)) {
				initialCommonCoveringNodeID= newCoveringNodeID;
			} else if (newCoveringNodeID.startsWith(oldCoveringNodeID)) {
				initialCommonCoveringNodeID= oldCoveringNodeID;
			} else {
				throw new RuntimeException("Can not find a common covering node!");
			}
			ASTNode oldCommonCoveringNode= ASTHelper.getASTNodeFromPositonalID(oldRootNode, initialCommonCoveringNodeID);
			ASTNode newCommonCoveringNode= ASTHelper.getASTNodeFromPositonalID(newRootNode, initialCommonCoveringNodeID);
			while (oldCommonCoveringNode.getStartPosition() != newCommonCoveringNode.getStartPosition() ||
					oldCommonCoveringNode.getLength() - removedTextLength + addedTextLength != newCommonCoveringNode.getLength() ||
					oldCommonCoveringNode.getNodeType() != newCommonCoveringNode.getNodeType()) {
				oldCommonCoveringNode= oldCommonCoveringNode.getParent();
				newCommonCoveringNode= newCommonCoveringNode.getParent();
			}

			String commonCoveringNodeID= ASTHelper.getPositionalNodeID(oldCommonCoveringNode);
			System.out.println("commonCoveringNodeID: " + commonCoveringNodeID);
			//ASTHelper.printSubtree(oldCommonCoveringNode);
			//ASTHelper.printSubtree(newCommonCoveringNode);

			Map<ASTNode, ASTNode> matchedNodes= new HashMap<ASTNode, ASTNode>();
			Set<ASTNode> deletedNodes= new HashSet<ASTNode>();
			Set<ASTNode> addedNodes= new HashSet<ASTNode>();

			matchedNodes.put(oldCommonCoveringNode, newCommonCoveringNode);
			ChildrenNodesFinder oldChildrenNodesFinder= new ChildrenNodesFinder(oldCommonCoveringNode);
			ChildrenNodesFinder newChildrenNodesFinder= new ChildrenNodesFinder(newCommonCoveringNode);
			List<ASTNode> oldChildren= oldChildrenNodesFinder.getChildrenNodes();
			List<ASTNode> newChildren= newChildrenNodesFinder.getChildrenNodes();

			//First, match nodes that are completely before or completely after the changed range.
			for (ASTNode oldChild : oldChildren) {
				if (oldChild.getStartPosition() + oldChild.getLength() <= changeOffset) {
					for (ASTNode newChild : newChildren) {
						if (oldChild.getStartPosition() == newChild.getStartPosition() && oldChild.getLength() == newChild.getLength() &&
								oldChild.getNodeType() == newChild.getNodeType()) {
							matchedNodes.put(oldChild, newChild);
							break;
						}
					}
				}
				if (oldChild.getStartPosition() >= changeOffset + removedTextLength) {
					for (ASTNode newChild : newChildren) {
						if (oldChild.getStartPosition() + deltaTextLength == newChild.getStartPosition() && oldChild.getLength() == newChild.getLength() &&
								oldChild.getNodeType() == newChild.getNodeType()) {
							matchedNodes.put(oldChild, newChild);
						}
					}
				}
			}

			//Next, match yet unmatched nodes with the same structural positions and types.
			for (ASTNode oldChild : oldChildren) {
				if (!matchedNodes.containsKey(oldChild)) {
					String oldChildPositionalID= ASTHelper.getPositionalNodeID(oldChild);
					ASTNode tentativeNewChildMatchingNode= ASTHelper.getASTNodeFromPositonalID(newRootNode, oldChildPositionalID);
					if (tentativeNewChildMatchingNode != null && !matchedNodes.containsValue(tentativeNewChildMatchingNode) &&
							oldChild.getNodeType() == tentativeNewChildMatchingNode.getNodeType()) {
						matchedNodes.put(oldChild, tentativeNewChildMatchingNode);
					}
				}
			}

			//Finally, collect the remaining unmatched nodes as deleted and added nodes.
			for (ASTNode oldChild : oldChildren) {
				if (!matchedNodes.containsKey(oldChild)) {
					deletedNodes.add(oldChild);
				}
			}
			for (ASTNode newChild : newChildren) {
				if (!matchedNodes.containsValue(newChild)) {
					addedNodes.add(newChild);
				}
			}

			//TODO: Mapping of persistent IDs to location-based IDs should consider location file in order to avoid identifying elements from
			//different files with the same ID.
			//TODO: Add cyclomatic complexity of the containing method, if any, to each recorded ASTOperation.
			//TODO: For on-the-fly recording use the current time. While replaying - take the time of the last code change operation.
			//TODO: Refactor!

			long currentTime= System.currentTimeMillis();
			Map<String, String> updatePositionalNodeIDsMap= new HashMap<String, String>();
			for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
				//System.out.println("Matched nodes:\n");
				ASTNode oldNode= mapEntry.getKey();
				ASTNode newNode= mapEntry.getValue();
				//ASTHelper.printASTNode(oldNode);
				//ASTHelper.printASTNode(newNode);
				updatePositionalNodeIDsMap.put(ASTHelper.getPositionalNodeID(oldNode), ASTHelper.getPositionalNodeID(newNode));
				for (SimplePropertyDescriptor simplePropertyDescriptor : ASTHelper.getSimplePropertyDescriptors(oldNode)) {
					Object oldProperty= oldNode.getStructuralProperty(simplePropertyDescriptor);
					Object newProperty= newNode.getStructuralProperty(simplePropertyDescriptor);
					if (oldProperty == null && newProperty != null || oldProperty != null && !oldProperty.equals(newProperty)) {
						//Matched node is changed.
						recordASTOperation(OperationKind.CHANGE, oldNode, newNode.toString(), currentTime);
						break;
					}
				}
			}

			//System.out.println("Deleted nodes:\n");
			for (ASTNode deletedNode : deletedNodes) {
				//ASTHelper.printASTNode(deletedNode);
				recordASTOperation(OperationKind.DELETE, deletedNode, "", currentTime);
				ASTHelper.removePersistentNodeID(deletedNode);
			}

			//Update the persistent IDs after delete, but before add. Also, do it as an atomic operation.
			ASTHelper.updatePersistentNodeIDs(updatePositionalNodeIDsMap);

			//System.out.println("Added node:\n");
			for (ASTNode addedNode : addedNodes) {
				//ASTHelper.printASTNode(addedNode);
				recordASTOperation(OperationKind.ADD, addedNode, "", currentTime);
			}
		}
		currentTextChange= null;
	}

	private void recordASTOperation(OperationKind operationKind, ASTNode affectedNode, String newText, long timestamp) {
		String containingMethodName= "";
		long containingMethodPersistentID= -1;
		MethodDeclaration containingMethod= ASTHelper.getContainingMethod(affectedNode);
		if (containingMethod != null) {
			containingMethodName= ASTHelper.getQualifiedMethodName(containingMethod);
			containingMethodPersistentID= ASTHelper.getPersistentNodeID(containingMethod);
		}
		operationRecorder.recordASTOperation(operationKind, affectedNode, newText, ASTHelper.getPersistentNodeID(affectedNode), containingMethodPersistentID,
				containingMethodName, timestamp);
	}

	public void afterDocumentChange(DocumentEvent event) {
		if (currentTextChange == null) {
			throw new RuntimeException("The current coherent text change should not be null after a document was changed: " + event);
		}
		currentTextChange.updateNewDocumentText(event.getDocument().get());
	}

	private CoveredNodesFinder getAffectedNodesFinder(String message, String source, int offset, int length) {
		//System.out.println(message + "[" + offset + ", " + (offset + length) + "): " + source.substring(offset, offset + length));
		ASTParser parser= createParser();
		parser.setSource(source.toCharArray());
		ASTNode rootNode= parser.createAST(null);
		//System.out.println("Root node: " + rootNode.toString());
		//System.out.println("Offset=" + offset + ", length=" + length);
		CoveredNodesFinder nodeFinder= new CoveredNodesFinder(rootNode, offset, length);
		return nodeFinder;
	}

	//TODO: Should the parser be created once and then just reused?
	private ASTParser createParser() {
		ASTParser parser= ASTParser.newParser(3);
		parser.setStatementsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		//Avoid resolving bindings to speed up the parsing.
		parser.setResolveBindings(false);
		parser.setBindingsRecovery(false);
		return parser;
	}

}
