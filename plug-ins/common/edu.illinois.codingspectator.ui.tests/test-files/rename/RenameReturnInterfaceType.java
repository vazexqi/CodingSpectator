/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

interface IType {
	long m();
}

class Concrete implements IType {
	public long m() {
		return 10;
	}
}

public class RenameReturnInterfaceType {
	
    public IType test() {
    	return new Concrete();
    }
	
    public static void main(String[] args) {
        System.out.println(new RenameReturnInterfaceType().test().m());
    }

}
