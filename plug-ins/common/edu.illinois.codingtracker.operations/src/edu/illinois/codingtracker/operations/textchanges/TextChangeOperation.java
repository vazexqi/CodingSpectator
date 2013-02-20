/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.textchanges;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

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

	public static long lastReplayedTimestamp;

	protected IDocument currentDocument= null;

	protected ISourceViewer currentViewer= null;

	private IFile editedFile= null;

	private boolean isRecordedWhileRefactoring= false;


	public TextChangeOperation() {
		super();
	}

	public TextChangeOperation(DocumentEvent documentEvent, String replacedText) {
		this(documentEvent, replacedText, System.currentTimeMillis());
	}

	public TextChangeOperation(DocumentEvent documentEvent, String replacedText, long timestamp) {
		super(timestamp);
		this.replacedText= replacedText;
		newText= documentEvent.getText();
		offset= documentEvent.getOffset();
		length= documentEvent.getLength();
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getReplacedText() {
		return replacedText;
	}

	public String getNewText() {
		return newText;
	}

	public DocumentEvent getDocumentEvent(String initialDocumentText) {
		return new DocumentEvent(new Document(initialDocumentText), offset, length, newText);
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
		lastReplayedTimestamp= getTime();
		if (isReplayedRefactoring) {
			isRecordedWhileRefactoring= true;
		} else {
			updateCurrentState();
			preReplay();
			replayTextChange();
			postReplay();
		}
	}

	/**
	 * This replay is used to emphasize the cursor location.
	 * 
	 * @throws BadLocationException
	 */
	public void splitReplay() throws BadLocationException {
		updateCurrentState();
		preReplay();
	}

	private void preReplay() throws BadLocationException {
		if (!Configuration.isInPostprocessMode) {
			//This is not executed while postprocessing to improve performance.
			currentViewer.revealRange(offset, length > newText.length() ? length : newText.length());
			//This is not executed while postprocessing to avoid USER Objects leak.
			currentViewer.setSelectedRange(offset, length);
		}
		if (!replacedText.equals(currentDocument.get(offset, length))) {
			throw new RuntimeException("Replaced text is not present in the document: " + this);
		}
	}

	private void postReplay() throws BadLocationException {
		if (!Configuration.isInPostprocessMode) {
			//This is not executed while postprocessing to avoid USER Objects leak.
			currentViewer.setSelectedRange(offset, newText.length());
		}
		if (!newText.equals(currentDocument.get(offset, newText.length()))) {
			throw new RuntimeException("New text does not appear in the document: " + this);
		}
	}

	private void replayTextChange() throws BadLocationException, ExecutionException {
		//Timestamp updates are not reproducible, because the corresponding UndoableOperation2ChangeAdapter operation 
		//is executed as a simple text change
		if (!isTimestampUpdate()) {
			if (Configuration.isInTestMode) {
				replaySpecificTextChange();
			} else {
				currentDocument.replace(offset, length, newText);
			}
		}
	}

	private void updateCurrentState() {
		EditorHelper.activateEditor(currentEditor);
		if (currentEditor instanceof CompareEditor) {
			CompareEditor compareEditor= (CompareEditor)currentEditor;
			currentViewer= EditorHelper.getEditingSourceViewer(compareEditor);
			editedFile= EditorHelper.getEditedJavaFile(compareEditor);
		} else if (currentEditor instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor abstractDecoratedTextEditor= (AbstractDecoratedTextEditor)currentEditor;
			currentViewer= EditorHelper.getEditingSourceViewer(abstractDecoratedTextEditor);
			editedFile= EditorHelper.getEditedJavaFile(abstractDecoratedTextEditor);
		}
		currentDocument= currentViewer.getDocument();
	}

	/**
	 * Valid only during replay.
	 * 
	 * @return
	 */
	public String getEditedText() {
		updateCurrentState();
		return currentDocument.get();
	}

	/**
	 * Valid only during replay.
	 * 
	 * @return
	 */
	public String getEditedFilePath() {
		updateCurrentState();
		return ResourceHelper.getPortableResourcePath(editedFile);
	}

	/**
	 * Valid during replay only. Used to measure the impact of automated refactorings only.
	 * 
	 * @return
	 */
	public int[] getAffectedLineNumbers() {
		updateCurrentState();
		//Add 1 since IDocument#computeNumberOfLines returns a number that is less by 1 than the correct number.
		int affectedLinesCount= 1 + Math.max(currentDocument.computeNumberOfLines(replacedText), currentDocument.computeNumberOfLines(newText));
		int[] affectedLineNumbers= new int[affectedLinesCount];
		int startLineNumber;
		try {
			startLineNumber= currentDocument.getLineOfOffset(offset);
		} catch (BadLocationException e) {
			throw new RuntimeException("Could not get the line number of offset: " + offset, e);
		}
		for (int i= 0; i < affectedLineNumbers.length; i++) {
			affectedLineNumbers[i]= startLineNumber + i;
		}
		if (replacedText.equals(newText) && affectedLineNumbers.length == 1 && affectedLineNumbers[0] == 0) {
			//Return an empty array of affected line numbers for scenarios in which the package is updated with the same name,
			//which happens as part of many automated Eclipse refactorings for no apparent reason.
			return new int[] {};
		}
		return affectedLineNumbers;
	}

	/**
	 * Detects whether this text change and the given text change are possibly representing the same
	 * text change happening in several edit boxes (e.g. when a developer renames a program entity).
	 * 
	 * @param operation
	 * @return
	 */
	public boolean isPossiblyCorrelatedWith(TextChangeOperation operation) {
		final long maxTimeDelta= 150; // 150 ms.
		return Math.abs(getTime() - operation.getTime()) < maxTimeDelta && !isCommentingOrUncommenting() &&
				newText.equals(operation.newText) && replacedText.equals(operation.replacedText) &&
				isPossiblyChangingCode() && !containsNewLine() && !isAdjacent(operation);
	}

	private boolean isAdjacent(TextChangeOperation operation) {
		return offset + newText.length() - replacedText.length() == operation.offset ||
				operation.offset + operation.newText.length() - operation.replacedText.length() == offset ||
				newText.length() == 0 && operation.newText.length() == 0 && offset == operation.offset;
	}

	private boolean isPossiblyChangingCode() {
		String actualReplacedText= replacedText.trim();
		String actualNewText= newText.trim();
		return !actualReplacedText.equals(actualNewText) && (!actualReplacedText.isEmpty() || !actualNewText.isEmpty());
	}

	private boolean containsNewLine() {
		final String newLine= "\n";
		return replacedText.indexOf(newLine) != -1 || newText.indexOf(newLine) != -1;
	}

	/**
	 * Shows whether this text change comments or uncomments a line of code. This check is used to
	 * avoid considering changes that might be produced by automated commenting or uncommenting
	 * several lines of code as possibly correlated changes caused by several edit boxes.
	 * 
	 * @return
	 */
	private boolean isCommentingOrUncommenting() {
		final String singleLineComment= "//";
		return replacedText.equals(singleLineComment) && newText.isEmpty() ||
				replacedText.isEmpty() && newText.equals(singleLineComment);
	}

	/**
	 * Detects whether this text change is undone by the given text change.
	 * 
	 * @param operation
	 * @return
	 */
	public boolean isUndoneBy(TextChangeOperation operation) {
		return this instanceof PerformedTextChangeOperation && operation instanceof UndoneTextChangeOperation &&
				offset == operation.offset && newText.equals(operation.replacedText) && replacedText.equals(operation.newText);
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
