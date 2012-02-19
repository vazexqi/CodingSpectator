/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.saferecorder;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;

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
	public void preSubmit() {
	}

	@Override
	public void preCommit() {
		for (final SafeRecorder safeRecorderInstance : safeRecorderInstances) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					safeRecorderInstance.aboutToCommit();
				}

				@Override
				public void handleException(Throwable exception) {
				}
			});
		}
	}

	@Override
	public void postSubmit(boolean succeeded) {
		for (final SafeRecorder safeRecorderInstance : safeRecorderInstances) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					safeRecorderInstance.commitCompleted();
				}

				@Override
				public void handleException(Throwable exception) {
				}
			});
		}
	}

}
