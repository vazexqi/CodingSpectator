/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

/**
 * 
 * @author Stas Negara
 * 
 */
public class CoherentTextChange {

	private final String oldDocumentText;

	private String newDocumentText;

	private final int offset;

	private int removedTextLength;

	private int initialRemovedTextLength;

	private int intermediateRemovedTextLength;

	private int addedTextLength;

	private boolean isFirstGluing= true;

	private boolean isDeletingOnly= false;

	private boolean isBackspaceDeleting= false;

	public CoherentTextChange(String oldDocumentText, int offset, int removedTextLength, int addedTextLength) {
		this.oldDocumentText= oldDocumentText;
		this.offset= offset;
		initialRemovedTextLength= removedTextLength;
		intermediateRemovedTextLength= 0;
		this.addedTextLength= addedTextLength;
		if (removedTextLength > 0 && addedTextLength == 0) {
			isDeletingOnly= true;
		}
	}

	public String getOldDocumentText() {
		return oldDocumentText;
	}

	public String getNewDocumentText() {
		return newDocumentText;
	}

	public int getOffset() {
		return offset;
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

	public boolean isActualChange() {
		return getRemovedTextLength() != 0 || getAddedTextLength() != 0;
	}

	public void updateNewDocumentText(String newDocumentText) {
		this.newDocumentText= newDocumentText;
	}

	public void glueNewTextChange(int newOffset, int newRemovedTextLength, int newAddedTextLength) {
		if (!shouldGlueNewTextChange(newOffset, newRemovedTextLength)) {
			throw new RuntimeException("Should not call glueNewTextChange for an incoherent change offset: " + newOffset);
		}
		if (isDeletingOnly) {
			removedTextLength+= newRemovedTextLength;
		} else {
			intermediateRemovedTextLength+= newRemovedTextLength;
		}
		addedTextLength+= newAddedTextLength;
		isFirstGluing= false;
	}

	public boolean shouldGlueNewTextChange(int newOffset, int newRemovedTextLength) {
		if (newRemovedTextLength == 0) {
			isDeletingOnly= false;
		}
		if (isDeletingOnly && isFirstGluing) {
			isBackspaceDeleting= offset != newOffset;
		}
		if (isDeletingOnly && !isBackspaceDeleting) {
			//System.out.println("To glue: " + (offset == newOffset));
			return offset == newOffset;
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
			return offset - removedTextLength - intermediateRemovedTextLength + addedTextLength == newOffset + newRemovedTextLength;
		}
	}
}
