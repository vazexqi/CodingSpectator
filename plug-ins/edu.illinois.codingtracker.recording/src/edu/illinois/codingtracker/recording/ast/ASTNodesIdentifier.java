/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import edu.illinois.codingtracker.helpers.ResourceHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("rawtypes")
public class ASTNodesIdentifier {

	private static final Map<String, Map<String, Long>> persistentNodeIDs= new HashMap<String, Map<String, Long>>();

	private static final Map<Long, ASTNode> identifiedNodes= new HashMap<Long, ASTNode>();

	private static long nextFreePersistentID= 1;

	private static final String ROOT_NODE_LOCATION_ID= "root";

	private static final String LOCATIONS_DELIMITER= "|";


	/**
	 * Very dangerous! Should be used ONLY for batch processing to reset the state of the previous
	 * sequence!
	 */
	public static void resetIDs() {
		persistentNodeIDs.clear();
		identifiedNodes.clear();
		nextFreePersistentID= 1;
	}

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

	public static String getCommonPositonalNodeID(ASTNode node1, ASTNode node2) {
		String commonCoveringNodeID= "";
		StringTokenizer nodeLocations1= new StringTokenizer(ASTNodesIdentifier.getPositionalNodeID(node1), LOCATIONS_DELIMITER);
		StringTokenizer nodeLocations2= new StringTokenizer(ASTNodesIdentifier.getPositionalNodeID(node2), LOCATIONS_DELIMITER);
		while (nodeLocations1.hasMoreTokens() && nodeLocations2.hasMoreTokens()) {
			String location1= nodeLocations1.nextToken();
			String location2= nodeLocations2.nextToken();
			if (location1.equals(location2)) {
				commonCoveringNodeID+= location1 + LOCATIONS_DELIMITER;
			} else {
				break;
			}
		}
		return commonCoveringNodeID;
	}

	/**
	 * Returns null if the node with the given positional ID does not exist under the given root
	 * node.
	 * 
	 * @param rootNode
	 * @param positionalNodeID
	 * @return
	 */
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
				return null;
			}
			Object structuralProperty= currentNode.getStructuralProperty(ASTHelper.getLocationDescriptor(currentNode, currentNodeLocation.getLocationID()));
			if (structuralProperty == null) {
				return null;
			}
			if (currentNodeLocation.getChildIndex() != -1) {
				if (!(structuralProperty instanceof List) || ((List)structuralProperty).size() <= currentNodeLocation.getChildIndex()) {
					return null;
				}
				List children= (List)structuralProperty;
				currentNode= (ASTNode)children.get(currentNodeLocation.getChildIndex());
			} else {
				currentNode= (ASTNode)structuralProperty;
			}
		}
		return currentNode;
	}

	public static long getPersistentNodeID(String filePath, ASTNode node) {
		return getPersistentNodeID(getFilePersistentNodeIDs(filePath), node);
	}

	private static long getPersistentNodeID(Map<String, Long> filePersistentNodeIDs, ASTNode node) {
		String positionalNodeID= getPositionalNodeID(node);
		Long persistentNodeID= filePersistentNodeIDs.get(positionalNodeID);
		if (persistentNodeID == null) {
			persistentNodeID= nextFreePersistentID;
			filePersistentNodeIDs.put(positionalNodeID, nextFreePersistentID);
			identifiedNodes.put(persistentNodeID, node);
			nextFreePersistentID++;
		}
		return persistentNodeID;
	}

	public static void removePersistentNodeID(String filePath, ASTNode node) {
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(filePath);
		identifiedNodes.remove(getPersistentNodeID(filePersistentNodeIDs, node));
		filePersistentNodeIDs.remove(getPositionalNodeID(node));
	}

	private static Map<String, Long> getFilePersistentNodeIDs(String filePath) {
		Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.get(filePath);
		if (filePersistentNodeIDs == null) {
			filePersistentNodeIDs= new HashMap<String, Long>();
			persistentNodeIDs.put(filePath, filePersistentNodeIDs);
		}
		return filePersistentNodeIDs;
	}

	public static void updatePersistentNodeIDs(String filePath, Map<ASTNode, ASTNode> matchedNodes) {
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(filePath);
		//Collect new mappings in a separate map and update the main map only in the end to avoid paths collisions.
		Map<String, Long> newPersistentNodeIDs= new HashMap<String, Long>();
		for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			long persistentNodeID= getPersistentNodeID(filePersistentNodeIDs, oldNode);
			//Do NOT call removePersistentNodeID since we do not want to modify identifiedNodes.
			filePersistentNodeIDs.remove(getPositionalNodeID(oldNode));
			newPersistentNodeIDs.put(getPositionalNodeID(newNode), persistentNodeID);
		}
		filePersistentNodeIDs.putAll(newPersistentNodeIDs);
	}

	public static void updateFilePersistentNodeIDsMapping(String oldPrefix, String newPrefix) {
		for (String filePath : getFilePathsPrefixedBy(oldPrefix)) {
			String newFilePath= filePath.replaceFirst(oldPrefix, newPrefix);
			Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.remove(filePath);
			persistentNodeIDs.put(newFilePath, filePersistentNodeIDs);
		}
	}

	public static Map<String, Set<ASTNode>> getASTNodesFromAllDeletedFiles(String deletedResourcePath) {
		Map<String, Set<ASTNode>> collectedASTNodes= new HashMap<String, Set<ASTNode>>();
		for (String filePath : getFilePathsPrefixedBy(deletedResourcePath)) {
			Set<ASTNode> collectedFileASTNodes= new HashSet<ASTNode>();
			collectedASTNodes.put(filePath, collectedFileASTNodes);
			Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.get(filePath);
			for (long astNodeID : filePersistentNodeIDs.values()) {
				collectedFileASTNodes.add(identifiedNodes.get(astNodeID));
			}
		}
		return collectedASTNodes;
	}

	private static Set<String> getFilePathsPrefixedBy(String prefix) {
		return ResourceHelper.getFilePathsPrefixedBy(prefix, persistentNodeIDs.keySet());
	}

}
