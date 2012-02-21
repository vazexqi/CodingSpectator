/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

public class Utils {

	public static String toJavaRefactoringID(String id) {
		final String JAVA_RENAME_CLASS_ID= "org.eclipse.jdt.ui.rename.class";
		if (id.equals("org.eclipse.jdt.ui.rename.compilationunit")) {
			return JAVA_RENAME_CLASS_ID;
		}
		if (id.equals("org.eclipse.jdt.ui.rename.type")) {
			return JAVA_RENAME_CLASS_ID;
		}
		return id;
	}

}
