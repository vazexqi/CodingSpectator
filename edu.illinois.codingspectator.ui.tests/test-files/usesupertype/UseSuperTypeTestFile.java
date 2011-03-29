/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

class Grandparent {
    public void m1() {
    }

    void m2() {
    }
}

class Parent extends Grandparent {
    
    public void m1() {
        Parent p = new Parent();
        p.m2();
    }
    
    void m2() {
    }
}

class Child extends Parent {
    public void m1() {
        Child c = new Child();
        c.m2();
    }
    
    void m2() {
    }
}
