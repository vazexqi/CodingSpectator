/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTMethodDescriptor {

	private final long methodID;

	private final String methodFullName;

	private final int methodLinesCount;

	private final int methodCyclomaticComplexity;


	public ASTMethodDescriptor(long methodID, String methodFullName, int methodLinesCount, int methodCyclomaticComplexity) {
		this.methodID= methodID;
		this.methodFullName= methodFullName;
		this.methodLinesCount= methodLinesCount;
		this.methodCyclomaticComplexity= methodCyclomaticComplexity;
	}

	public long getMethodID() {
		return methodID;
	}

	public String getMethodFullName() {
		return methodFullName;
	}

	public int getMethodLinesCount() {
		return methodLinesCount;
	}

	public int getMethodCyclomaticComplexity() {
		return methodCyclomaticComplexity;
	}

	public void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(methodID);
		textChunk.append(methodFullName);
		textChunk.append(methodLinesCount);
		textChunk.append(methodCyclomaticComplexity);
	}

	public static ASTMethodDescriptor createFrom(OperationLexer operationLexer) {
		return new ASTMethodDescriptor(operationLexer.readLong(), operationLexer.readString(), operationLexer.readInt(),
										operationLexer.readInt());
	}

	public void appendContent(StringBuffer sb) {
		sb.append("Method ID: " + methodID + "\n");
		sb.append("Fully qualified method name: " + methodFullName + "\n");
		sb.append("Method lines count: " + methodLinesCount + "\n");
		sb.append("Method cyclomatic complexity: " + methodCyclomaticComplexity + "\n");
	}

}
