/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

public class ConvertLocalVariableToFieldTestFile {

    Object field;
	
    void m() {
        Object localVariable= new Object();
        System.out.println(localVariable);
        System.out.println(field);
    }

}
