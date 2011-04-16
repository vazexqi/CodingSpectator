/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.ui.JavaPlugin;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.CompareEditorsUpkeeper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class OpenedConflictEditorOperation extends ConflictEditorOperation {

	private String editedFilePath;

	private String initialContent;

	public OpenedConflictEditorOperation() {
		super();
	}

	public OpenedConflictEditorOperation(String editorID, IFile editedFile, String initialContent) {
		super(editorID);
		this.editedFilePath= FileHelper.getPortableFilePath(editedFile);
		this.initialContent= initialContent;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_OPENED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Opened conflict editor";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(editedFilePath);
		textChunk.append(initialContent);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		editedFilePath= operationLexer.getNextLexeme();
		initialContent= operationLexer.getNextLexeme();
	}

	@Override
	public void replay() {
		IResource editedFile= FileHelper.findWorkspaceMember(new Path(editedFilePath));
		FileHelper.checkResourceExists(editedFile, "Conflict editor file does not exist: " + this);
		CompareUI.openCompareEditor(new DocumentCompareEditorInput(editedFile, initialContent));
		CompareEditorsUpkeeper.addEditor(editorID, (CompareEditor)JavaPlugin.getActivePage().getActiveEditor());
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("File path: " + editedFilePath + "\n");
		sb.append("Initial content: " + initialContent + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
