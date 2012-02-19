/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

public class IntroduceFactoryTestFile {

    private Object o;
	
    public IntroduceFactoryTestFile(Object o) {
        this.o = o;
    }

    public static void main(String[] args) {
        System.out.println(new IntroduceFactoryTestFile(new Object()));
    }

}
