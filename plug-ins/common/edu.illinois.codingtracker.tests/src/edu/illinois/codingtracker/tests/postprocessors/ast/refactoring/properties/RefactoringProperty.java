/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class RefactoringProperty {

	private boolean isActive= true;


	public boolean isActive() {
		return isActive;
	}

	public void disable() {
		isActive= false;
	}

}
