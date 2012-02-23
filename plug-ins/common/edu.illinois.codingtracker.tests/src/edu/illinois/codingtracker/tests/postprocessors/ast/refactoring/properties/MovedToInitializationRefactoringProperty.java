/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents an AST node added as initialization of a variable.
 * 
 * @author Stas Negara
 * 
 */
public class MovedToInitializationRefactoringProperty extends RefactoringProperty {

	private final NodeDescriptor movedNode;

	private final String variableName;

	private final long moveID;


	public MovedToInitializationRefactoringProperty(NodeDescriptor movedNode, String variableName, long moveID) {
		this.movedNode= movedNode;
		this.variableName= variableName;
		this.moveID= moveID;
	}

	public NodeDescriptor getMovedNode() {
		return movedNode;
	}

	public String getVariableName() {
		return variableName;
	}

	public long getMoveID() {
		return moveID;
	}

}
