/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatched;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedProcessor;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedRefactoring;

/**
 * A generic move refactoring. The actual refactoring is done by the move processor passed to the
 * constructor.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * @since 3.0
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @author Mohsen Vakilian, nchen - Made the class implement IWatchedRefactoring.
 */
public class MoveRefactoring extends ProcessorBasedRefactoring implements IWatchedRefactoring {

	private MoveProcessor fProcessor;

	/**
	 * Creates a new move refactoring with the given move processor.
	 * 
	 * @param processor the move processor
	 */
	public MoveRefactoring(MoveProcessor processor) {
		super(processor);
		Assert.isNotNull(processor);
		fProcessor= processor;
	}

	/**
	 * Returns the move processor associated with this move refactoring.
	 * 
	 * @return returns the move processor associated with this move refactoring
	 */
	public MoveProcessor getMoveProcessor() {
		return fProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}

	//CODINGSPECTATOR: Added the following methods.

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		if (!(fProcessor instanceof IWatched))
			throw new UnsupportedOperationException();
		return ((IWatched)fProcessor).getSimpleRefactoringDescriptor(refactoringStatus);
	}

	public boolean isWatched() {
		return fProcessor instanceof IWatchedProcessor;
	}
}
