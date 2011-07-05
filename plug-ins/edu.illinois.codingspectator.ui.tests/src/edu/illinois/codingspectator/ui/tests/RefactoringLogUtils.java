/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.Path;

import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;

/**
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class RefactoringLogUtils {

	static final String EXPECTED_DESCRIPTORS= "expected-descriptors";

	public static RefactoringLog getExpectedRefactoringLog(String expectedRefactoringDescriptorRelativePath) {
		return new RefactoringLog(new Path(EXPECTED_DESCRIPTORS).append(expectedRefactoringDescriptorRelativePath));
	}

	public static CapturedRefactoringDescriptor getTheSingleExpectedRefactoringDescriptor(String expectedRefactoringDescriptorRelativePath, String projectName) {
		RefactoringLog refactoringLog= getExpectedRefactoringLog(expectedRefactoringDescriptorRelativePath);
		return getTheSingleRefactoringDescriptor(refactoringLog, projectName);
	}

	public static CapturedRefactoringDescriptor getTheSingleRefactoringDescriptor(RefactoringLog refactoringLog, String projectName) {
		assertTrue(refactoringLog.exists());
		Collection<CapturedRefactoringDescriptor> descriptors= refactoringLog.getRefactoringDescriptors(projectName);
		assertEquals(1, descriptors.size());
		return descriptors.iterator().next();
	}

}
