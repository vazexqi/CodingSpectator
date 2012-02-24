/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents a moved AST node that is added as part of the code that is NOT a variable
 * declaration.
 * 
 * @author Stas Negara
 * 
 */
public class MovedToUsageRefactoringProperty extends RefactoringProperty {

	private final NodeDescriptor movedNode;

	private final long moveID;

	private final long parentID;


	public MovedToUsageRefactoringProperty(NodeDescriptor movedNode, long moveID, long parentID) {
		this.movedNode= movedNode;
		this.moveID= moveID;
		this.parentID= parentID;
	}

	public NodeDescriptor getMovedNode() {
		return movedNode;
	}

	public long getMoveID() {
		return moveID;
	}

	public long getParentID() {
		return parentID;
	}

}
