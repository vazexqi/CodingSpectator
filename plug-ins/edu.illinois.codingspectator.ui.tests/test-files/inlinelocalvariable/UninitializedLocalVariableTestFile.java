/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

public class UninitializedLocalVariableTestFile {

    public static void main(String[] args) {
        String localVariable;
        System.out.println(localVariable);
    }

}