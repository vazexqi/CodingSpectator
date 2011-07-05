/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

class C {

    void m1(Object o1, Object o2) {
    }
    
    void m2() {
        m1(new Object(), new Object());
    }

}

class D extends C {
	
    @Override
    void m1(Object o1, Object o2) {
        super.m1(o1, o2);
    }
    
}
