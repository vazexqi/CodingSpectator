/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.startup.StartupListener;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.operations.starts.StartedEclipseOperation;
import edu.illinois.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted PartListener from this class.
 * 
 */
public class JDTUIStartupListener implements StartupListener {

	/**
	 * This is the entry point of codingtracker.recording plugin. It registers all listeners except
	 * TextListener, which is registered in SelectionListener.
	 */
	@Override
	public void jdtuiIsAboutToStart() {
		Debugger.debug("NOTIFIED");
		if (doesMonitorUIExist()) {
			Debugger.debug("STARTED");
			FileBufferListener.register();
			DocumentAdapterListener.register();
			SelectionListener.register();
			PartListener.register();
			ResourceListener.register();
			CVSResourceChangeListener.register();
			OperationHistoryListener.register();
			RefactoringExecutionListener.register();
			JUnitListener.register();
			LaunchListener.register();
			TextRecorder.record(new StartedEclipseOperation());
		}
	}

	private static boolean doesMonitorUIExist() {
		return Platform.getBundle("edu.illinois.codingspectator.monitor.ui") != null;
	}

}
