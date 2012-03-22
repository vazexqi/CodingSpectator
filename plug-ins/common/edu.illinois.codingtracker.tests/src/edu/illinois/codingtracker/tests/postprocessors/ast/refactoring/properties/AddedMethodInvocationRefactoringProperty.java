/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added invocation of a method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedMethodInvocationRefactoringProperty extends AtomicRefactoringProperty {


	public AddedMethodInvocationRefactoringProperty(String entityName, long entityNameNodeID, String sourceMethodName, long sourceMethodID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_NAME, sourceMethodName);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_ID, sourceMethodID);
	}

}
