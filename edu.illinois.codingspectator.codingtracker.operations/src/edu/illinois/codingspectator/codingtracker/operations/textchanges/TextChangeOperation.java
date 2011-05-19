/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public abstract class TextChangeOperation extends UserOperation {

	protected String replacedText;

	protected String newText;

	protected int offset;

	protected int length;

	//The following fields are computed during replay, do not serialize/deserialize them!
	protected IDocument currentDocument= null;

	protected ISourceViewer currentViewer= null;

	private boolean isRecordedWhileRefactoring= false;

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
		replacedText= operationLexer.readString();
		newText= operationLexer.readString();
		offset= operationLexer.readInt();
		length= operationLexer.readInt();
	}

	@Override
	public void replay() throws BadLocationException, ExecutionException {
		if (isInTestMode && isRefactoring) {
			isRecordedWhileRefactoring= true;
		} else {
			updateCurrentState();
			currentViewer.revealRange(offset, length > newText.length() ? length : newText.length());
			currentViewer.setSelectedRange(offset, length);
			if (!replacedText.equals(currentDocument.get(offset, length))) {
				throw new RuntimeException("Replaced text is not present in the document: " + this);
			}
			replayTextChange();
			currentViewer.setSelectedRange(offset, newText.length());
			if (!newText.equals(currentDocument.get(offset, newText.length()))) {
				throw new RuntimeException("New text does not appear in the document: " + this);
			}
		}
	}

	private void replayTextChange() throws BadLocationException, ExecutionException {
		//Timestamp updates are not reproducible, because the corresponding UndoableOperation2ChangeAdapter operation 
		//is executed as a simple text change
		if (!isTimestampUpdate()) {
			if (isInTestMode) {
				replaySpecificTextChange();
			} else {
				currentDocument.replace(offset, length, newText);
			}
		}
	}

	private void updateCurrentState() {
		EditorHelper.activateEditor(currentEditor);
		if (currentEditor instanceof CompareEditor) {
			currentViewer= EditorHelper.getEditingSourceViewer((CompareEditor)currentEditor);
		} else if (currentEditor instanceof AbstractDecoratedTextEditor) {
			currentViewer= EditorHelper.getEditingSourceViewer((AbstractDecoratedTextEditor)currentEditor);
		}
		currentDocument= currentViewer.getDocument();
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

	@Override
	public boolean isTestReplayRecorded() {
		return isRecordedWhileRefactoring || !isTimestampUpdate();
	}

	/**
	 * If a recorded text change operation does not change anything in the document, it is a
	 * timestamp update (happens when an UndoableOperation2ChangeAdapter is undone/redone)
	 * 
	 * @return
	 */
	private boolean isTimestampUpdate() {
		return newText.isEmpty() && replacedText.isEmpty() && offset == 0 && length == 0;
	}

	protected abstract void replaySpecificTextChange() throws BadLocationException, ExecutionException;

}
