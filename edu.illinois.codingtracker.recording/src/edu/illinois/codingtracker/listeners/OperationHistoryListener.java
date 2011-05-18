/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationHistoryListener extends BasicListener implements IOperationHistoryListener {

	public static void register() {
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new OperationHistoryListener());
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		int eventType= event.getEventType();
		updateState(eventType);
		if (isRefactoring && isFinishOperation(eventType)) {
			isRefactoring= false;
			operationRecorder.recordFinishedRefactoring(eventType != OperationHistoryEvent.OPERATION_NOT_OK);
		}
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

}
