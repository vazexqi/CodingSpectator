package edu.illinois.codingspectator;

class Box {
	Color _color;
		
	enum Color {
        RED,
		GREEN,
		BLUE
	};
	
	Box(Color color) {
		_color = color;
	}
	
	public static void main(String[] args) {
		Box b = new Box(Color.RED);
		System.out.println(b);
	}
}
