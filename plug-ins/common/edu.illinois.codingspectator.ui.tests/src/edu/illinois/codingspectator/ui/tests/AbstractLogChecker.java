/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Mohsen Vakilian
 * 
 */
public abstract class AbstractLogChecker implements LogChecker {

	@Override
	public void generateExpectedLog(boolean overwrite) throws Exception {
		if (overwrite) {
			deleteExpectedLogsIfDoesNotMatchActualLogs();
		}
		copyActualLogsAsExpectedLogs();
	}

	protected void deleteExpectedLogsIfDoesNotMatchActualLogs() throws Exception {
		if (!doMatch()) {
			deleteExpectedLogs();
		}
	}

	private boolean doMatch() throws Exception {
		try {
			if (actualLogExists() && expectedLogExists()) {
				assertMatch();
				return true;
			} else {
				return false;
			}
		} catch (AssertionError e) {
			return false;
		}
	}

	abstract protected void copyActualLogsAsExpectedLogs() throws CoreException;

	abstract protected void deleteExpectedLogs() throws CoreException;

	abstract protected boolean expectedLogExists();

}
