/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

public class InlineMethodTestFile {

	void m1() {
		System.out.println("m1");
		m2();
	}

	void m2() {
		System.out.println("m2");
	}

}
