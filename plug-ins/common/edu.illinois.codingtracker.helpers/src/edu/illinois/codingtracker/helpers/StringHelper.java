/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;


/**
 * 
 * @author Stas Negara
 * 
 */
public class StringHelper {

	/**
	 * Replaces oldPrefix with newPrefix in str. The callers are responsible to ensure that str
	 * indeed starts with oldPrefix.
	 * 
	 * @param str
	 * @param oldPrefix
	 * @param newPrefix
	 * @return
	 */
	public static String replacePrefix(String str, String oldPrefix, String newPrefix) {
		return newPrefix + str.substring(oldPrefix.length());
	}

	public static String capitalizeFirstCharacter(String str) {
		if (str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

}
