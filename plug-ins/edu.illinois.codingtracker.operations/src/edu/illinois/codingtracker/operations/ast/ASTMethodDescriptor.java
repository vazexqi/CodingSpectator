/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;


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

}
