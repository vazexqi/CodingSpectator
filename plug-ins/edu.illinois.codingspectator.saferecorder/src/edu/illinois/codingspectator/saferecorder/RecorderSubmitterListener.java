/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.saferecorder;

import java.util.Collection;
import java.util.LinkedList;

import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class RecorderSubmitterListener implements SubmitterListener {

	private static Collection<SafeRecorder> safeRecorderInstances= new LinkedList<SafeRecorder>();

	public static synchronized void addSafeRecorderInstance(SafeRecorder safeRecorder) {
		if (safeRecorderInstances.contains(safeRecorder)) {
			throw new RuntimeException("Can not add the same instance of SafeRecorder twice");
		}
		safeRecorderInstances.add(safeRecorder);
	}

	@Override
	public void preLock() {
		for (SafeRecorder safeRecorderInstance : safeRecorderInstances) {
			safeRecorderInstance.aboutToCommit();
		}
	}

	//TODO: If no code reacts to this notification, get rid of it, and rename preLock to preSubmit.
	@Override
	public void preSubmit() {
	}

	//TODO: Remove if not needed any more?
	@Override
	public void postSubmit(boolean succeeded) {
	}

}
