/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

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
import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ASTInferenceTextRecorder;

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

	private List<CoherentTextChange> currentTextChanges= new LinkedList<CoherentTextChange>();

	private IDocument currentDocument;

	private String currentEditedFilePath;

	private String currentRecordedFilePath;

	private int currentIndexToGlueWith= 0;

	private int correlatedBatchSize= -1;

	private final CyclomaticComplexityCalculator cyclomaticComplexityCalculator= new CyclomaticComplexityCalculator();

	private boolean isInProblemMode= false;

	private String snapshotBeforeASTProblems;


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
		if (currentTextChanges.isEmpty()) {
			currentTextChanges.add(new CoherentTextChange(event, timestamp));
		} else {
			addToCurrentTextChanges(event, timestamp);
		}
		currentEditedFilePath= filePath;
	}

	private void addToCurrentTextChanges(DocumentEvent event, long timestamp) {
		if (correlatedBatchSize == -1) { //Batch size is not established yet.
			CoherentTextChange lastTextChange= currentTextChanges.get(currentTextChanges.size() - 1);
			CoherentTextChange newTextChange= new CoherentTextChange(event, timestamp);
			if (!isReplayingSnapshotDifference && lastTextChange.isFirstGluing() &&
					lastTextChange.isPossiblyCorrelatedWith(newTextChange)) {
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
			flushCurrentTextChanges(false);
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

	public void flushCurrentTextChanges(boolean isForced) {
		if (isAnythingToFlush()) {
			checkBatchedEditIsCompleted();
			if (isInProblemMode) {
				flushProblematicTextChanges(isForced);
			} else {
				CoherentTextChange firstTextChange= currentTextChanges.get(0);
				ASTOperationInferencer astOperationInferencer= new ASTOperationInferencer(currentTextChanges.size(), firstTextChange);
				//Perform AST inference when forced or AST inference is not problematic.
				if (isForced || !astOperationInferencer.isProblematicInference()) {
					inferAndRecordASTOperations(astOperationInferencer);
				} else {
					isInProblemMode= true;
					snapshotBeforeASTProblems= firstTextChange.getInitialDocumentText();
				}
			}
		}
		currentTextChanges.clear();
		correlatedBatchSize= -1;
	}

	private void flushProblematicTextChanges(boolean isForced) {
		String finalDocumentText= currentTextChanges.get(0).getFinalDocumentText();
		//Perform AST inference when forced or AST is no longer problematic.
		if (isForced || !ASTHelper.isProblematicAST(finalDocumentText)) {
			String currentSnapshot= snapshotBeforeASTProblems;
			for (PerformedTextChangeOperation textChangeOperation : SnapshotDifferenceCalculator.getSnapshotDifference(snapshotBeforeASTProblems, finalDocumentText, -1)) {
				CoherentTextChange coherentTextChange= new CoherentTextChange(textChangeOperation.getDocumentEvent(currentSnapshot), getTextChangeTimestamp());
				ASTOperationInferencer astOperationInferencer= new ASTOperationInferencer(1, coherentTextChange);
				inferAndRecordASTOperations(astOperationInferencer);
				currentSnapshot= coherentTextChange.getFinalDocumentText();
			}
			isInProblemMode= false;
		}
	}

	private boolean isAnythingToFlush() {
		return !currentTextChanges.isEmpty() && currentTextChanges.get(0).isActualChange();
	}

	private void checkBatchedEditIsCompleted() {
		if (correlatedBatchSize != -1 && currentIndexToGlueWith != 0) {
			throw new RuntimeException("Stopped gluing in the middle of a correlated batch!");
		}
	}

	private void inferAndRecordASTOperations(ASTOperationInferencer astOperationInferencer) {
		astOperationInferencer.inferASTOperations();

		cyclomaticComplexityCalculator.resetCache();
		recordChangeASTOperations(currentEditedFilePath, astOperationInferencer.getChangedNodes());
		recordDeleteASTOperations(currentEditedFilePath, astOperationInferencer.getDeletedNodes());

		//Update the persistent IDs after delete, but before add. Also, do it as an atomic operation.
		ASTNodesIdentifier.updatePersistentNodeIDs(currentEditedFilePath, astOperationInferencer.getMatchedNodes());

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
		for (ASTNode deletedNode : deletedNodes) {
			recordASTOperation(filePath, OperationKind.DELETE, deletedNode, "");
		}
		//Delete nodes after recording all delete operations to avoid scenarios, in which recording a delete operation,
		//requires a node that already was deleted (e.g. the containing method node).
		for (ASTNode deletedNode : deletedNodes) {
			ASTNodesIdentifier.removePersistentNodeID(filePath, deletedNode);
		}
	}

	private void recordAddASTOperations(String filePath, Set<ASTNode> addedNodes) {
		for (ASTNode addedNode : addedNodes) {
			recordASTOperation(filePath, OperationKind.ADD, addedNode, "");
		}
	}

	private void recordASTOperation(String filePath, OperationKind operationKind, ASTNode affectedNode, String newText) {
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
			cyclomaticComplexityCalculator.resetCache();
			String deletedResourcePath= ResourceHelper.getPortableResourcePath(deletedResource);
			Map<String, Set<ASTNode>> nodesToDelete= ASTNodesIdentifier.getASTNodesFromAllDeletedFiles(deletedResourcePath);
			for (Entry<String, Set<ASTNode>> fileNodesToDelete : nodesToDelete.entrySet()) {
				recordDeleteASTOperations(fileNodesToDelete.getKey(), fileNodesToDelete.getValue());
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
				String filePath= ResourceHelper.getPortableResourcePath(containedJavaFile).replaceFirst(copiedResourcePath, destinationPath);
				addAllNodesFromJavaFile(filePath, containedJavaFile);
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

	private void addAllNodesFromJavaFile(String filePath, IFile javaFile) {
		Set<ASTNode> allNodes= ASTHelper.getAllNodesFromText(ResourceHelper.readFileContent(javaFile));
		recordAddASTOperations(filePath, allNodes);
	}

}
