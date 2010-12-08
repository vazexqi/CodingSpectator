/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import org.eclipse.core.resources.IFile;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class OperationFactory {

	public static StartEclipseOperation createStartEclipseOperation() {
		return new StartEclipseOperation();
	}

	public static FileEditOperation createFileEditOperation(IFile editedFile) {
		return new FileEditOperation(editedFile);
	}

}
