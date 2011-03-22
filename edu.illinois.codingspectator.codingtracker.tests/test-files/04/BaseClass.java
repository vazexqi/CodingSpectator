package edu.illinois.test;

import java.util.LinkedList;
import java.util.List;

public class BaseClass {

	public void method1(){
		int myInteger=5;
		long myLong= 1000;
		List<String> myStringList = new LinkedList<String>();
		String elementString = String.valueOf(myInteger) + myLong;
		myStringList.add(elementString);
		System.out.println("Element=" + myStringList.get(0));
	}
	
}
