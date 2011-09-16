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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.ast.ASTOperation.OperationKind;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ASTInferenceTextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
//TODO: Refactor this and other classes in this package!
public class ASTOperationRecorder {

	public static final boolean isInASTInferenceMode= System.getenv("AST_INFERENCE_MODE") != null;

	public static final boolean isInReplayMode= System.getenv("REPLAY_MODE") != null;

	private static volatile ASTOperationRecorder astRecorderInstance= null;

	private List<CoherentTextChange> currentTextChanges= new LinkedList<CoherentTextChange>();

	private IDocument currentDocument;

	private String currentFileID;

	private int currentIndexToGlueWith= 0;

	private int correlatedBatchSize= -1;

	private final CyclomaticComplexityCalculator cyclomaticComplexityCalculator= new CyclomaticComplexityCalculator();


	public static ASTOperationRecorder getInstance() {
		if (astRecorderInstance == null) {
			if (isInASTInferenceMode) {
				astRecorderInstance= new ASTOperationRecorder();
			} else {
				astRecorderInstance= new InactiveASTOperationRecorder();
			}
		}
		return astRecorderInstance;
	}

	ASTOperationRecorder() { //hide the constructor
		//do nothing
	}

	public void beforeDocumentChange(DocumentEvent event, String fileID) {
		//If the edited document has changed, flush the accumulated changes.
		if (currentDocument != null && currentDocument != event.getDocument()) {
			flushCurrentTextChanges();
		}
		currentDocument= event.getDocument();
		long timestamp= getTextChangeTimestamp();
		if (currentTextChanges.isEmpty()) {
			currentTextChanges.add(new CoherentTextChange(event, timestamp));
		} else {
			addToCurrentTextChanges(event, timestamp);
		}
		currentFileID= fileID;
	}

	private void addToCurrentTextChanges(DocumentEvent event, long timestamp) {
		if (correlatedBatchSize == -1) { //Batch size is not established yet.
			CoherentTextChange lastTextChange= currentTextChanges.get(currentTextChanges.size() - 1);
			CoherentTextChange newTextChange= new CoherentTextChange(event, timestamp);
			if (lastTextChange.isFirstGluing() && lastTextChange.isPossiblyCorrelatedWith(newTextChange)) {
				currentTextChanges.add(newTextChange);
				applyTextChangeToBatch(event, currentTextChanges.size() - 1);
			} else {
				correlatedBatchSize= currentTextChanges.size();
				currentIndexToGlueWith= 0;
				tryGluingInBatch(event, timestamp);
			}
		} else { //Batch size is already established.
			tryGluingInBatch(event, timestamp);
		}
	}

	private void tryGluingInBatch(DocumentEvent event, long timestamp) {
		CoherentTextChange textChangeToGlueWith= currentTextChanges.get(currentIndexToGlueWith);
		if (textChangeToGlueWith.shouldGlueNewTextChange(event)) {
			textChangeToGlueWith.glueNewTextChange(event, timestamp);
			applyTextChangeToBatch(event, currentIndexToGlueWith);
			currentIndexToGlueWith++;
			if (currentIndexToGlueWith == correlatedBatchSize) {
				currentIndexToGlueWith= 0;
			}
		} else {
			flushCurrentTextChanges();
			currentTextChanges.add(new CoherentTextChange(event, timestamp));
		}
	}

	/**
	 * 
	 * @param event
	 * @param excludeIndex - index of a batched coherent text change, whose document was already
	 *            updated (either in constructor or while gluing), so it should not be considered
	 *            again.
	 */
	private void applyTextChangeToBatch(DocumentEvent event, int excludeIndex) {
		for (int i= 0; i < currentTextChanges.size(); i++) {
			if (i != excludeIndex) {
				currentTextChanges.get(i).applyTextChange(event);
			}
		}
	}

