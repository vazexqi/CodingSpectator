/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public interface LogChecker {

	void assertLogIsEmpty();

	void assertMatch() throws Exception;

	void clean() throws CoreException;

	void generateExpectedLog() throws CoreException;

}
