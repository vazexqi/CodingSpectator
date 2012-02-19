/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.files.FileOperation;

/**
 * This operation helps to identify the file, to which the subsequent ASTOperations belong.
 * 
 * @author Stas Negara
 * 
 */
public class ASTFileOperation extends FileOperation {

	public ASTFileOperation() {
		super();
	}

	public ASTFileOperation(String astFilePath, long timestamp) {
		super(astFilePath, timestamp);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.AST_FILE_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "AST file operation";
	}

	@Override
	public void replay() {
		//do nothing
	}

}
