/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

public class RenameNonOverriddenMethodTestFile {

    public void nonOverriddenMethod() {
    	System.out.println("Non overridden method");
    }
	
    public static void main(String[] args) {
        new RenameNonOverriddenMethodTestFile().nonOverriddenMethod();
    }

}
