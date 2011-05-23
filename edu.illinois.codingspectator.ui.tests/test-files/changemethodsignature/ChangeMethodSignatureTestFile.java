/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

class C {

    void m1(Object o) {
    }
    
    void m2() {
    	m1(new Object());
    }

}

class D extends C {
	
    @Override
    void m1(Object o) {
        super.m1(o);
    }
}
