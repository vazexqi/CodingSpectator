/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.listeners.OperationHistoryListener;
import edu.illinois.codingspectator.codingtracker.listeners.PartListener;
import edu.illinois.codingspectator.codingtracker.listeners.RefactoringExecutionListener;
import edu.illinois.codingspectator.codingtracker.listeners.ResourceChangeListener;
import edu.illinois.codingspectator.codingtracker.listeners.SelectionListener;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted PartListener from this class.
 * 
 */
public class CodeChangeTracker {

	/**
	 * This is the entry point of codingtracker plugin. It registers all listeners except
	 * TextListener, which is registered in SelectionListener.
	 */
	public static void start() {
		Debugger.debug("STARTED");
		ResourceChangeListener.register();
		OperationHistoryListener.register();
		RefactoringExecutionListener.register();
		SelectionListener.register();
		PartListener.register(); //should be registered after SelectionListener 
	}

}
