/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;



/**
 * 
 * @author Stas Negara
 * 
 */
public class LongItem implements Item {

	private Long value;


	public LongItem(long value) {
		this.value= value;
	}

	@Override
	public int compareTo(Item otherItem) {
		return value.compareTo(((LongItem)otherItem).value);
	}

	/**
	 * It works without the overriding of equals method, but keep it for a reference.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LongItem)) {
			return false;
		}
		return value.equals(((LongItem)obj).value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
