/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
		//TODO: Do we want to use XMLEncoder or write out own format? The format for XMLEncoder is quite verbose
		//TODO: Make use of SafeRecorder to combine several XML files together
		for (IMarkerDelta delta : deltas) {
			XMLEncoder encoder;
			ByteArrayOutputStream stream= new ByteArrayOutputStream();
			encoder= new XMLEncoder(stream);
			RefactoringProblem problem= new RefactoringProblem(delta);
			encoder.writeObject(problem);
			encoder.close();
			System.err.println(stream.toString());
		}
	}
}
