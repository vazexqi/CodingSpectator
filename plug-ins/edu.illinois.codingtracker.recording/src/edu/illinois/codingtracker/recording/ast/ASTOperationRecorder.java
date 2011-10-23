/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.ast.ASTOperation.OperationKind;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ASTInferenceTextRecorder;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.helpers.CyclomaticComplexityCalculator;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;
import edu.illinois.codingtracker.recording.ast.identification.IdentifiedNodeInfo;
import edu.illinois.codingtracker.recording.ast.inferencing.ASTOperationInferencer;
import edu.illinois.codingtracker.recording.ast.inferencing.CoherentTextChange;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperationRecorder {

	public static final boolean isInASTInferenceMode= System.getenv("AST_INFERENCE_MODE") != null;

	public static final boolean isInReplayMode= System.getenv("REPLAY_MODE") != null;

	public static boolean isReplayingSnapshotDifference= false;

	private static volatile ASTOperationRecorder astRecorderInstance= null;

	private List<CoherentTextChange> batchTextChanges= new LinkedList<CoherentTextChange>();

	private List<CoherentTextChange> cachedTextChanges= new LinkedList<CoherentTextChange>();

	private List<CoherentTextChange> problematicTextChanges= new LinkedList<CoherentTextChange>();

	private IDocument currentDocument;

	private String currentEditedFilePath;

	private String currentRecordedFilePath;

	private int currentIndexToGlueWith= 0;

	private int correlatedBatchSize= -1;

	private boolean isInProblemMode= false;


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

	public void beforeDocumentChange(DocumentEvent event, String filePath) {
		//If the edited document has changed, flush the accumulated changes.
		if (currentDocument != null && currentDocument != event.getDocument()) {
			flushCurrentTextChanges(true);
		}
		currentDocument= event.getDocument();
		long timestamp= getTextChangeTimestamp();
		if (batchTextChanges.isEmpty()) {
			addNewCoherentTextChange(event, timestamp);
		} else {
			addToCurrentTextChanges(event, timestamp);
		}
		currentEditedFilePath= filePath;
	}

	private void addToCurrentTextChanges(DocumentEvent event, long timestamp) {
		if (correlatedBatchSize == -1) { //Batch size is not established yet.
			CoherentTextChange lastTextChange= batchTextChanges.get(batchTextChanges.size() - 1);
			CoherentTextChange newTextChange= new CoherentTextChange(event, timestamp);
			if (!isReplayingSnapshotDifference && lastTextChange.isNeverGlued() &&
					lastTextChange.isPossiblyCorrelatedWith(newTextChange)) {
				addNewCoherentTextChange(event, timestamp);
				applyTextChangeToBatch(event, batchTextChanges.size() - 1);
			} else {
				correlatedBatchSize= batchTextChanges.size();
				currentIndexToGlueWith= 0;
				tryGluingInBatch(event, timestamp);
			}
		} else { //Batch size is already established.
			tryGluingInBatch(event, timestamp);
		}
	}

	private void tryGluingInBatch(DocumentEvent event, long timestamp) {
		CoherentTextChange textChangeToGlueWith= batchTextChanges.get(currentIndexToGlueWith);
		if (textChangeToGlueWith.shouldGlueNewTextChange(event)) {
			textChangeToGlueWith.glueNewTextChange(event, timestamp);
			applyTextChangeToBatch(event, currentIndexToGlueWith);
			currentIndexToGlueWith++;
			if (currentIndexToGlueWith == correlatedBatchSize) {
				currentIndexToGlueWith= 0;
			}
		} else {
			flushCurrentTextChanges(false);
			addNewCoherentTextChange(event, timestamp);
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
		for (int i= 0; i < batchTextChanges.size(); i++) {
			if (i != excludeIndex) {
				batchTextChanges.get(i).applyTextChange(event);
			}
		}
	}

	private void addNewCoherentTextChange(DocumentEvent event, long timestamp) {
		//Add two distinct CoherentTextChange objects since those that are in batchTextChanges will get updated,
		//while we need to keep the original CoherentTextChange objects in cachedTextChanges.
		batchTextChanges.add(new CoherentTextChange(event, timestamp));
		cachedTextChanges.add(new CoherentTextChange(event, timestamp));
	}

	public void flushCurrentTextChanges(boolean isForced) {
		if (isAnythingToFlush()) {
			checkBatchedEditIsCompleted();
			if (isInProblemMode) {
				flushProblematicTextChanges(isForced);
			} else {
				CoherentTextChange firstTextChange= batchTextChanges.get(0);
				if (batchTextChanges.size() > 1 && firstTextChange.isNeverGlued()) {
					flushBatchAsSeparateChanges(isForced);
				} else {
					ASTOperationInferencer astOperationInferencer= new ASTOperationInferencer(batchTextChanges.size(), firstTextChange);
					//Perform AST inference when forced or AST inference is not problematic.
					if (isForced || !astOperationInferencer.isProblematicInference()) {
						inferAndRecordASTOperations(astOperationInferencer);
					} else {
						enterInProblemMode();
					}
				}
			}
		}
		batchTextChanges.clear();
		cachedTextChanges.clear();
		correlatedBatchSize= -1;
	}

	private void flushBatchAsSeparateChanges(boolean isForced) {
		batchTextChanges.clear();
		//Make a copy of the cached text changes since each flushing would clean the field cachedTextChanges.
		List<CoherentTextChange> cachedTextChanges= new LinkedList<CoherentTextChange>();
		cachedTextChanges.addAll(this.cachedTextChanges);
		for (CoherentTextChange textChange : cachedTextChanges) {
			batchTextChanges.add(textChange);
			flushCurrentTextChanges(isForced);
		}
	}

	private void enterInProblemMode() {
		if (batchTextChanges.size() != 1) {
			throw new RuntimeException("Entering in the problem mode with a wrong batch size: " + batchTextChanges.size());
		}
		isInProblemMode= true;
		//The only batch text change becomes the first problematic text change.
		problematicTextChanges.add(batchTextChanges.get(0));
	}

	private void flushProblematicTextChanges(boolean isForced) {
		if (batchTextChanges.size() > 1) {
			throw new RuntimeException("Flushing problematic changes with a wrong batch size: " + batchTextChanges.size());
		}
		if (batchTextChanges.size() == 1) {
			problematicTextChanges.add(batchTextChanges.get(0));
		}
		ASTOperationInferencer astOperationInferencer= new ASTOperationInferencer(problematicTextChanges);

		//Perform AST inference when forced or AST inference is no longer problematic.
		if (isForced || !astOperationInferencer.isProblematicInference()) {
			inferAndRecordASTOperations(astOperationInferencer);
			problematicTextChanges.clear();
			isInProblemMode= false;
		}
	}

	private boolean isAnythingToFlush() {
		return isInProblemMode || !batchTextChanges.isEmpty() && batchTextChanges.get(0).isActualChange();
	}

	private void checkBatchedEditIsCompleted() {
		if (correlatedBatchSize != -1 && currentIndexToGlueWith != 0) {
			throw new RuntimeException("Stopped gluing in the middle of a correlated batch!");
		}
	}

	private void inferAndRecordASTOperations(ASTOperationInferencer astOperationInferencer) {
		astOperationInferencer.inferASTOperations();

		CyclomaticComplexityCalculator.resetCache();
		recordChangeASTOperations(currentEditedFilePath, astOperationInferencer.getChangedNodes());
		recordDeleteASTOperations(currentEditedFilePath, astOperationInferencer.getDeletedNodes());

		//Update the persistent IDs after delete, but before add. Also, do it as an atomic operation.
		ASTNodesIdentifier.updatePersistentNodeIDs(currentEditedFilePath, astOperationInferencer.getMatchedNodes(), astOperationInferencer.getNewCommonCoveringNode());

		recordAddASTOperations(currentEditedFilePath, astOperationInferencer.getAddedNodes());
	}

	private long getTextChangeTimestamp() {
		if (isInReplayMode) {
			return TextChangeOperation.lastReplayedTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}

	private void recordChangeASTOperations(String filePath, Map<ASTNode, ASTNode> changedNodes) {
		for (Entry<ASTNode, ASTNode> mapEntry : changedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			recordASTOperation(filePath, OperationKind.CHANGE, oldNode, newNode.toString());
		}
	}

	private void recordDeleteASTOperations(String filePath, Set<ASTNode> deletedNodes) {
		Set<IdentifiedNodeInfo> deletedNodeInfos= new HashSet<IdentifiedNodeInfo>();
		for (ASTNode deletedNode : deletedNodes) {
			deletedNodeInfos.add(new IdentifiedNodeInfo(filePath, deletedNode));
		}
		recordDeleteASTOperations(filePath, deletedNodeInfos, true);
	}

	private void recordDeleteASTOperations(String filePath, Set<IdentifiedNodeInfo> deletedNodeInfos, boolean shouldGetOriginalContainingMethods) {
		for (IdentifiedNodeInfo deletedNodeInfo : deletedNodeInfos) {
			recordASTOperation(filePath, OperationKind.DELETE, deletedNodeInfo.getIdentifiedNode(), "",
								deletedNodeInfo.getContainingMethod(shouldGetOriginalContainingMethods));
		}
		//Delete nodes after recording all delete operations to avoid scenarios, in which recording a delete operation,
		//requires a node that already was deleted (e.g. the containing method node).
		for (IdentifiedNodeInfo deletedNodeInfo : deletedNodeInfos) {
			ASTNodesIdentifier.removePersistentNodeID(filePath, deletedNodeInfo.getIdentifiedNode());
		}
	}

	private void recordAddASTOperations(String filePath, Set<ASTNode> addedNodes) {
		for (ASTNode addedNode : addedNodes) {
			recordASTOperation(filePath, OperationKind.ADD, addedNode, "");
		}
	}

	private void recordASTOperation(String filePath, OperationKind operationKind, ASTNode affectedNode, String newText) {
		recordASTOperation(filePath, operationKind, affectedNode, newText, ASTHelper.getContainingMethod(affectedNode));
	}

	private void recordASTOperation(String filePath, OperationKind operationKind, ASTNode affectedNode, String newText, MethodDeclaration containingMethod) {
		String containingMethodName= "";
		long containingMethodPersistentID= -1;
		int containingMethodLinesCount= -1;
		int containingMethodCyclomaticComplexity= -1;
		if (containingMethod != null) {
			//Note that for added nodes we get lines count and cyclomatic complexity of the resulting containing method 
			//that already contains these added nodes.
			//Also, note that containingMethodLinesCount would not count lines with comments or white spaces, but would
			//count several statements on the same line as separate lines (i.e. AST node is normalized such that each statement
			//appears on a separate line, which is usually the case with the actual code as well).
			containingMethodLinesCount= (new Document(containingMethod.toString().trim())).getNumberOfLines();
			containingMethodCyclomaticComplexity= CyclomaticComplexityCalculator.getCyclomaticComplexity(containingMethod);
			containingMethodName= ASTHelper.getQualifiedMethodName(containingMethod);
			containingMethodPersistentID= ASTNodesIdentifier.getPersistentNodeID(filePath, containingMethod);
		}
		if (!filePath.equals(currentRecordedFilePath)) {
			currentRecordedFilePath= filePath;
			ASTInferenceTextRecorder.recordASTFileOperation(currentRecordedFilePath);
		}
		ASTInferenceTextRecorder.recordASTOperation(operationKind, affectedNode, newText, ASTNodesIdentifier.getPersistentNodeID(filePath, affectedNode), containingMethodPersistentID,
				containingMethodLinesCount, containingMethodCyclomaticComplexity, containingMethodName);
	}

	public void recordASTOperationForDeletedResource(IResource deletedResource, boolean success) {
		if (success) {
			CyclomaticComplexityCalculator.resetCache();
			String deletedResourcePath= ResourceHelper.getPortableResourcePath(deletedResource);
			Map<String, Set<IdentifiedNodeInfo>> nodesToDelete= ASTNodesIdentifier.getNodeInfosFromAllDeletedFiles(deletedResourcePath);
			for (Entry<String, Set<IdentifiedNodeInfo>> fileNodesToDelete : nodesToDelete.entrySet()) {
				recordDeleteASTOperations(fileNodesToDelete.getKey(), fileNodesToDelete.getValue(), false);
			}
		}
	}

	public void recordASTOperationForMovedResource(IResource movedResource, IPath destination, boolean success) {
		if (success) {
			ASTNodesIdentifier.updateFilePersistentNodeIDsMapping(ResourceHelper.getPortableResourcePath(movedResource), destination.toPortableString());
		}
	}

	public void recordASTOperationForCopiedResource(IResource copiedResource, IPath destination, boolean success) {
		if (success) {
			CyclomaticComplexityCalculator.resetCache();
			String copiedResourcePath= ResourceHelper.getPortableResourcePath(copiedResource);
			String destinationPath= destination.toPortableString();
			Set<IFile> containedJavaFiles;
			try {
				containedJavaFiles= ResourceHelper.getContainedJavaFiles(copiedResource);
			} catch (CoreException e) {
				throw new RuntimeException("Could not get contained Java files for resource: " + copiedResourcePath, e);
			}
			for (IFile containedJavaFile : containedJavaFiles) {
				String filePath= ResourceHelper.getPortableResourcePath(containedJavaFile).replaceFirst(copiedResourcePath, destinationPath);
				addAllNodesFromJavaFile(filePath, containedJavaFile);
			}
		}
	}

	public void recordASTOperationForCreatedResource(IResource createdResource, boolean success) {
		if (success && createdResource instanceof IFile) {
			CyclomaticComplexityCalculator.resetCache();
			IFile createdFile= (IFile)createdResource;
			if (ResourceHelper.isJavaFile(createdFile)) {
				addAllNodesFromJavaFile(ResourceHelper.getPortableResourcePath(createdFile), createdFile);
			}
		}
	}

	private void addAllNodesFromJavaFile(String filePath, IFile javaFile) {
		Set<ASTNode> allNodes= ASTHelper.getAllNodesFromText(ResourceHelper.readFileContent(javaFile));
		recordAddASTOperations(filePath, allNodes);
	}

}
