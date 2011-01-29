/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class PullUpTest extends CodingSpectatorTest {

	@Override
	protected String refactoringMenuItemName() {
		return "Pull Up...";
	}

	@Override
	protected String getTestFileName() {
		return "PullUpFieldTestFile";
	}

	@Override
	protected String getRefactoringDialogName() {
		return "Refactoring";
	}

}
