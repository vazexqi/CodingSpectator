/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording.filtering;

public class FilterUtils {
	
	/**
	 * This method returns a suggestion for a filter pattern based on the 
	 * bundle symbolic ids provided.
	 * 
	 * @param names an array of bundle symbolic ids.
	 * @return a suggestion for a a filter pattern.
	 */
	public static String getFilterSuggestionBasedOnBundleIds(String[] names) {
		int index;
		StringBuilder builder = new StringBuilder();
		index = 0;
		outer: while (true) {
			if (names[0].length() <= index) break outer;
			char next = names[0].charAt(index);
			for (int i=1;i<names.length;i++) {
				if (names[i].length() <= index) break outer;
				if (names[i].charAt(index) != next) break outer;
			}
			builder.append(next);
			index++;
		}
		if (builder.length() == 0) return getDefaultFilterSuggestion();
		builder.append("*"); //$NON-NLS-1$
		return builder.toString();
	}
	
	public static String getDefaultFilterSuggestion() {
		return "com.*"; //$NON-NLS-1$
	}
	
	public static boolean isValidBundleIdPattern(String pattern) {
		return pattern.matches("[a-zA-Z0-9\\*]+?(\\.[a-zA-Z0-9\\*]+?)*?"); //$NON-NLS-1$
	}

}
