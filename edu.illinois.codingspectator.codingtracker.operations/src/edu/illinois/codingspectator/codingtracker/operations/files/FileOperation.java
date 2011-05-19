/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.operations.resources.ResourceOperation;

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
		super();
		resourcePath= ResourceHelper.getPortableResourcePath(file);
	}

}
