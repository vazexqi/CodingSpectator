/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class RefactoringLogUtils {

	public static RefactoringLog getExpectedRefactoringLog(String expectedRefactoringDescriptorRelativePath) {
		return new RefactoringLog(new Path("expected-refactoring-descriptors").append(expectedRefactoringDescriptorRelativePath));
	}

	public static CapturedRefactoringDescriptor getTheSingleExpectedRefactoringDescriptor(String expectedRefactoringDescriptorRelativePath, String projectName) {
		RefactoringLog refactoringLog= getExpectedRefactoringLog(expectedRefactoringDescriptorRelativePath);
		return getTheSingleRefactoringDescriptor(refactoringLog, projectName);
	}

	public static CapturedRefactoringDescriptor getTheSingleRefactoringDescriptor(RefactoringLog refactoringLog, String projectName) {
		assertTrue(refactoringLog.exists());
		Collection<JavaRefactoringDescriptor> descriptors= refactoringLog.getRefactoringDescriptors(projectName);
		assertEquals(1, descriptors.size());
		return new CapturedRefactoringDescriptor(descriptors.iterator().next());
	}

}
