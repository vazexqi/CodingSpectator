/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class TextChangeOperation extends UserOperation {

	private String replacedText;

	private String newText;

	private int offset;

	private int length;

	public TextChangeOperation() {
		super();
	}

	public TextChangeOperation(TextEvent textEvent) {
		super();
		replacedText= textEvent.getReplacedText() == null ? "" : textEvent.getReplacedText();
		//Use DocumentEvent to get correct, file-based offsets (which do not depend on expanding/collapsing of import statements,methods,etc.)
		DocumentEvent documentEvent= textEvent.getDocumentEvent(); //should never be null in this method
		newText= documentEvent.getText();
		offset= documentEvent.getOffset();
		length= documentEvent.getLength();
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(replacedText);
		textChunk.append(newText);
		textChunk.append(offset);
		textChunk.append(length);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		replacedText= operationLexer.getNextLexeme();
		newText= operationLexer.getNextLexeme();
		offset= Integer.valueOf(operationLexer.getNextLexeme());
		length= Integer.valueOf(operationLexer.getNextLexeme());
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Replaced text: " + replacedText + "\n");
		sb.append("New text: " + newText + "\n");
		sb.append("Offset: " + offset + "\n");
		sb.append("Length: " + length + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
