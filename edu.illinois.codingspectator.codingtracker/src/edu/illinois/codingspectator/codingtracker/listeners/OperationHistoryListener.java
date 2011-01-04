/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class OperationHistoryListener extends BasicListener implements IOperationHistoryListener {

	public static void register() {
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new OperationHistoryListener());
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		int eventType= event.getEventType();
		updateState(eventType);
		if (eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || (eventType == OperationHistoryEvent.ABOUT_TO_REDO) ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			IUndoableOperation undoableOperation= event.getOperation();
			if (undoableOperation instanceof TriggeredOperations) {
				IUndoableOperation triggeringOperation= ((TriggeredOperations)undoableOperation).getTriggeringOperation();
				if (triggeringOperation instanceof UndoableOperation2ChangeAdapter) {
					Set<IFile> affectedFiles= getAffectedFiles((UndoableOperation2ChangeAdapter)triggeringOperation);
					eventLogger.ensureAreKnownFiles(affectedFiles);
				}
			}
		}
	}

	private void updateState(int eventType) {
		if (eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			isUndoing= true;
		} else {
			isUndoing= false;
		}
		if (eventType == OperationHistoryEvent.ABOUT_TO_REDO) {
			isRedoing= true;
		} else {
			isRedoing= false;
		}
		if (eventType == OperationHistoryEvent.UNDONE || eventType == OperationHistoryEvent.REDONE) {
			//note that conflict editors remain dirty until saved
			if (currentEditor != null && !EditorHelper.isConflictEditor(currentEditor)) {
				if (currentEditor.isDirty()) {
					dirtyFiles.add(currentFile);
				} else {
					dirtyFiles.remove(currentFile);
				}
			}
		}
	}

	private Set<IFile> getAffectedFiles(UndoableOperation2ChangeAdapter operation) {
		Set<IFile> affectedFiles= new HashSet<IFile>();
		Object[] affectedObjects= operation.getAllAffectedObjects();
		if (affectedObjects != null) {
			for (Object affectedObject : affectedObjects) {
				if (affectedObject instanceof CompilationUnit) {
					IResource resource= ((CompilationUnit)affectedObject).getResource();
					if (resource instanceof IFile) { //Could it be something else?
						IFile file= (IFile)resource;
						Debugger.debugFilePath("File affected by refactoring: ", file);
						affectedFiles.add(file);
					}
				}
			}
		}
		return affectedFiles;
	}
}
