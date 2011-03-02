/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class TextChangeOperation extends UserOperation {

	protected String replacedText;

	protected String newText;

	protected int offset;

	protected int length;

	public TextChangeOperation() {
		super();
	}

	public TextChangeOperation(DocumentEvent documentEvent, String replacedText) {
		super();
		this.replacedText= replacedText;
		newText= documentEvent.getText();
		offset= documentEvent.getOffset();
		length= documentEvent.getLength();
	}

	protected IDocumentUndoManager getCurrentDocumentUndoManager() {
		return DocumentUndoManagerRegistry.getDocumentUndoManager(currentDocument);
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
	public void replay() throws BadLocationException, ExecutionException {
		currentViewer.revealRange(offset, length > newText.length() ? length : newText.length());
		//TODO: Would it make changes more visible?
		//currentViewer.setSelectedRange(offset, length > newText.length() ? length : newText.length());
		if (!replacedText.equals(currentDocument.get(offset, length))) {
			throw new RuntimeException("Replaced text is not present in the document: " + this);
		}
		replayTextChange();
		if (!newText.equals(currentDocument.get(offset, newText.length()))) {
			throw new RuntimeException("New text does not appear in the document: " + this);
		}
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

	protected abstract void replayTextChange() throws BadLocationException, ExecutionException;

}
