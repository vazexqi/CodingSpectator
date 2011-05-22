package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedProcessor;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface IWatchedJavaProcessor extends IWatchedProcessor {

	public JavaRefactoringDescriptor getOriginalRefactoringDescriptor();

}
