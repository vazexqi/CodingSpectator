/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.textchanges;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.ui.IEditorPart;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ConflictEditorTextChangeOperation extends TextChangeOperation {

	public static boolean isReplaying= false;

	private String editorID;

	public ConflictEditorTextChangeOperation() {
		super();
	}

	public ConflictEditorTextChangeOperation(String editorID, DocumentEvent documentEvent, String replacedText) {
		super(documentEvent, replacedText);
		this.editorID= editorID;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(editorID);
		super.populateTextChunk(textChunk);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		editorID= operationLexer.readString();
		super.initializeFrom(operationLexer);
	}

	@Override
	public void replay() throws ExecutionException, BadLocationException {
		IEditorPart oldEditor= currentEditor;
		currentEditor= EditorHelper.getCompareEditor(editorID);
		if (currentEditor == null) {
			//Sometimes, CodingTracker records document changes of closed conflict editors. This should not happen unless Eclipse 
			//does not handle correctly certain scenarios, but we do not know yet which kind of scenarios cause this problem. 
			//So, for now just output a warning.
			Debugger.debugWarning("Ignored text change in an inexisting conflict editor:\n" + this);
		} else {
			isReplaying= true;
			super.replay();
			isReplaying= false;
		}
		currentEditor= oldEditor;
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Editor ID: " + editorID + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
