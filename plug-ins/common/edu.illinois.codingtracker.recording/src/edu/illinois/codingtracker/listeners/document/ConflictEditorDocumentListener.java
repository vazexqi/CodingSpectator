/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.document;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ConflictEditorDocumentListener extends DocumentListener {

	private final CompareEditor conflictEditor;

	private final String conflictEditorID;


	public ConflictEditorDocumentListener(CompareEditor conflictEditor) {
		this.conflictEditor= conflictEditor;
		conflictEditorID= EditorHelper.getConflictEditorID(conflictEditor);
	}

	@Override
	protected String getCurrentFilePath() {
		//TODO: For now, return the conflict editor ID to distinguish AST elements. After implementing a better support for
		//conflict editors operations, return the actual file's full path (like in FileDocumentListener).
		return conflictEditorID;
	}

	@Override
	protected void handleDocumentChange(DocumentEvent event) {
		operationRecorder.recordConflictEditorChangedText(event, replacedText, conflictEditorID, isUndoing, isRedoing);
	}

}
