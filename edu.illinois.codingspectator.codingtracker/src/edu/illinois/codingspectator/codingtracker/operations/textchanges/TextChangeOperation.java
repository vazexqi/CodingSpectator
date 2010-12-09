/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class TextChangeOperation extends UserOperation {

	private final TextEvent textEvent;

	public TextChangeOperation(TextEvent textEvent) {
		super();
		this.textEvent= textEvent;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		String replacedText= textEvent.getReplacedText() == null ? "" : textEvent.getReplacedText();
		textChunk.append(replacedText);
		//Use DocumentEvent to get correct, file-based offsets (which do not depend on expanding/collapsing of import statements,methods,etc.)
		DocumentEvent documentEvent= textEvent.getDocumentEvent(); //should never be null in this method
		textChunk.append(documentEvent.getText());
		textChunk.append(documentEvent.getOffset());
		textChunk.append(documentEvent.getLength());
	}

}
