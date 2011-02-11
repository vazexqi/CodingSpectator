/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;


class Base {
	public void virtualMethod() {
		System.out.println("Base version of virtualMethod");
	}
}

class Derived extends Base {
	
	@Override
    public void virtualMethod() {
		System.out.println("Derived version of virtualMethod");
	}
}

public class RenameVirtualMethodTestFile {
	
    public static void main(String[] args) {
        new Derived().virtualMethod();
    }
}
