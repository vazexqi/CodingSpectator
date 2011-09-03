/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
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
			CoveredNodesFinder oldAffectedNodesFinder= getAffectedNodesFinder("BEFORE:\n", currentTextChange.getOldDocumentText(), currentTextChange.getOffset(),
					currentTextChange.getRemovedTextLength());
			CoveredNodesFinder newAffectedNodesFinder= getAffectedNodesFinder("AFTER:\n", currentTextChange.getNewDocumentText(), currentTextChange.getOffset(),
					currentTextChange.getAddedTextLength());
			ASTNode oldCoveringNode= oldAffectedNodesFinder.getCoveringNode();
			List<ASTNode> oldCoveredNodes= oldAffectedNodesFinder.getCoveredNodes();
			ASTNode newCoveringNode= newAffectedNodesFinder.getCoveringNode();
			List<ASTNode> newCoveredNodes= newAffectedNodesFinder.getCoveredNodes();
			String oldCoveringNodeID= ASTHelper.getPositionalNodeID(oldCoveringNode);
			System.out.println("oldCoveringNodeID: " + oldCoveringNodeID);
			String newCoveringNodeID= ASTHelper.getPositionalNodeID(newCoveringNode);
			System.out.println("newCoveringNodeID: " + newCoveringNodeID);
			String commonCoveringNodeID;
			if (oldCoveringNodeID.startsWith(newCoveringNodeID)) {
				commonCoveringNodeID= newCoveringNodeID;
			} else if (newCoveringNodeID.startsWith(oldCoveringNodeID)) {
				commonCoveringNodeID= oldCoveringNodeID;
			} else {
				throw new RuntimeException("Can not find a common covering node!");
			}
			System.out.println("commonCoveringNodeID: " + commonCoveringNodeID);
			//TODO: For a method AST operation store the fully qualified name of the method. Also, note that methodID is nodeID.
			//TODO: For on-the-fly recording use the current time. While replaying - take the time of the last code change operation.
			long currentTime= System.currentTimeMillis();
			if (isNodeChange(oldCoveringNode, oldCoveredNodes, newCoveringNode, newCoveredNodes)) {
				operationRecorder.recordASTOperation(OperationKind.CHANGE, oldCoveringNode, newCoveringNode.toString(), -1, -1, "", currentTime);
			} else {
				for (ASTNode deletedNode : oldCoveredNodes) {
					operationRecorder.recordASTOperation(OperationKind.DELETE, deletedNode, "", ASTHelper.getPersistentNodeID(deletedNode), -1, "", currentTime);
					ASTHelper.removePersistentNodeID(deletedNode);
				}

				//Update the persistent IDs after delete, but before add. Also, do it as an atomic operation.
				ASTNode oldCommonParentNode= ASTHelper.getASTNodeFromPositonalID(oldAffectedNodesFinder.getRootNode(), commonCoveringNodeID);
				PersistentNodesFinder oldPersistentNodesFinder= new PersistentNodesFinder(oldCommonParentNode, oldCoveredNodes);
				List<ASTNode> oldPersistentNodes= oldPersistentNodesFinder.getPersistentNodes();
				ASTNode newCommonParentNode= ASTHelper.getASTNodeFromPositonalID(newAffectedNodesFinder.getRootNode(), commonCoveringNodeID);
				PersistentNodesFinder newPersistentNodesFinder= new PersistentNodesFinder(newCommonParentNode, newCoveredNodes);
				List<ASTNode> newPersistentNodes= newPersistentNodesFinder.getPersistentNodes();
				if (oldPersistentNodes.size() != newPersistentNodes.size()) {
					System.out.println("oldPeristentNodesCount: " + oldPersistentNodes.size());
					System.out.println("newPeristentNodesCount: " + newPersistentNodes.size());
					System.out.println("oldCommonParentNode: " + oldCommonParentNode);
					System.out.println("newCommonParentNode: " + newCommonParentNode);
					ASTHelper.printSubtree(oldCommonParentNode);
					ASTHelper.printSubtree(newCommonParentNode);
					oldCommonParentNode= oldCommonParentNode.getParent();
					newCommonParentNode= newCommonParentNode.getParent();
					oldPersistentNodesFinder= new PersistentNodesFinder(oldCommonParentNode, oldCoveredNodes);
					oldPersistentNodes= oldPersistentNodesFinder.getPersistentNodes();
					newPersistentNodesFinder= new PersistentNodesFinder(newCommonParentNode, newCoveredNodes);
					newPersistentNodes= newPersistentNodesFinder.getPersistentNodes();
					System.out.println("oldPeristentNodesCount: " + oldPersistentNodes.size());
					System.out.println("newPeristentNodesCount: " + newPersistentNodes.size());
					System.out.println("oldCommonParentNode: " + oldCommonParentNode);
					System.out.println("newCommonParentNode: " + newCommonParentNode);
					ASTHelper.printSubtree(oldCommonParentNode);
					ASTHelper.printSubtree(newCommonParentNode);
					//throw new RuntimeException("Different number of old and new persistent nodes!");
				}
				Map<String, String> updatePositionalNodeIDsMap= new HashMap<String, String>();
				for (int i= 0; i < oldPersistentNodes.size(); i++) {
					ASTNode oldPersistentNode= oldPersistentNodes.get(i);
					ASTNode newPersistentNode= newPersistentNodes.get(i);
					if (!oldPersistentNode.getClass().getSimpleName().equals(newPersistentNode.getClass().getSimpleName())) {
						//throw new RuntimeException("Matched new and old persistent nodes have different class names!");
					}
					updatePositionalNodeIDsMap.put(ASTHelper.getPositionalNodeID(oldPersistentNode), ASTHelper.getPositionalNodeID(newPersistentNode));
				}
				ASTHelper.updatePersistentNodeIDs(updatePositionalNodeIDsMap);

				for (ASTNode addedNode : newCoveredNodes) {
					operationRecorder.recordASTOperation(OperationKind.ADD, addedNode, "", ASTHelper.getPersistentNodeID(addedNode), -1, "", currentTime);
				}
			}
		}
		currentTextChange= null;
	}

	private boolean isNodeChange(ASTNode oldCoveringNode, List<ASTNode> oldCoveredNodes, ASTNode newCoveringNode, List<ASTNode> newCoveredNodes) {
		if (!ASTHelper.getPositionalNodeID(oldCoveringNode).equals(ASTHelper.getPositionalNodeID(newCoveringNode)) || oldCoveringNode.getNodeType() != newCoveringNode.getNodeType()) {
			return false;
		}
		if (oldCoveredNodes.isEmpty() && newCoveredNodes.isEmpty()) {
			return true;
		}
		if (oldCoveredNodes.size() == 1 && newCoveredNodes.size() == 1 && oldCoveredNodes.get(0) == oldCoveringNode && newCoveredNodes.get(0) == newCoveringNode) {
			return true;
		}
		return false;
	}

	public void afterDocumentChange(DocumentEvent event) {
		if (currentTextChange == null) {
			throw new RuntimeException("The current coherent text change should not be null after a document was changed: " + event);
		}
		currentTextChange.updateNewDocumentText(event.getDocument().get());
	}

	private CoveredNodesFinder getAffectedNodesFinder(String message, String source, int offset, int length) {
		System.out.println(message + "[" + offset + ", " + (offset + length) + "): " + source.substring(offset, offset + length));
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
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		return parser;
	}

}
