/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.identification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("rawtypes")
public class ASTNodesIdentifier {

	private static final Map<String, Map<String, Long>> persistentNodeIDs= new HashMap<String, Map<String, Long>>();

	private static final Map<Long, IdentifiedNodeInfo> identifiedNodes= new HashMap<Long, IdentifiedNodeInfo>();

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
		return getPersistentNodeID(filePath, node, true);
	}

	private static long getPersistentNodeID(String filePath, ASTNode node, boolean shouldAddToIdentifiedNodes) {
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(filePath);
		String positionalNodeID= getPositionalNodeID(node);
		Long persistentNodeID= filePersistentNodeIDs.get(positionalNodeID);
		if (persistentNodeID == null) {
			persistentNodeID= nextFreePersistentID;
			filePersistentNodeIDs.put(positionalNodeID, nextFreePersistentID);
			nextFreePersistentID++;
		}
		if (shouldAddToIdentifiedNodes && !identifiedNodes.containsKey(persistentNodeID)) {
			identifiedNodes.put(persistentNodeID, new IdentifiedNodeInfo(filePath, node, persistentNodeID));
		}
		return persistentNodeID;
	}

	public static long removePersistentNodeID(String filePath, ASTNode node) {
		long persistentNodeID= getPersistentNodeID(filePath, node, false);
		String positionalNodeID= getPositionalNodeID(node);
		return removePersistentNodeID(filePath, persistentNodeID, positionalNodeID);
	}

	public static long removePersistentNodeID(String filePath, long persistentNodeID, String positionalNodeID) {
		identifiedNodes.remove(persistentNodeID);
		//Remove the persistent node ID entry after getting the persistentNodeID.
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(filePath);
		filePersistentNodeIDs.remove(positionalNodeID);
		return persistentNodeID;
	}

	private static Map<String, Long> getFilePersistentNodeIDs(String filePath) {
		Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.get(filePath);
		if (filePersistentNodeIDs == null) {
			filePersistentNodeIDs= new HashMap<String, Long>();
			persistentNodeIDs.put(filePath, filePersistentNodeIDs);
		}
		return filePersistentNodeIDs;
	}

	public static void updatePersistentNodeIDs(String filePath, Map<ASTNode, ASTNode> matchedNodes, ASTNode newCommonCoveringNode) {
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(filePath);
		//To avoid structural IDs collisions, first collect new mappings in a separate map, then update the main map, 
		//and finally, use the updated main map (indirectly) to update the identified nodes. 
		Map<String, Long> newPersistentNodeIDs= new HashMap<String, Long>();
		for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			long persistentNodeID= removePersistentNodeID(filePath, oldNode);
			newPersistentNodeIDs.put(getPositionalNodeID(newNode), persistentNodeID);
		}
		filePersistentNodeIDs.putAll(newPersistentNodeIDs);
		updateIdentifiedNodes(filePath, matchedNodes, newCommonCoveringNode);
	}

	private static void updateIdentifiedNodes(String filePath, Map<ASTNode, ASTNode> matchedNodes, ASTNode newCommonCoveringNode) {
		for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
			ASTNode newNode= mapEntry.getValue();
			long newNodeID= getPersistentNodeID(filePath, newNode, false);
			identifiedNodes.put(newNodeID, new IdentifiedNodeInfo(filePath, newNode, newNodeID));
		}
		MethodDeclaration containingMethod= ASTHelper.getContainingMethod(newCommonCoveringNode);
		if (containingMethod != null) {
			long containingMethodID= getPersistentNodeID(filePath, containingMethod);
			identifiedNodes.put(containingMethodID, new IdentifiedNodeInfo(filePath, containingMethod, containingMethodID));
		}
	}

	public static void updateFilePersistentNodeIDsMapping(String oldPrefix, String newPrefix) {
		for (String filePath : getFilePathsPrefixedBy(oldPrefix)) {
			String newFilePath= StringHelper.replacePrefix(filePath, oldPrefix, newPrefix);
			Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.remove(filePath);
			persistentNodeIDs.put(newFilePath, filePersistentNodeIDs);
		}
	}

	public static Map<String, Set<IdentifiedNodeInfo>> getNodeInfosFromAllDeletedFiles(String deletedResourcePath) {
		Map<String, Set<IdentifiedNodeInfo>> collectedNodeInfos= new HashMap<String, Set<IdentifiedNodeInfo>>();
		for (String filePath : getFilePathsPrefixedBy(deletedResourcePath)) {
			Set<IdentifiedNodeInfo> collectedFileNodeInfos= new HashSet<IdentifiedNodeInfo>();
			collectedNodeInfos.put(filePath, collectedFileNodeInfos);
			Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.get(filePath);
			for (long astNodeID : filePersistentNodeIDs.values()) {
				collectedFileNodeInfos.add(identifiedNodes.get(astNodeID));
			}
		}
		return collectedNodeInfos;
	}

	public static IdentifiedNodeInfo getIdentifiedNodeInfo(long persistentNodeID) {
		//It is assumed that if the caller obtained persistentNodeID, the node should be already among identified nodes.
		return identifiedNodes.get(persistentNodeID);
	}

	private static Set<String> getFilePathsPrefixedBy(String prefix) {
		return ResourceHelper.getFilePathsPrefixedBy(prefix, persistentNodeIDs.keySet());
	}

}
