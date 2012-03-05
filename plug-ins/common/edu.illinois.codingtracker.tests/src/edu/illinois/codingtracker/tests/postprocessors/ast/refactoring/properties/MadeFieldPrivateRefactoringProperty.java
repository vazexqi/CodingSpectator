/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



/**
 * This class represents a field, whose visibility was changed to private.
 * 
 * @author Stas Negara
 * 
 */
public class MadeFieldPrivateRefactoringProperty extends RefactoringProperty {

	private static final Set<MadeFieldPrivateRefactoringProperty> currentProperties= new HashSet<MadeFieldPrivateRefactoringProperty>();


	public static MadeFieldPrivateRefactoringProperty createInstance(String entityName) {
		MadeFieldPrivateRefactoringProperty newInstance= new MadeFieldPrivateRefactoringProperty(entityName);
		currentProperties.add(newInstance);
		return newInstance;
	}

	private MadeFieldPrivateRefactoringProperty(String entityName) {
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
	}

	@Override
	public void disable() {
		//Disable all the current properties for the same field.
		Iterator<MadeFieldPrivateRefactoringProperty> propertiesIterator= currentProperties.iterator();
		while (propertiesIterator.hasNext()) {
			MadeFieldPrivateRefactoringProperty property= propertiesIterator.next();
			if (property.doesMatch(this)) {
				property.directDisable();
				propertiesIterator.remove();
			}
		}
	}

	private void directDisable() {
		super.disable();
	}

}
