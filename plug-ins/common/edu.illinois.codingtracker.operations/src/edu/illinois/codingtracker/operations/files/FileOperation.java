/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingtracker.operations.resources.ResourceOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class FileOperation extends ResourceOperation {

	public FileOperation() {
		super();
	}

	public FileOperation(IFile file) {
		super(file);
	}

	public FileOperation(IFile file, long timestamp) {
		super(file, timestamp);
	}

	public FileOperation(String filePath, long timestamp) {
		super(filePath, timestamp);
	}

}
