/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoring;



/**
 * 
 * @author Stas Negara
 * 
 */
public interface RefactoringProperty {

	public static final long decayTimeThreshold= 5 * 60 * 1000; // 5 minutes until a property becomes too old.

	public static final long closenessTimeThreshold= 100; // 100 milliseconds.

	public Object getAttribute(String name);

	public ASTOperation getLastRelatedOperation();

	public void setRefactoringID(long refactoringID);

	public boolean addPossiblyRelatedOperation(ASTOperation operation);

	public long getActivationTimestamp();

	public void checkTimeout(long currentTimestamp);

	public String getClassName();

	public boolean isActive();

	public void disable();

	public void addRefactoring(InferredRefactoring refactoring);

	public void removeRefactoring(InferredRefactoring refactoring);

	public void setMainOperation(ASTOperation mainOperation);

	public void fireCorrected();

	/**
	 * containingRefactoring is the refactoring that contains this property. If the containing
	 * refactoring does not matter, containingRefactoring is null.
	 * 
	 * @param containingRefactoring
	 * @param anotherProperty
	 * @return
	 */
	public boolean doesMatch(InferredRefactoring containingRefactoring, RefactoringProperty anotherProperty);

	/**
	 * Checks whether two properties affect the same program entity, which is used to avoid
	 * over-growing completed refactorings when a refactoring is undone and then performed again.
	 * 
	 * TODO: Currently it is implemented only for refactoring properties that contribute to Rename
	 * refactorings.
	 * 
	 * @param refactoringProperty
	 * @return
	 */
	public boolean doesAffectSameEntity(RefactoringProperty refactoringProperty);

}
