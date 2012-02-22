/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents an AST node that is deleted and moved to other place.
 * 
 * @author Stas Negara
 * 
 */
public class MovedFromUsageRefactoringProperty implements RefactoringProperty {

	private final NodeDescriptor movedNode;

	private final long moveID;

	private final long parentID;


	public MovedFromUsageRefactoringProperty(NodeDescriptor movedNode, long moveID, long parentID) {
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