	public void flushCurrentTextChanges() {
		if (!currentTextChanges.isEmpty() && currentTextChanges.get(0).isActualChange()) {
			if (correlatedBatchSize != -1 && currentIndexToGlueWith != 0) {
				throw new RuntimeException("Stopped gluing in the middle of a correlated batch!");
			}
			cyclomaticComplexityCalculator.resetCache();
			boolean isMultiBatch= currentTextChanges.size() > 1;
			CoherentTextChange firstTextChange= currentTextChanges.get(0);
			int changeOffset= isMultiBatch ? 0 : firstTextChange.getOffset();
			int addedTextLength= isMultiBatch ? firstTextChange.getFinalDocumentText().length() : firstTextChange.getAddedTextLength();
			int removedTextLength= isMultiBatch ? firstTextChange.getInitialDocumentText().length() : firstTextChange.getRemovedTextLength();
			int deltaTextLength= addedTextLength - removedTextLength;
			CoveredNodesFinder oldAffectedNodesFinder= getAffectedNodesFinder("BEFORE:\n", firstTextChange.getInitialDocumentText(), changeOffset, removedTextLength);
			CoveredNodesFinder newAffectedNodesFinder= getAffectedNodesFinder("AFTER:\n", firstTextChange.getFinalDocumentText(), changeOffset, addedTextLength);
			ASTNode oldRootNode= oldAffectedNodesFinder.getRootNode();
			ASTNode oldCoveringNode= oldAffectedNodesFinder.getCoveringNode();
			ASTNode newRootNode= newAffectedNodesFinder.getRootNode();
			ASTNode newCoveringNode= newAffectedNodesFinder.getCoveringNode();
			String initialCommonCoveringNodeID= ASTHelper.getCommonPositonalNodeID(oldCoveringNode, newCoveringNode);
			ASTNode oldCommonCoveringNode= ASTHelper.getASTNodeFromPositonalID(oldRootNode, initialCommonCoveringNodeID);
			ASTNode newCommonCoveringNode= ASTHelper.getASTNodeFromPositonalID(newRootNode, initialCommonCoveringNodeID);
			while (oldCommonCoveringNode.getStartPosition() != newCommonCoveringNode.getStartPosition() ||
					oldCommonCoveringNode.getLength() - removedTextLength + addedTextLength != newCommonCoveringNode.getLength() ||
					oldCommonCoveringNode.getNodeType() != newCommonCoveringNode.getNodeType()) {
				oldCommonCoveringNode= oldCommonCoveringNode.getParent();
				newCommonCoveringNode= newCommonCoveringNode.getParent();
			}
			//System.out.println("commonCoveringNodeID: " + ASTHelper.getPositionalNodeID(oldCommonCoveringNode));

			Map<ASTNode, ASTNode> matchedNodes= new HashMap<ASTNode, ASTNode>();
			Set<ASTNode> deletedNodes= new HashSet<ASTNode>();
			Set<ASTNode> addedNodes= new HashSet<ASTNode>();

			matchedNodes.put(oldCommonCoveringNode, newCommonCoveringNode);
			ChildrenNodesFinder oldChildrenNodesFinder= new ChildrenNodesFinder(oldCommonCoveringNode);
			ChildrenNodesFinder newChildrenNodesFinder= new ChildrenNodesFinder(newCommonCoveringNode);
			Set<ASTNode> oldChildren= oldChildrenNodesFinder.getChildrenNodes();
			Set<ASTNode> newChildren= newChildrenNodesFinder.getChildrenNodes();

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

			Map<ASTNode, ASTNode> changedMatchedNodes= new HashMap<ASTNode, ASTNode>(); //Needed for correctness checking only.

			Map<ASTNode, ASTNode> updatePositionalNodeIDsMap= new HashMap<ASTNode, ASTNode>();
			for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
				ASTNode oldNode= mapEntry.getKey();
				ASTNode newNode= mapEntry.getValue();
				updatePositionalNodeIDsMap.put(oldNode, newNode);
				for (SimplePropertyDescriptor simplePropertyDescriptor : ASTHelper.getSimplePropertyDescriptors(oldNode)) {
					Object oldProperty= oldNode.getStructuralProperty(simplePropertyDescriptor);
					Object newProperty= newNode.getStructuralProperty(simplePropertyDescriptor);
					if (oldProperty == null && newProperty != null || oldProperty != null && !oldProperty.equals(newProperty)) {
						//Matched node is changed.
						recordASTOperation(currentFileID, OperationKind.CHANGE, oldNode, newNode.toString());
						changedMatchedNodes.put(oldNode, newNode);
						break;
					}
				}
			}

			checkMultiBatchCorrectness(isMultiBatch, changedMatchedNodes, deletedNodes, addedNodes);

			recordDeleteASTOperation(currentFileID, deletedNodes);

			//Update the persistent IDs after delete, but before add. Also, do it as an atomic operation.
			ASTHelper.updatePersistentNodeIDs(currentFileID, updatePositionalNodeIDsMap);

			recordAddASTOperation(currentFileID, addedNodes);
		}
		currentTextChanges.clear();
		correlatedBatchSize= -1;
	}

	/**
	 * More sanity checks to ensure that multi batches are created only for boxed renames (can
	 * happen before rename and after extract method refactorings).
	 * 
	 * @param isMultiBatch
	 * @param matchedNodes
	 * @param deletedNodes
	 * @param addedNodes
	 */
	private void checkMultiBatchCorrectness(boolean isMultiBatch, Map<ASTNode, ASTNode> changedMatchedNodes, Set<ASTNode> deletedNodes, Set<ASTNode> addedNodes) {
		if (!isMultiBatch) {
			return;
		}
		if (changedMatchedNodes.size() != currentTextChanges.size() || !deletedNodes.isEmpty() || !addedNodes.isEmpty()) {
			throw new RuntimeException("Multi batched node collections have wrong sizes!");
		}
		String oldText= null;
		String newText= null;
		Set<ASTNode> oldNodes= new HashSet<ASTNode>();
		Set<ASTNode> newNodes= new HashSet<ASTNode>();
		for (Entry<ASTNode, ASTNode> mapEntry : changedMatchedNodes.entrySet()) {
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

	private long getTextChangeTimestamp() {
		if (isInReplayMode) {
			return TextChangeOperation.lastReplayedTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}

	private void recordDeleteASTOperation(String fileID, Set<ASTNode> deletedNodes) {
		for (ASTNode deletedNode : deletedNodes) {
			recordASTOperation(fileID, OperationKind.DELETE, deletedNode, "");
		}
		//Delete nodes after recording all delete operations to avoid scenarios, in which recording a delete operation,
		//requires a node that already was deleted (e.g. the containing method node).
		for (ASTNode deletedNode : deletedNodes) {
			ASTHelper.removePersistentNodeID(fileID, deletedNode);
		}
	}

	private void recordAddASTOperation(String fileID, Set<ASTNode> addedNodes) {
		for (ASTNode addedNode : addedNodes) {
			recordASTOperation(fileID, OperationKind.ADD, addedNode, "");
		}
	}

	private void recordASTOperation(String fileID, OperationKind operationKind, ASTNode affectedNode, String newText) {
		String containingMethodName= "";
		long containingMethodPersistentID= -1;
		int containingMethodLinesCount= -1;
		int containingMethodCyclomaticComplexity= -1;
		MethodDeclaration containingMethod= ASTHelper.getContainingMethod(affectedNode);
		if (containingMethod != null) {
			//Note that for added nodes we get lines count and cyclomatic complexity of the resulting containing method 
			//that already contains these added nodes.
			//Also, note that containingMethodLinesCount would not count lines with comments or white spaces, but would
			//count several statements on the same line as separate lines (i.e. AST node is normalized such that each statement
			//appears on a separate line, which is usually the case with the actual code as well).
			containingMethodLinesCount= (new Document(containingMethod.toString().trim())).getNumberOfLines();
			containingMethodCyclomaticComplexity= cyclomaticComplexityCalculator.getCyclomaticComplexity(containingMethod);
			containingMethodName= ASTHelper.getQualifiedMethodName(containingMethod);
			containingMethodPersistentID= ASTHelper.getPersistentNodeID(fileID, containingMethod);
		}
		ASTInferenceTextRecorder.recordASTOperation(operationKind, affectedNode, newText, ASTHelper.getPersistentNodeID(fileID, affectedNode), containingMethodPersistentID,
				containingMethodLinesCount, containingMethodCyclomaticComplexity, containingMethodName);
	}

	public void recordASTOperationForDeletedResource(IResource deletedResource, boolean success) {
		if (success) {
			cyclomaticComplexityCalculator.resetCache();
			String deletedResourceID= ResourceHelper.getPortableResourcePath(deletedResource);
			Map<String, Set<ASTNode>> nodesToDelete= ASTHelper.getASTNodesFromAllDeletedFiles(deletedResourceID);
			for (Entry<String, Set<ASTNode>> fileNodesToDelete : nodesToDelete.entrySet()) {
				recordDeleteASTOperation(fileNodesToDelete.getKey(), fileNodesToDelete.getValue());
			}
		}
	}

	public void recordASTOperationForMovedResource(IResource movedResource, IPath destination, boolean success) {
		if (success) {
			ASTHelper.updateFilePersistentNodeIDsMapping(ResourceHelper.getPortableResourcePath(movedResource), destination.toPortableString());
		}
	}

	public void recordASTOperationForCopiedResource(IResource copiedResource, IPath destination, boolean success) {
		if (success) {
			cyclomaticComplexityCalculator.resetCache();
			String copiedResourcePath= ResourceHelper.getPortableResourcePath(copiedResource);
			String destinationPath= destination.toPortableString();
			Set<IFile> containedJavaFiles;
			try {
				containedJavaFiles= ResourceHelper.getContainedJavaFiles(copiedResource);
			} catch (CoreException e) {
				throw new RuntimeException("Could not get contained Java files for resource: " + copiedResourcePath, e);
			}
			for (IFile containedJavaFile : containedJavaFiles) {
				String fileID= ResourceHelper.getPortableResourcePath(containedJavaFile).replaceFirst(copiedResourcePath, destinationPath);
				addAllNodesFromJavaFile(fileID, containedJavaFile);
			}
		}
	}

	public void recordASTOperationForCreatedResource(IResource createdResource, boolean success) {
		if (success && createdResource instanceof IFile) {
			cyclomaticComplexityCalculator.resetCache();
			IFile createdFile= (IFile)createdResource;
			if (ResourceHelper.isJavaFile(createdFile)) {
				addAllNodesFromJavaFile(ResourceHelper.getPortableResourcePath(createdFile), createdFile);
			}
		}
	}

	private void addAllNodesFromJavaFile(String fileID, IFile javaFile) {
		ASTParser parser= createParser();
		parser.setSource(ResourceHelper.readFileContent(javaFile).toCharArray());
		ChildrenNodesFinder childrenNodesFinder= new ChildrenNodesFinder(parser.createAST(null));
		Set<ASTNode> childrenNodes= childrenNodesFinder.getChildrenNodes();
		recordAddASTOperation(fileID, childrenNodes);
	}

	private CoveredNodesFinder getAffectedNodesFinder(String message, String source, int offset, int length) {
		ASTParser parser= createParser();
		parser.setSource(source.toCharArray());
		ASTNode rootNode= parser.createAST(null);
		CoveredNodesFinder nodeFinder= new CoveredNodesFinder(rootNode, offset, length);
		return nodeFinder;
	}

	//TODO: Should the parser be created once and then just reused?
	private ASTParser createParser() {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setStatementsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		//Avoid resolving bindings to speed up the parsing.
		parser.setResolveBindings(false);
		parser.setBindingsRecovery(false);
		return parser;
	}

}
