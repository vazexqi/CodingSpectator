/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;



/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class RefactoringProperty {

	//Attributes, whose values should be different to allow the properties to match.
	private static final Map<String, String> disjointAttributes= new HashMap<String, String>();

	static {
		//Disjoint attributes are (obviously) symmetrical.
		disjointAttributes.put(RefactoringPropertyAttributes.SOURCE_METHOD_ID, RefactoringPropertyAttributes.DESTINATION_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, RefactoringPropertyAttributes.SOURCE_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.GETTER_METHOD_ID, RefactoringPropertyAttributes.SETTER_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.SETTER_METHOD_ID, RefactoringPropertyAttributes.GETTER_METHOD_ID);
	}

	private final Map<String, Object> attributes= new HashMap<String, Object>();

	private boolean isActive= true;


	public String getClassName() {
		return getClass().getSimpleName();
	}

	public boolean isActive() {
		return isActive;
	}

	public void disable() {
		isActive= false;
	}

	protected void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public boolean doesMatch(RefactoringProperty anotherProperty) {
		for (Entry<String, Object> entry : attributes.entrySet()) {
			Object objectToMatch= anotherProperty.attributes.get(entry.getKey());
			if (objectToMatch != null && !objectToMatch.equals(entry.getValue())) {
				return false;
			}
			Object objectToDisjoin= anotherProperty.attributes.get(disjointAttributes.get(entry.getKey()));
			if (objectToDisjoin != null && objectToDisjoin.equals(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

}
