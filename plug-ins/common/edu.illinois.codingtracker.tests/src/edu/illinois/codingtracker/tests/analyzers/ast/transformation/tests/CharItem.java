/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation.tests;

import edu.illinois.codingtracker.tests.analyzers.ast.transformation.Item;



/**
 * 
 * @author Stas Negara
 * 
 */
public class CharItem implements Item {

	private String charString;


	public CharItem(char c) {
		charString= String.valueOf(c);
	}

	@Override
	public int compareTo(Item otherItem) {
		return charString.compareTo(((CharItem)otherItem).charString);
	}

	/**
	 * It works without the overriding of equals method, but keep it for a reference.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CharItem)) {
			return false;
		}
		return charString.equals(((CharItem)obj).charString);
	}

	@Override
	public int hashCode() {
		return charString.hashCode();
	}

	@Override
	public String toString() {
		return charString;
	}

}
