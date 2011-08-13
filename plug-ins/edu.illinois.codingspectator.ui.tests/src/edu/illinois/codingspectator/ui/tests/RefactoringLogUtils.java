/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

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

	public static Collection<CapturedRefactoringDescriptor> getExpectedRefactoringDescriptors(String expectedRefactoringDescriptorRelativePath, String projectName) {
		RefactoringLog refactoringLog= getExpectedRefactoringLog(expectedRefactoringDescriptorRelativePath);
		return getRefactoringDescriptors(refactoringLog, projectName);
	}

	public static Collection<CapturedRefactoringDescriptor> getRefactoringDescriptors(RefactoringLog refactoringLog, String projectName) {
		assertTrue(refactoringLog.exists());
		return refactoringLog.getRefactoringDescriptors(projectName);
	}

}
