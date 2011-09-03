/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.ast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("rawtypes")
public class ASTHelper {

	private static final Map<String, Long> persistentNodeIDs= new HashMap<String, Long>();

	private static long nextFreePersistentID= 1;

	private static final String ROOT_NODE_LOCATION_ID= "root";

	private static final String LOCATIONS_DELIMITER= "|";


	public static String getPositionalNodeID(ASTNode node) {
		String positionalNodeID= "";
		while (node.getParent() != null) {
			ASTNode parentNode= node.getParent();
			StructuralPropertyDescriptor locationInParent= node.getLocationInParent();
			int childIndex= -1;
			if (locationInParent instanceof ChildListPropertyDescriptor) {
				List children= (List)parentNode.getStructuralProperty(locationInParent);
				childIndex= children.indexOf(node);
				if (childIndex == -1) {
					throw new RuntimeException("Could not find the child node: " + node + " in parent: " + parentNode);
				}
			}
			NodeLocation nodeLocation= new NodeLocation(locationInParent, childIndex);
			positionalNodeID= nodeLocation.getLocationString() + LOCATIONS_DELIMITER + positionalNodeID;
			node= parentNode;
		}
		//Finally, add the root node.
		NodeLocation rootNodeLocation= new NodeLocation(node, ROOT_NODE_LOCATION_ID);
		positionalNodeID= rootNodeLocation.getLocationString() + LOCATIONS_DELIMITER + positionalNodeID;
		return positionalNodeID;
	}

	public static ASTNode getASTNodeFromPositonalID(ASTNode rootNode, String positionalNodeID) {
		StringTokenizer locationsTokenizer= new StringTokenizer(positionalNodeID, LOCATIONS_DELIMITER);
		NodeLocation rootNodeLocation= new NodeLocation(locationsTokenizer.nextToken());
		if (!rootNodeLocation.getLocationID().equals(ROOT_NODE_LOCATION_ID)) {
			throw new RuntimeException("positionalNodeID does not start with the root node: " + positionalNodeID);
		}
		if (!rootNodeLocation.getParentNodeName().equals(rootNode.getClass().getSimpleName())) {
			throw new RuntimeException("positionalNodeID starts with a root node of different type than the provided root node: " + positionalNodeID);
		}
		ASTNode currentNode= rootNode;
		while (locationsTokenizer.hasMoreTokens()) {
			NodeLocation currentNodeLocation= new NodeLocation(locationsTokenizer.nextToken());
			if (!currentNode.getClass().getSimpleName().equals(currentNodeLocation.getParentNodeName())) {
				throw new RuntimeException("Current node's class does not match the current location's parent node class: " + currentNodeLocation.getParentNodeName());
			}
			Object structuralProperty= currentNode.getStructuralProperty(getLocationDescriptor(currentNode, currentNodeLocation.getLocationID()));
			if (currentNodeLocation.getChildIndex() != -1) {
				List children= (List)structuralProperty;
				currentNode= (ASTNode)children.get(currentNodeLocation.getChildIndex());
			} else {
				currentNode= (ASTNode)structuralProperty;
			}
		}
		return currentNode;
	}

	private static StructuralPropertyDescriptor getLocationDescriptor(ASTNode node, String locationID) {
		//Since the constructors of StructuralPropertyDescriptor and its subclasses are not accessible, iterate over all
		//of them for this node type and find the match.
		for (Object structuralPropertyDescriptor : node.structuralPropertiesForType()) {
			StructuralPropertyDescriptor locationDescriptor= (StructuralPropertyDescriptor)structuralPropertyDescriptor;
			if (locationDescriptor.getId().equals(locationID)) {
				return locationDescriptor;
			}
		}
		throw new RuntimeException("Could not find the location descriptor for id: " + locationID);
	}

	public static void printSubtree(ASTNode node) {
		System.out.println("Printing children for node:");
		printASTNode(node);
		List<ASTNode> workList= new LinkedList<ASTNode>();
		for (Object structuralPropertyDescriptor : node.structuralPropertiesForType()) {
			StructuralPropertyDescriptor locationDescriptor= (StructuralPropertyDescriptor)structuralPropertyDescriptor;
			Object structuralProperty= node.getStructuralProperty(locationDescriptor);
			if (structuralProperty instanceof ASTNode) {
				ASTNode child= (ASTNode)structuralProperty;
				printASTNode(child);
				workList.add(child);
			} else if (structuralProperty instanceof List) {
				List children= (List)structuralProperty;
				for (Object obj : children) {
					ASTNode child= (ASTNode)obj;
					printASTNode(child);
					workList.add(child);
				}
			}
		}
		for (ASTNode child : workList) {
			printSubtree(child);
		}
	}

	public static void printASTNode(ASTNode node) {
		int start= node.getStartPosition();
		int end= start + node.getLength();
		System.out.println("ASTNode \"" + node.getClass().getSimpleName() + "\" [" + start + ", " + end + "): " + node);
	}

	public static long getPersistentNodeID(ASTNode node) {
		return getPersistentNodeID(getPositionalNodeID(node));
	}

	public static long getPersistentNodeID(String positionalNodeID) {
		Long persistentNodeID= persistentNodeIDs.get(positionalNodeID);
		if (persistentNodeID == null) {
			persistentNodeID= nextFreePersistentID;
			persistentNodeIDs.put(positionalNodeID, nextFreePersistentID);
			nextFreePersistentID++;
		}
		return persistentNodeID;
	}

	public static void removePersistentNodeID(ASTNode node) {
		removePersistentNodeID(getPositionalNodeID(node));
	}

	public static void removePersistentNodeID(String positionalNodeID) {
		persistentNodeIDs.remove(positionalNodeID);
	}

	public static void updatePersistentNodeIDs(Map<String, String> updatePositionalNodeIDsMap) {
		//Collect new mappings in a separate map and update the main map only in the end to avoid paths collisions.
		Map<String, Long> newPersistentNodeIDs= new HashMap<String, Long>();
		for (Entry<String, String> mapEntry : updatePositionalNodeIDsMap.entrySet()) {
			long persistentNodeID= getPersistentNodeID(mapEntry.getKey());
			removePersistentNodeID(mapEntry.getKey());
			newPersistentNodeIDs.put(mapEntry.getValue(), persistentNodeID);
		}
		persistentNodeIDs.putAll(newPersistentNodeIDs);
	}

	private static class NodeLocation {
		private static final String LOCATION_INTERNAL_DELIMITER= ":";

		private final String parentNodeName; //With the exception of the root node, which is the parent of itself.

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

}