/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

interface IUniversityEmployee {
	String name();
}

class StudentAssistant implements IUniversityEmployee {
	private String _name;
	
	StudentAssistant(String name) {
		_name = name;
	}

	@Override
	public String name() {
		return _name;
	}
}

class University {
	public static void main(String[] args) {
		IUniversityEmployee balaji = new StudentAssistant("Balaji Ambresh");
		printWelcomeMessage(balaji);
	}

    private static void printWelcomeMessage(IUniversityEmployee employee) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Welcome To UIUC")
			.append(employee.name());
		
		System.out.println(buffer.toString());
	}
}
