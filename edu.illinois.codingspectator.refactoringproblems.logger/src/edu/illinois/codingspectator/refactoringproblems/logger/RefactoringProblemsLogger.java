/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import org.eclipse.core.resources.IMarkerDelta;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringProblemsLogger {
	public void logRefactoringProblems(IMarkerDelta[] deltas) {
		//TODO: Use XMLEncoder
		//TODO: Make use of SafeRecorder
		for (IMarkerDelta delta : deltas) {
			System.err.println(delta);
		}
	}
}
