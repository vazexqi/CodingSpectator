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
		if (isBeginOperation(eventType)) {
			IUndoableOperation undoableOperation= event.getOperation();
			if (undoableOperation instanceof TriggeredOperations) {
				IUndoableOperation triggeringOperation= ((TriggeredOperations)undoableOperation).getTriggeringOperation();
				if (triggeringOperation instanceof UndoableOperation2ChangeAdapter) {
					//TODO: Ensuring that affected files are known might not be needed after refactorings are recorded on the atomic level
					Set<IFile> affectedFiles= getAffectedFiles((UndoableOperation2ChangeAdapter)triggeringOperation);
					operationRecorder.ensureFilesAreKnown(affectedFiles, true);
				}
			}
		} else if (isRefactoring && isFinishOperation(eventType)) {
			isRefactoring= false;
			operationRecorder.recordFinishedRefactoring(eventType != OperationHistoryEvent.OPERATION_NOT_OK);
		}
	}

	private boolean isBeginOperation(int eventType) {
		return eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || eventType == OperationHistoryEvent.ABOUT_TO_REDO ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO;
	}

	private boolean isFinishOperation(int eventType) {
		return eventType == OperationHistoryEvent.DONE || eventType == OperationHistoryEvent.REDONE ||
				eventType == OperationHistoryEvent.UNDONE || eventType == OperationHistoryEvent.OPERATION_NOT_OK;
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
