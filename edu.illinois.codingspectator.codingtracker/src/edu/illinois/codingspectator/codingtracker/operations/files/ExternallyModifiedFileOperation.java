/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ExternallyModifiedFileOperation extends FileOperation {

	public ExternallyModifiedFileOperation(IFile externallyModifiedFile) {
		super(externallyModifiedFile);
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.FILE_EXTERNALLY_MODIFIED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "File externally modified: ";
	}

}
