/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.inferencing;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

import edu.illinois.codingtracker.listeners.BasicListener;
import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.UndoneTextChangeOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class CoherentTextChange implements Cloneable {

	private final long timestamp;

	//This field is not final only to allow setting it in the method 'clone'.
	private IDocument editedDocument;

	private String initialDocumentText;

	private int baseOffset;

	private int batchOffsetShift; //It is assigned only when this text change is part of a batch.

	//The length of the text that is removed from the document (i.e. NOT from the own added text) by glued DocumentEvents.
	//Thus, it does not include neither intermediateRemovedTextLength nor initialRemovedTextLength.
	private int removedTextLength;

	private int addedTextLength;

	//The length of the text that is removed as the first change, i.e. by DocumentEvent from which an instance of 
	//CoherentTextChange is created. Thus, it should be assigned only once, in the constructor of CoherentTextChange.
	private final int initialRemovedTextLength;

	//The length of the text that is removed from the own added text. Thus, it should never be greater than addedTextLength.
	private int intermediateRemovedTextLength;

	private final TextChangeOperation initialTextChangeOperation;

	private boolean isNeverGlued= true;

	private boolean isDeletingOnly= false;

	private boolean isBackspaceDeleting= false;

	private boolean isUndoing= false;

	private final boolean isConflictEditorChange;


	public CoherentTextChange(DocumentEvent documentEvent, boolean isConflictEditorChange, long timestamp) {
		this.isConflictEditorChange= isConflictEditorChange;
		isUndoing= isCurrentEventUndoing();
		batchOffsetShift= 0;
		this.timestamp= timestamp;
		initialDocumentText= documentEvent.getDocument().get();
		editedDocument= new Document(initialDocumentText);
		baseOffset= documentEvent.getOffset();
		initialRemovedTextLength= documentEvent.getLength();
		addedTextLength= documentEvent.getText().length();
		intermediateRemovedTextLength= 0;
		if (initialRemovedTextLength > 0 && addedTextLength == 0) {
			isDeletingOnly= true;
		}
		String replacedText= initialDocumentText.substring(baseOffset, baseOffset + initialRemovedTextLength);
		initialTextChangeOperation= new PerformedTextChangeOperation(documentEvent, replacedText, timestamp);
		applyTextChange(documentEvent);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getInitialDocumentText() {
		return initialDocumentText;
	}

	public String getFinalDocumentText() {
		return editedDocument.get();
	}

	/**
	 * The returned offset is always valid for the initial document. It is also valid for the final
	 * document if this text change is not part of a batch. If this text change is part of a batch,
	 * use {@link #getBatchOffset} to get the offset in the final document.
	 * 
	 * @return
	 */
	public int getOffset() {
		if (isBackspaceDeleting) {
			return baseOffset - removedTextLength;
		}
		return baseOffset;
	}

	/**
	 * It is different from {@link #getOffset()} only when this text change is part of a batch.
	 * 
	 * @return
	 */
	private int getBatchOffset() {
		return getOffset() + batchOffsetShift;
	}

	public String getRemovedText() {
		return initialDocumentText.substring(getOffset(), getOffset() + getRemovedTextLength());
	}

	public int getRemovedTextLength() {
		return initialRemovedTextLength + removedTextLength;
	}

	public int getAddedTextLength() {
		int remainingAddedTextLength= addedTextLength - intermediateRemovedTextLength;
		if (remainingAddedTextLength < 0) {
			throw new RuntimeException("The remaining added text length is negative: " + remainingAddedTextLength);
		}
		return remainingAddedTextLength;
	}

	public String getAddedText() {
		return getFinalDocumentText().substring(getBatchOffset(), getBatchOffset() + getAddedTextLength());
	}

	public int getDeltaTextLength() {
		return getAddedTextLength() - getRemovedTextLength();
	}

	public boolean isActualChange() {
		return getRemovedTextLength() != 0 || getAddedTextLength() != 0;
	}

	public boolean isNeverGlued() {
		return isNeverGlued;
	}

	/**
	 * TODO: Note that if a comment is produced by editing the code such that the edits could not be
	 * glued in a comment marker, but the resulting code is actually a comment marker (e.g. after
	 * deleting space in between '/' and '*'), then we would incorrectly consider that this is NOT a
	 * commenting/uncommenting change. Nevertheless, the current implementation correctly handles
	 * the most common scenarios of commenting/uncommenting the code.
	 * 
	 * Also, note that this code is not meant to detect when comments are added/removed, but rather
	 * when the existing code is commented or uncommented (i.e. the only change is adding/removing
	 * the comment markers).
	 * 
	 * @return
	 */
	public boolean isCommentingOrUncommenting() {
		return isCommentMarker(getAddedText()) || isCommentMarker(getRemovedText());
	}

	private boolean isCommentMarker(String str) {
		return str.equals("//") || str.equals("/*") || str.equals("*/");
	}

	public boolean isUndoing() {
		return isUndoing;
	}

	public boolean isConflictEditorChange() {
		return isConflictEditorChange;
	}

	/**
	 * Can be called only before this and the given CoherentTextChange are glued the first time.
	 * 
	 * @param textChange
	 * @return
	 */
	public boolean isPossiblyCorrelatedWith(CoherentTextChange textChange) {
		checkBothNeverGlued(textChange, "isPossiblyCorrelatedWith");
		return initialTextChangeOperation.isPossiblyCorrelatedWith(textChange.initialTextChangeOperation);
	}

	public void glueNewTextChange(DocumentEvent documentEvent) {
		if (!shouldGlueNewTextChange(documentEvent)) {
			throw new RuntimeException("Should not call glueNewTextChange for an incoherent change offset: " + documentEvent.getOffset());
		}
		//All glued events should be undone to consider the whole change as an undone change.
		isUndoing= isUndoing && isCurrentEventUndoing();
		int newRemovedTextLength= documentEvent.getLength();
		if (isDeletingOnly) {
			removedTextLength+= newRemovedTextLength;
		} else {
			intermediateRemovedTextLength+= newRemovedTextLength;
		}
		addedTextLength+= documentEvent.getText().length();
		//Updating the deleting state should follow the adjusting of addedTextLength.
		updateDeletingState(documentEvent.getOffset());
		applyTextChange(documentEvent);
		isNeverGlued= false; //Set isNeverGlued at the end in order to avoid confusion in the preceding functionality.
	}

	private void updateDeletingState(int newOffset) {
		//The check to establish isBackspaceDeleting should be done before isDeletingOnly update 
		//in order to use the current value of isDeletingOnly.
		if (isNeverGlued && isDeletingOnly) {
			isBackspaceDeleting= getBatchOffset() != newOffset;
		}
		if (addedTextLength > 0) {
			isDeletingOnly= false;
		}
	}

	public boolean shouldGlueNewTextChange(DocumentEvent documentEvent) {
		final int currentOffset= getBatchOffset();
		final int newOffset= documentEvent.getOffset();
		final int newRemovedTextLength= documentEvent.getLength();
		if (isNeverGlued && isDeletingOnly && currentOffset == newOffset) {
			return true;
		}
		if (isDeletingOnly && !isBackspaceDeleting && !isNeverGlued) {
			return currentOffset == newOffset;
		}
		//TODO: Consider also cases when a developer adds several nodes, then reconsiders and deletes all of them.
		//Currently, we discard these additions and deletions. We need to introduce either a threshold on the number
		//of allowed deletions or the deletion of one or more AST nodes, at which point we need to treat the addition and
		//deletion as two distinct operations. Thus, we would need to capture the document's text at the moment of reversal
		//(i.e. when a developer starts to delete the recently added characters).
		if (!isDeletingOnly && addedTextLength - intermediateRemovedTextLength - newRemovedTextLength < 0) {
			return false;
		}
		return currentOffset + addedTextLength - intermediateRemovedTextLength == newOffset + newRemovedTextLength;
	}

	public void applyTextChange(DocumentEvent documentEvent) {
		final int changeOffset= documentEvent.getOffset();
		final int removedTextLength= documentEvent.getLength();
		final String addedText= documentEvent.getText();
		try {
			editedDocument.replace(changeOffset, removedTextLength, addedText);
		} catch (BadLocationException e) {
			throw new RuntimeException("Could not apply text change to the edited document of a CoherentTextChange!", e);
		}
		//Note that for own updates (e.g. constructor or gluing) getBatchOffset() == changeOffset, so nothing will get updated.
		if (getBatchOffset() > changeOffset) {
			batchOffsetShift+= addedText.length() - removedTextLength;
		}
	}

	/**
	 * Can be called only in the batch mode and before this and the given CoherentTextChange are
	 * glued the first time.
	 * 
	 * @param textChange
	 * @return
	 */
	public void undoTextChange(CoherentTextChange textChange) {
		checkBothNeverGlued(textChange, "undoTextChange");
		initialDocumentText= textChange.getInitialDocumentText();
		if (getBatchOffset() > textChange.getBatchOffset()) {
			baseOffset-= textChange.getDeltaTextLength();
			batchOffsetShift+= textChange.getDeltaTextLength();
		}
	}

	private void checkBothNeverGlued(CoherentTextChange textChange, String checkingMethodName) {
		if (!isNeverGlued || !textChange.isNeverGlued) {
			throw new RuntimeException("It is not valid to call the method \"" + checkingMethodName + "\" if at least one argument represents an already glued text change!");
		}
	}

	private boolean isCurrentEventUndoing() {
		//The first part is for online AST inferencing, the second part is for AST inferencing while replaying.
		return BasicListener.isUndoing || UndoneTextChangeOperation.isReplaying;
	}

	public PerformedTextChangeOperation createTextChangeOperation() {
		Document editedDocument= new Document(getInitialDocumentText());
		DocumentEvent documentEvent= new DocumentEvent(editedDocument, getOffset(), getRemovedTextLength(), getAddedText());
		return new PerformedTextChangeOperation(documentEvent, getRemovedText(), getTimestamp());
	}

	@Override
	public CoherentTextChange clone() {
		CoherentTextChange coherentTextChangeClone= null;
		try {
			coherentTextChangeClone= (CoherentTextChange)super.clone();
		} catch (Exception e) {
			//Should never get here.
			throw new RuntimeException("Failed to clone coherent text change: " + this);
		}
		//Field editedDocumet is the only field that requires deep copying since all other fields are primitive, immutable, 
		//or never mutated (the only mutable but never mutated field is initialTextChangeOperation).
		coherentTextChangeClone.editedDocument= new Document(editedDocument.get());
		return coherentTextChangeClone;
	}

	public static DocumentEvent cloneDocumentEvent(DocumentEvent event) {
		Document document= new Document(event.getDocument().get());
		return new DocumentEvent(document, event.getOffset(), event.getLength(), event.getText());
	}

}
