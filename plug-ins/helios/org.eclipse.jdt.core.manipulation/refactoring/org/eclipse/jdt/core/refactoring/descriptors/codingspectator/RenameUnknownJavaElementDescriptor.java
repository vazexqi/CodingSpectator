/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.refactoring.descriptors.codingspectator;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * 
 * Refactoring descriptor for the rename Java element refactorings. When the user tries to invoke an
 * rename refactoring on a Java element where no such refactoring is possible, we use this
 * descriptor for the attempted refactoring. This class is based on InlineConstantDescriptor.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public final class RenameUnknownJavaElementDescriptor extends JavaRefactoringDescriptor {

	public RenameUnknownJavaElementDescriptor() {
		super(IJavaRefactorings.RENAME_UNKNOWN_JAVA_ELEMENT);
	}

	public RenameUnknownJavaElementDescriptor(String project, String description, String comment, Map arguments, int flags) {
		super(IJavaRefactorings.RENAME_UNKNOWN_JAVA_ELEMENT, project, description, comment, arguments, flags);
	}

}
