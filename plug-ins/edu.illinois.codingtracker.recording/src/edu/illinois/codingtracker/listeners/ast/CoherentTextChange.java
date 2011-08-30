/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.ast;

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

	private int addedTextLength;

	public CoherentTextChange(String oldDocumentText, int offset, int removedTextLength, int addedTextLength) {
		this.oldDocumentText= oldDocumentText;
		this.offset= offset;
		this.removedTextLength= removedTextLength;
		this.addedTextLength= addedTextLength;
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
		return removedTextLength;
	}

	public int getAddedTextLength() {
		return addedTextLength;
	}

	public void updateNewDocumentText(String newDocumentText) {
		this.newDocumentText= newDocumentText;
	}

	public void glueNewTextChange(int newOffset, int newRemovedTextLength, int newAddedTextLength) {
		if (!shouldGlueNewTextChange(newOffset)) {
			throw new RuntimeException("Should not call glueNewTextChange for an incoherent change offset: " + newOffset);
		}
		removedTextLength+= newRemovedTextLength;
		addedTextLength+= newAddedTextLength;
	}

	public boolean shouldGlueNewTextChange(int newOffset) {
		System.out.println("TO GLUE OR NOT TO GLUE");
		System.out.println("Offset=" + offset + ", removedTextLengt=" + removedTextLength + ", addedTextLength=" + addedTextLength + ", newOffset=" + newOffset);
		return offset - removedTextLength + addedTextLength == newOffset;
	}
}
