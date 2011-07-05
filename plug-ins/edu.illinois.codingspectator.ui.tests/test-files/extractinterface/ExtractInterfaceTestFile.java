/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

class Parent {
    private void m1() {    }
    private void m2() {    }
}

class Child1 extends Parent {
    private void m2() {    }
}

class Child2 extends Parent {
	
}
