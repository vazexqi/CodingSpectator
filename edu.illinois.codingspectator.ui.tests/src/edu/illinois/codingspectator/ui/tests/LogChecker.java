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

	public void assertLogIsEmpty();

	public void assertMatch() throws Exception;

	public void clean() throws CoreException;

}
