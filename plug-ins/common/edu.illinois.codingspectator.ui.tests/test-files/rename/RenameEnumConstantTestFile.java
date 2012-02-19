/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

class Box {
	Color color;
		
	enum Color {
        RED,
		GREEN,
		BLUE
	};
	
	Box(Color color) {
		this.color = color;
	}
	
	public static void main(String[] args) {
		Box b = new Box(Color.RED);
		System.out.println(b);
	}
}
