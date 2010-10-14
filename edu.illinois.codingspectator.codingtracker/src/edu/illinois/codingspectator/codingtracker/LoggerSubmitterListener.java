/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import edu.illinois.codingspectator.monitor.submission.SubmitterListener;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class LoggerSubmitterListener implements SubmitterListener {

	@Override
	public void preSubmit() {
		Logger.getInstance().commitStarted();
	}

	@Override
	public void postSubmit() {
		Logger.getInstance().commitCompleted();
	}

}
