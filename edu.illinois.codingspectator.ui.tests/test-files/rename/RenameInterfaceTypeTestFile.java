/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

interface IUniversityEmployee {
	String name();
}

class StudentAssistant implements IUniversityEmployee {
	private String name;
	
	StudentAssistant(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}
}