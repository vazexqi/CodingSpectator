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
public class LoggerSubmitterListener extends BasicListener implements SubmitterListener {

	@Override
	public void preSubmit() {
		eventLogger.commitStarted();
	}

	@Override
	public void postSubmit() {
		eventLogger.commitCompleted();
	}

	@Override
	public void failedToSubmit() {
		// FIXME: @Stas is there anything that you need to do when it fails to submit?
	}

}
