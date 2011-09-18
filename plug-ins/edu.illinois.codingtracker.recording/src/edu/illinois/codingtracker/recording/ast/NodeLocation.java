/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * 
 * @author Stas Negara
 * 
 */
public class NodeLocation {
	private static final String LOCATION_INTERNAL_DELIMITER= ":";

	//With the exception of the root node, which is the parent of itself.
	private final String parentNodeName;

	private final String locationID;

	private final int childIndex;

	NodeLocation(ASTNode rootNode, String locationID) {
		parentNodeName= rootNode.getClass().getSimpleName();
		this.locationID= locationID;
		childIndex= -1;
	}

	NodeLocation(StructuralPropertyDescriptor locationInParent, int childIndex) {
		parentNodeName= locationInParent.getNodeClass().getSimpleName();
		locationID= locationInParent.getId();
		this.childIndex= childIndex;
	}

	NodeLocation(String locationString) {
		StringTokenizer locationTokenizer= new StringTokenizer(locationString, LOCATION_INTERNAL_DELIMITER);
		parentNodeName= locationTokenizer.nextToken();
		locationID= locationTokenizer.nextToken();
		if (locationTokenizer.hasMoreTokens()) {
			childIndex= Integer.parseInt(locationTokenizer.nextToken());
		} else {
			childIndex= -1;
		}
	}

	String getParentNodeName() {
		return parentNodeName;
	}

	String getLocationID() {
		return locationID;
	}

	int getChildIndex() {
		return childIndex;
	}

	String getLocationString() {
		String childIndexString= childIndex == -1 ? "" : LOCATION_INTERNAL_DELIMITER + childIndex;
		return parentNodeName + LOCATION_INTERNAL_DELIMITER + locationID + childIndexString;
	}

}
