/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers;

import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;


/**
 * @author Stas Negara
 * 
 */
public class OperationFilePair {

	public final InferredUnknownTransformationOperation operation;

	public final String filePath;


	public OperationFilePair(InferredUnknownTransformationOperation operation, String filePath) {
		this.operation= operation;
		this.filePath= filePath;
	}

}
