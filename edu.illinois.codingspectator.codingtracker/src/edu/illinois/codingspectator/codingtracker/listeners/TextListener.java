/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class TextListener extends BasicListener implements ITextListener {

	@Override
	public void textChanged(TextEvent event) {
		DocumentEvent documentEvent= event.getDocumentEvent();
		if (documentEvent != null && !isRefactoring) {
			if (EditorHelper.isConflictEditor(currentEditor)) {
				CompareEditor compareEditor= (CompareEditor)currentEditor;
				dirtyConflictEditors.add(compareEditor);
				operationRecorder.recordConflictEditorChangedText(event, EditorHelper.getConflictEditorID(compareEditor), isUndoing, isRedoing);
			} else {
				dirtyFiles.add(currentFile);
				operationRecorder.recordChangedText(event, currentFile, isUndoing, isRedoing);
			}
		}
	}

}
