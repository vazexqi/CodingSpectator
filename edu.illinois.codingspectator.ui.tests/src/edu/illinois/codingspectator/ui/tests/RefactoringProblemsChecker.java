/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsLogDeserializer;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsParserException;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class RefactoringProblemsChecker implements LogChecker {

	IPath logPath;

	public RefactoringProblemsChecker(IPath logPath) {
		this.logPath= logPath;
	}

	@Override
	public void assertLogIsEmpty() {

	}

	@Override
	public void assertMatch() {
		try {
			//TODO: Parse actual and expected refactoring problems. Then, compare the problem modulo timestamps.
			new RefactoringProblemsLogDeserializer().deserializeRefactoringProblemsLog(logPath.toOSString());
		} catch (RefactoringProblemsParserException e) {
			//FIXME: Log exceptions
			e.printStackTrace();
		}
	}

	@Override
	public void clean() throws CoreException {
	}

}
