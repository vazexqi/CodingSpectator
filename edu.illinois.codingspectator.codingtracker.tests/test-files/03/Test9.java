package edu.illinois.test2;

import java.util.LinkedList;
import java.util.List;

public class Test9 {

	public void my_test(){
		List<String> very_long_list_name = new LinkedList<String>();
		int very_long_integer_name =5;
		long very_long_long_name = 100;
		qwerty(very_long_list_name, very_long_integer_name, very_long_long_name);
		System.out.println("aaabbb");
	}

	/**
	 * 
	 * @param good_name
	 * @param very_long_integer_name
	 * @param very_long_long_name
	 */
	private void qwerty(List<String> bad_name, int very_long_integer_name, long very_long_long_name) {
		bad_name.add(String.valueOf(very_long_integer_name+very_long_long_name));
	}

}
