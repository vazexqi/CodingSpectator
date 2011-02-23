/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class RecorderSubmitterListener extends BasicListener implements SubmitterListener {

	@Override
	public void preSubmit() {
		operationRecorder.commitStarted();
	}

	@Override
	public void postSubmit(boolean succeeded) {
		operationRecorder.commitCompleted();
	}

}
