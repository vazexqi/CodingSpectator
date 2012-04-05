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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor.OperationKind;
import edu.illinois.codingtracker.operations.ast.CompositeNodeDescriptor;
import edu.illinois.codingtracker.operations.files.snapshoted.RefreshedFileOperation;
import edu.illinois.codingtracker.operations.textchanges.ConflictEditorTextChangeOperation;
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

	public static boolean isReplayingSnapshotDifference= false;

	private static volatile ASTOperationRecorder astRecorderInstance= null;

	private List<CoherentTextChange> batchTextChanges= new LinkedList<CoherentTextChange>();

	//Contains a backup of batch changes, which is set after each completed iteration.
	private List<CoherentTextChange> batchTextChangesBackup= new LinkedList<CoherentTextChange>();

	//Contains original DocumentEvents from the last, incomplete batch iteration, so it should be used only
	//when the batch contains at least one complete iteration.
	private List<DocumentEventDescriptor> batchDocumentEventsLastIterationBackup= new LinkedList<DocumentEventDescriptor>();

	private List<CoherentTextChange> problematicTextChanges= new LinkedList<CoherentTextChange>();

	private String currentEditedFilePath;

	private String currentRecordedFilePath;

	private int currentIndexToGlueWith= 0;

	private int correlatedBatchSize= -1;

	private boolean isInProblemMode= false;

	//Persist some information required for refactoring inference.

	private ASTNode lastOldRootNode;

	private ASTNode lastNewRootNode;

	private Set<ASTNode> lastAddedNodes;

	private Set<ASTNode> lastDeletedNodes;

	private Map<ASTNode, ASTNode> lastMatchedNodes;


	public static ASTOperationRecorder getInstance() {
		if (astRecorderInstance == null) {
			if (Configuration.isInASTInferenceMode || Configuration.isInRefactoringInferenceMode) {
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

	public String getCurrentRecordedFilePath() {
		return currentRecordedFilePath;
	}

	public ASTNode getLastOldRootNode() {
		return lastOldRootNode;
	}

	public ASTNode getLastNewRootNode() {
		return lastNewRootNode;
	}

	public boolean isAdded(ASTNode node) {
		return lastAddedNodes.contains(node);
	}

	public boolean isDeleted(ASTNode node) {
		return lastDeletedNodes.contains(node);
	}

	public ASTNode getNewMatch(ASTNode oldNode) {
		return lastMatchedNodes.get(oldNode);
	}

	/**
	 * Very dangerous! Should be used ONLY for batch processing to reset the state of the previous
	 * sequence!
	 */
	public void resetState() {
		resetBatch();
		currentEditedFilePath= null;
		currentRecordedFilePath= null;
		problematicTextChanges.clear();
		currentIndexToGlueWith= 0;
		isInProblemMode= false;
	}

	public void beforeDocumentChange(DocumentEvent event, String filePath) {
		if (RefreshedFileOperation.isReplaying) {
			//ignore
			return;
		}
		//If we start to edit a different file, flush the accumulated changes.
		if (currentEditedFilePath != null && !currentEditedFilePath.equals(filePath)) {
			flushCurrentTextChanges(true);
		}
		long timestamp= getTextChangeTimestamp();
		addNewBatchChange(event, timestamp);
		//Assign the current file path at the end to ensure that any prior flushing uses the old file path.
		currentEditedFilePath= filePath;
	}

	private void addNewBatchChange(DocumentEvent event, long timestamp) {
		if (batchTextChanges.isEmpty()) {
			batchTextChanges.add(new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp));
		} else {
			addToCurrentTextChanges(event, timestamp);
		}
	}

	private void addToCurrentTextChanges(DocumentEvent event, long timestamp) {
		if (correlatedBatchSize == -1) { //Batch size is not established yet.
			if (shouldExtendBatch(event, timestamp)) {
				batchTextChanges.add(new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp));
				processNewlyAddedBatchChange(event);
			} else {
				correlatedBatchSize= batchTextChanges.size();
				currentIndexToGlueWith= 0;
				updateBatchBackup();
				tryGluingInBatch(event, timestamp);
			}
		} else { //Batch size is already established.
			tryGluingInBatch(event, timestamp);
		}
	}

	private boolean shouldExtendBatch(DocumentEvent event, long timestamp) {
		if (Configuration.isInRefactoringInferenceMode || isReplayingSnapshotDifference) {
			return false;
		}
		CoherentTextChange newTextChange= new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp);
		for (CoherentTextChange existingBatchChange : batchTextChanges) {
			if (!existingBatchChange.isPossiblyCorrelatedWith(newTextChange) ||
					existingBatchChange.shouldGlueNewTextChange(event)) {
				return false;
			}
		}
		return true;
	}

	private void processNewlyAddedBatchChange(DocumentEvent event) {
		int newlyAddedChangeIndex= batchTextChanges.size() - 1;

		//First, apply the new change on existing changes to ensure that the final text is the same for all batch changes.
		applyTextChangeToBatch(event, newlyAddedChangeIndex);

		//Next, undo all previous batch changes on the newly added one to ensure that the initial text is the same.
		CoherentTextChange newlyAddedChange= batchTextChanges.get(newlyAddedChangeIndex);
		for (int i= 0; i < newlyAddedChangeIndex; i++) {
			newlyAddedChange.undoTextChange(batchTextChanges.get(i));
		}
	}

	private void tryGluingInBatch(DocumentEvent event, long timestamp) {
		CoherentTextChange textChangeToGlueWith= batchTextChanges.get(currentIndexToGlueWith);
		if (!Configuration.isInRefactoringInferenceMode && shouldContinueBatch(event, timestamp) && textChangeToGlueWith.shouldGlueNewTextChange(event)) {
			textChangeToGlueWith.glueNewTextChange(event);
			applyTextChangeToBatch(event, currentIndexToGlueWith);
			//Clone the original document event since its document is updated by Eclipse.
			DocumentEventDescriptor eventDescriptor= new DocumentEventDescriptor(CoherentTextChange.cloneDocumentEvent(event), ConflictEditorTextChangeOperation.isReplaying);
			batchDocumentEventsLastIterationBackup.add(eventDescriptor);
			incrementGluingIndex();
		} else {
			if (isAnythingToFlush() && isBatchIncomplete()) {
				flushBatchBackup(false, true);
			} else {
				flushCurrentTextChanges(false);
			}
			addNewBatchChange(event, timestamp);
		}
	}

	private boolean shouldContinueBatch(DocumentEvent event, long timestamp) {
		if (correlatedBatchSize > 1 && !batchDocumentEventsLastIterationBackup.isEmpty()) {
			CoherentTextChange newTextChange= new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp);
			//It is sufficient to check against a single existing change since all existing changes are correlated among
			//themselves by construction.
			DocumentEventDescriptor eventDescriptor= batchDocumentEventsLastIterationBackup.get(0);
			CoherentTextChange existingTextChange= new CoherentTextChange(eventDescriptor.documentEvent, eventDescriptor.isConflictEditorChange, timestamp);
			return newTextChange.isPossiblyCorrelatedWith(existingTextChange);
		}
		return true;
	}

	private void incrementGluingIndex() {
		currentIndexToGlueWith++;
		//If we reached the end of a batch, reset the gluing index and update the batch backup.
		if (currentIndexToGlueWith == correlatedBatchSize) {
			currentIndexToGlueWith= 0;
			updateBatchBackup();
		}
	}

	private void updateBatchBackup() {
		//It makes sense to backup the batch only if it has more than one element since otherwise it can never be malformed.
		if (correlatedBatchSize > 1) {
			batchDocumentEventsLastIterationBackup.clear();
			batchTextChangesBackup.clear();
			for (CoherentTextChange textChange : batchTextChanges) {
				batchTextChangesBackup.add(textChange.clone());
			}
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

	public void flushCurrentTextChanges(boolean isForced) {
		if (isAnythingToFlush()) {
			if (isBatchIncomplete()) {
				flushBatchBackup(isForced, false);
			} else {
				checkBatchedEditIsWellFormed();
				if (batchTextChanges.size() > 1) {
					flushBatchAsSeparateChanges(isForced);
				} else {
					flushSingleBatchTextChange(isForced);
				}
			}
		}
		resetBatch();
	}

	private void resetBatch() {
		batchTextChanges.clear();
		batchTextChangesBackup.clear();
		batchDocumentEventsLastIterationBackup.clear();
		correlatedBatchSize= -1;
	}

	/**
	 * This method assumes that batchTextChanges contains 0 or 1 text changes, and 0 is allowed only
	 * when the inference is in the problem mode.
	 * 
	 * @param isForced
	 */
	private void flushSingleBatchTextChange(boolean isForced) {
		CoherentTextChange lastTextChange= batchTextChanges.size() == 1 ? batchTextChanges.get(0) : null;
		if (!Configuration.isInRefactoringInferenceMode && lastTextChange != null &&
				!lastTextChange.isConflictEditorChange()) {
			ASTInferenceTextRecorder.record(lastTextChange.createTextChangeOperation(), false);
		}
		if (isInProblemMode) {
			flushProblematicTextChanges(isForced);
		} else {
			ASTOperationInferencer astOperationInferencer= new ASTOperationInferencer(lastTextChange);
			if (isForcedOrSafeFlushing(isForced, astOperationInferencer)) {
				inferAndRecordASTOperations(astOperationInferencer);
			} else {
				enterInProblemMode();
			}
		}
	}

	private boolean isForcedOrSafeFlushing(boolean isForced, ASTOperationInferencer astOperationInferencer) {
		return isForced || !astOperationInferencer.isProblematicInference();
	}

	private void flushBatchBackup(boolean isForced, boolean isGluingFlush) {
		//First, make a copy of the backed up document events since each flushing cleans the field 
		//batchDocumentEventsLastIterationBackup.
		List<DocumentEventDescriptor> documentEventsBackup= new LinkedList<DocumentEventDescriptor>();
		documentEventsBackup.addAll(batchDocumentEventsLastIterationBackup);
		//Next, flush the complete batch backup.
		batchTextChanges.clear();
		batchTextChanges.addAll(batchTextChangesBackup);
		currentIndexToGlueWith= 0;
		flushCurrentTextChanges(isForced);
		//Finally, process anew the document events from the last, incomplete batch iteration.
		boolean currentIsReplaying= ConflictEditorTextChangeOperation.isReplaying;
		for (DocumentEventDescriptor eventDescriptor : documentEventsBackup) {
			ConflictEditorTextChangeOperation.isReplaying= eventDescriptor.isConflictEditorChange;
			beforeDocumentChange(eventDescriptor.documentEvent, currentEditedFilePath);
		}
		ConflictEditorTextChangeOperation.isReplaying= currentIsReplaying;
		if (!isGluingFlush) {
			flushCurrentTextChanges(isForced);
		}
	}

	private void flushBatchAsSeparateChanges(boolean isForced) {
		//Make a copy of the batch text changes since each flushing cleans the field batchTextChanges.
		List<CoherentTextChange> batchChangesToFlush= new LinkedList<CoherentTextChange>();
		batchChangesToFlush.addAll(batchTextChanges);
		batchTextChanges.clear();

		String changedText= batchChangesToFlush.get(0).getInitialDocumentText();
		for (int i= 0; i < batchChangesToFlush.size(); i++) {
			CoherentTextChange separateChange= createSeparateChange(batchChangesToFlush, i, changedText);
			batchTextChanges.add(separateChange);
			flushCurrentTextChanges(isForced);
			//The initial text of the subsequent change is the final text of the previous change.
			changedText= separateChange.getFinalDocumentText();
		}
	}

	private CoherentTextChange createSeparateChange(List<CoherentTextChange> batchChanges, int batchChangeIndex,
													String initialText) {
		CoherentTextChange batchChange= batchChanges.get(batchChangeIndex);
		int removedTextLength= batchChange.getRemovedTextLength();
		String addedText= batchChange.getAddedText();
		int adjustedOffset= getAdjustedOffset(batchChanges, batchChangeIndex);
		Document editedDocument= new Document(initialText);
		DocumentEvent documentEvent= new DocumentEvent(editedDocument, adjustedOffset, removedTextLength, addedText);
		return new CoherentTextChange(documentEvent, batchChange.isConflictEditorChange(), batchChange.getTimestamp());
	}

	private int getAdjustedOffset(List<CoherentTextChange> batchChanges, int adjustedChangeIndex) {
		CoherentTextChange adjustedChange= batchChanges.get(adjustedChangeIndex);
		int adjustedOffset= adjustedChange.getOffset();
		//Adjust the offset to account for the previous changes.
		for (int i= 0; i < adjustedChangeIndex; i++) {
			if (adjustedChange.getOffset() > batchChanges.get(i).getOffset()) {
				//Delta text length is the same for all changes in a batch.
				adjustedOffset+= adjustedChange.getDeltaTextLength();
			}
		}
		return adjustedOffset;
	}

	private void enterInProblemMode() {
		isInProblemMode= true;
		//The only batch text change becomes the first problematic text change.
		problematicTextChanges.add(batchTextChanges.get(0));
	}

	private void flushProblematicTextChanges(boolean isForced) {
		if (batchTextChanges.size() == 1) {
			problematicTextChanges.add(batchTextChanges.get(0));
		}
		ASTOperationInferencer astOperationInferencer= new ASTOperationInferencer(problematicTextChanges);

		if (isForcedOrSafeFlushing(isForced, astOperationInferencer)) {
			inferAndRecordASTOperations(astOperationInferencer);
			problematicTextChanges.clear();
			isInProblemMode= false;
		}
	}

	private boolean isAnythingToFlush() {
		return isInProblemMode || !batchTextChanges.isEmpty() && batchTextChanges.get(0).isActualChange();
	}

	private boolean isBatchIncomplete() {
		return correlatedBatchSize > 1 && currentIndexToGlueWith != 0;
	}

	private void checkBatchedEditIsWellFormed() {
		if (isBatchIncomplete()) {
			throw new RuntimeException("Stopped gluing in the middle of a correlated batch!");
		}
		if (!batchTextChanges.isEmpty()) {
			CoherentTextChange firstChange= batchTextChanges.get(0);
			final String initialText= firstChange.getInitialDocumentText();
			final String finalText= firstChange.getFinalDocumentText();
			final String removedText= firstChange.getRemovedText();
			final String addedText= firstChange.getAddedText();
			for (CoherentTextChange batchChange : batchTextChanges) {
				if (!initialText.equals(batchChange.getInitialDocumentText()) ||
						!finalText.equals(batchChange.getFinalDocumentText()) ||
						!removedText.equals(batchChange.getRemovedText()) ||
						!addedText.equals(batchChange.getAddedText())) {
					throw new RuntimeException("Batch changes are not equivalent!");
				}
			}
		}
	}

	private void inferAndRecordASTOperations(ASTOperationInferencer astOperationInferencer) {
		lastOldRootNode= astOperationInferencer.getOldRootNode();
		lastNewRootNode= astOperationInferencer.getNewRootNode();

		astOperationInferencer.inferASTOperations();
		boolean isCommentingOrUncommenting= astOperationInferencer.isCommentingOrUncommenting();
		boolean isUndoing= astOperationInferencer.isUndoing();

		CyclomaticComplexityCalculator.resetCache();
		recordChangeASTOperations(currentEditedFilePath, astOperationInferencer.getChangedNodes(), isCommentingOrUncommenting, isUndoing);
		lastDeletedNodes= astOperationInferencer.getDeletedNodes();
		recordDeleteASTOperations(currentEditedFilePath, lastDeletedNodes, isCommentingOrUncommenting, isUndoing);

		lastMatchedNodes= astOperationInferencer.getMatchedNodes();
		ASTNodesIdentifier.updatePersistentNodeIDs(currentEditedFilePath, lastMatchedNodes, astOperationInferencer.getNewCommonCoveringNode());

		lastAddedNodes= astOperationInferencer.getAddedNodes();
		recordAddASTOperations(currentEditedFilePath, lastAddedNodes, isCommentingOrUncommenting, isUndoing);
	}

	private long getTextChangeTimestamp() {
		if (Configuration.isInReplayMode) {
			return TextChangeOperation.lastReplayedTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}

	private void recordChangeASTOperations(String filePath, Map<ASTNode, ASTNode> changedNodes, boolean isCommentingOrUncommenting, boolean isUndoing) {
		ASTOperationDescriptor operationDescriptor= new ASTOperationDescriptor(OperationKind.CHANGE, isCommentingOrUncommenting, isUndoing);
		for (Entry<ASTNode, ASTNode> mapEntry : changedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			recordASTOperation(filePath, operationDescriptor, ASTHelper.createCompositeNodeDescriptor(filePath, oldNode, newNode.toString()));
		}
	}

	/**
	 * 
	 * @param filePath
	 * @param deletedEntities - is either a set of ASTNode or IdentifiedNodeInfo.
	 */
	private void recordDeleteASTOperations(String filePath, Set<? extends Object> deletedEntities, boolean isCommentingOrUncommenting, boolean isUndoing) {
		ASTOperationDescriptor operationDescriptor= new ASTOperationDescriptor(OperationKind.DELETE, isCommentingOrUncommenting, isUndoing);
		for (Object deletedEntity : deletedEntities) {
			CompositeNodeDescriptor nodeDescriptor;
			if (deletedEntity instanceof ASTNode) {
				nodeDescriptor= ASTHelper.createCompositeNodeDescriptor(filePath, (ASTNode)deletedEntity, "");
			} else {
				nodeDescriptor= ASTHelper.createCompositeNodeDescriptor((IdentifiedNodeInfo)deletedEntity);
			}
			if (nodeDescriptor != null) { //nodeDescriptor could be null for orphan nodes in IdentifiedNodeInfo.
				recordASTOperation(filePath, operationDescriptor, nodeDescriptor);
			}
		}
		//Delete nodes after recording all delete operations to avoid scenarios, in which recording a delete operation,
		//requires a node that already was deleted (e.g. the containing method node).
		for (Object deletedEntity : deletedEntities) {
			if (deletedEntity instanceof ASTNode) {
				ASTNodesIdentifier.removePersistentNodeID(filePath, (ASTNode)deletedEntity);
			} else {
				IdentifiedNodeInfo deletedNodeInfo= (IdentifiedNodeInfo)deletedEntity;
				ASTNodesIdentifier.removePersistentNodeID(filePath, deletedNodeInfo.getNodeID(), deletedNodeInfo.getPositionalNodeID());
			}
		}
	}

	private void recordAddASTOperations(String filePath, Set<ASTNode> addedNodes, boolean isCommentingOrUncommenting, boolean isUndoing) {
		ASTOperationDescriptor operationDescriptor= new ASTOperationDescriptor(OperationKind.ADD, isCommentingOrUncommenting, isUndoing);
		for (ASTNode addedNode : addedNodes) {
			recordASTOperation(filePath, operationDescriptor, ASTHelper.createCompositeNodeDescriptor(filePath, addedNode, ""));
		}
	}

	private void recordASTOperation(String filePath, ASTOperationDescriptor operationDescriptor, CompositeNodeDescriptor affectedNodeDescriptor) {
		if (!filePath.equals(currentRecordedFilePath)) {
			currentRecordedFilePath= filePath;
			if (!Configuration.isInRefactoringInferenceMode) {
				ASTInferenceTextRecorder.recordASTFileOperation(currentRecordedFilePath);
			}
		}
		if (!Configuration.isInRefactoringInferenceMode) {
			ASTInferenceTextRecorder.recordASTOperation(operationDescriptor, affectedNodeDescriptor);
		}
	}

	public void recordASTOperationForDeletedResource(IResource deletedResource, boolean success) {
		if (success) {
			CyclomaticComplexityCalculator.resetCache();
			String deletedResourcePath= ResourceHelper.getPortableResourcePath(deletedResource);
			Map<String, Set<IdentifiedNodeInfo>> nodesToDelete= ASTNodesIdentifier.getNodeInfosFromAllDeletedFiles(deletedResourcePath);
			for (Entry<String, Set<IdentifiedNodeInfo>> fileNodesToDelete : nodesToDelete.entrySet()) {
				recordDeleteASTOperations(fileNodesToDelete.getKey(), fileNodesToDelete.getValue(), false, false);
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
				String oldFilePath= ResourceHelper.getPortableResourcePath(containedJavaFile);
				String newFilePath= StringHelper.replacePrefix(oldFilePath, copiedResourcePath, destinationPath);
				addAllNodesFromJavaFile(newFilePath, containedJavaFile);
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
		recordAddASTOperations(filePath, allNodes, false, false);
	}

	private class DocumentEventDescriptor {

		final DocumentEvent documentEvent;

		final boolean isConflictEditorChange;


		DocumentEventDescriptor(DocumentEvent documentEvent, boolean isCOnflictEditorChange) {
			this.documentEvent= documentEvent;
			this.isConflictEditorChange= isCOnflictEditorChange;
		}

	}
}
