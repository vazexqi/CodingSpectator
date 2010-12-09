/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.recording.Symbols;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class RedoneConflictEditorTextChangeOperation extends ConflictEditorTextChangeOperation {

	public RedoneConflictEditorTextChangeOperation(String editorID, TextEvent textEvent) {
		super(editorID, textEvent, Symbols.CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL, "Redone conflict editor text change: ");
	}

}
