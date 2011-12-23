/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SVNCommittedFileOperation extends CommittedFileOperation {

	public SVNCommittedFileOperation() {
		super();
	}

	public SVNCommittedFileOperation(IFile committedFile, String revision, String committedRevision) {
		super(committedFile, revision, committedRevision);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_SVN_COMMITTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "SVN committed file";
	}

}
