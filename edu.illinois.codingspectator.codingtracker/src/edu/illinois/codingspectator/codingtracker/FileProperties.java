/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import org.eclipse.core.resources.IFile;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
public class FileProperties {

	static boolean isJavaFile(IFile file) {
		String fileExtension= file.getFileExtension();
		if (fileExtension != null && fileExtension.equals("java")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

}
