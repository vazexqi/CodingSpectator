/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.inferencing;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class CoherentTextChange {

	private final long timestamp;

	private final IDocument editedDocument;

	private final String initialDocumentText;

	private int baseOffset;

	private int removedTextLength;

	private int addedTextLength;

	private int initialRemovedTextLength;

	private int intermediateRemovedTextLength;

	private TextChangeOperation initialTextChangeOperation;

	private boolean neverGlued= true;

	private boolean isDeletingOnly= false;

	private boolean isBackspaceDeleting= false;


	public CoherentTextChange(DocumentEvent documentEvent, long timestamp) {
		this.timestamp= timestamp;
		initialDocumentText= documentEvent.getDocument().get();
		editedDocument= new Document(initialDocumentText);
		initialTextChangeOperation= createTextChangeOperation(documentEvent);
		baseOffset= documentEvent.getOffset();
		initialRemovedTextLength= documentEvent.getLength();
		String addedText= documentEvent.getText();
		addedTextLength= addedText.length();
		intermediateRemovedTextLength= 0;
		if (initialRemovedTextLength > 0 && addedTextLength == 0) {
			isDeletingOnly= true;
		}
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

	public int getOffset() {
		if (isBackspaceDeleting) {
			return baseOffset - removedTextLength;
		}
		return baseOffset;
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

	public int getDeltaTextLength() {
		return getAddedTextLength() - getRemovedTextLength();
	}

	public boolean isActualChange() {
		return getRemovedTextLength() != 0 || getAddedTextLength() != 0;
	}

	public boolean isNeverGlued() {
		return neverGlued;
	}

	/**
	 * Should be called before this and the given CoherentTextChange are glued the first time.
	 * 
	 * @param textChange
	 * @return
	 */
	public boolean isPossiblyCorrelatedWith(CoherentTextChange textChange) {
		if (!neverGlued || !textChange.neverGlued) {
			throw new RuntimeException("It is not valid to call the method \"isPossiblyCorrelatedWith\" if at least one argument represents an already glued text change!");
		}
		return initialTextChangeOperation.isPossiblyCorrelatedWith(textChange.initialTextChangeOperation);
	}

	public void glueNewTextChange(DocumentEvent documentEvent, long textChangeTimestamp) {
		if (!shouldGlueNewTextChange(documentEvent)) {
			throw new RuntimeException("Should not call glueNewTextChange for an incoherent change offset: " + documentEvent.getOffset());
		}
		int newRemovedTextLength= documentEvent.getLength();
		if (isDeletingOnly) {
			removedTextLength+= newRemovedTextLength;
		} else {
			intermediateRemovedTextLength+= newRemovedTextLength;
		}
		addedTextLength+= documentEvent.getText().length();
		neverGlued= false;
		applyTextChange(documentEvent);
	}

	public boolean shouldGlueNewTextChange(DocumentEvent documentEvent) {
		int newOffset= documentEvent.getOffset();
		int newRemovedTextLength= documentEvent.getLength();
		if (newRemovedTextLength == 0) {
			isDeletingOnly= false;
		}
		if (isDeletingOnly && neverGlued) {
			isBackspaceDeleting= baseOffset != newOffset;
		}
		if (isDeletingOnly && !isBackspaceDeleting) {
			//System.out.println("To glue: " + (offset == newOffset));
			return baseOffset == newOffset;
		} else {
			//TODO: Consider also cases when a developer adds several nodes, then reconsiders and deletes all of them.
			//Currently, we discard these additions and deletions. We need to introduce either a threshold on the number
			//of allowed deletions or the deletion of one or more AST nodes, at which point we need to treat the addition and
			//deletion as two distinct operations. Thus, we would need to capture the document's text at the moment of reversal
			//(i.e. when a developer starts to delete the recently added characters).
			if (!isDeletingOnly && addedTextLength - intermediateRemovedTextLength - newRemovedTextLength < 0) {
				//System.out.println("To glue: false");
				return false;
			}
			//System.out.println("To glue: " + (offset - removedTextLength - intermediateRemovedTextLength + addedTextLength == newOffset + newRemovedTextLength));
			return baseOffset - removedTextLength - intermediateRemovedTextLength + addedTextLength == newOffset + newRemovedTextLength;
		}
	}

	public void applyTextChange(DocumentEvent documentEvent) {
		int changeOffset= documentEvent.getOffset();
		int removedTextLength= documentEvent.getLength();
		String addedText= documentEvent.getText();
		try {
			editedDocument.replace(changeOffset, removedTextLength, addedText);
		} catch (BadLocationException e) {
			throw new RuntimeException("Could not apply text change to the edited document of a CoherentTextChange!", e);
		}
		//Note that for own updates (e.g. constructor or gluing) getOffset() == changeOffset, so nothing will get updated.
		if (getOffset() > changeOffset) {
			baseOffset+= addedText.length() - removedTextLength;
		}
	}

	private TextChangeOperation createTextChangeOperation(DocumentEvent documentEvent) {
		String replacedText;
		try {
			replacedText= editedDocument.get(documentEvent.getOffset(), documentEvent.getLength());
		} catch (BadLocationException e) {
			throw new RuntimeException("Could not get the replaced text: " + documentEvent, e);
		}
		return new PerformedTextChangeOperation(documentEvent, replacedText, timestamp);
	}

}
