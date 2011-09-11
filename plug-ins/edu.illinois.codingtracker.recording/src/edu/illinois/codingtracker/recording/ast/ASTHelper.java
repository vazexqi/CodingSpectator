/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("rawtypes")
public class ASTHelper {

	private static final Map<String, Map<String, Long>> persistentNodeIDs= new HashMap<String, Map<String, Long>>();

	private static final Map<Long, ASTNode> identifiedNodes= new HashMap<Long, ASTNode>();

	private static long nextFreePersistentID= 1;

	private static final String ROOT_NODE_LOCATION_ID= "root";

	private static final String QUALIFICATION_DELIMITER= ".";

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

	public static String getCommonPositonalNodeID(ASTNode node1, ASTNode node2) {
		String commonCoveringNodeID= "";
		StringTokenizer nodeLocations1= new StringTokenizer(ASTHelper.getPositionalNodeID(node1), LOCATIONS_DELIMITER);
		StringTokenizer nodeLocations2= new StringTokenizer(ASTHelper.getPositionalNodeID(node2), LOCATIONS_DELIMITER);
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
			Object structuralProperty= currentNode.getStructuralProperty(getLocationDescriptor(currentNode, currentNodeLocation.getLocationID()));
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

	public static StructuralPropertyDescriptor getLocationDescriptor(ASTNode node, String locationID) {
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

	public static Set<SimplePropertyDescriptor> getSimplePropertyDescriptors(ASTNode node) {
		Set<SimplePropertyDescriptor> simplePropertyDescriptors= new HashSet<SimplePropertyDescriptor>();
		for (Object structuralPropertyDescriptor : node.structuralPropertiesForType()) {
			if (structuralPropertyDescriptor instanceof SimplePropertyDescriptor) {
				simplePropertyDescriptors.add((SimplePropertyDescriptor)structuralPropertyDescriptor);
			}
		}
		return simplePropertyDescriptors;
	}

	public static MethodDeclaration getContainingMethod(ASTNode node) {
		ASTNode parentNode= getParent(node, MethodDeclaration.class);
		if (parentNode != null) {
			return (MethodDeclaration)parentNode;
		}
		return null;
	}

	public static TypeDeclaration getContainingType(ASTNode node) {
		ASTNode parentNode= getParent(node, TypeDeclaration.class);
		if (parentNode != null) {
			return (TypeDeclaration)parentNode;
		}
		return null;
	}

	public static CompilationUnit getContainingCompilationUnit(ASTNode node) {
		ASTNode parentNode= getParent(node, CompilationUnit.class);
		if (parentNode != null) {
			return (CompilationUnit)parentNode;
		}
		return null;
	}

	private static ASTNode getParent(ASTNode node, Class parentClass) {
		while (node != null) {
			if (node.getClass().equals(parentClass)) {
				return node;
			}
			node= node.getParent();
		}
		return null;
	}

	public static String getQualifiedMethodName(MethodDeclaration methodDeclaration) {
		//TODO: Consider adding method signature after it is recorded for test execution.
		String methodName= methodDeclaration.getName().toString();
		TypeDeclaration typeDeclaration= getContainingType(methodDeclaration);
		if (typeDeclaration != null) {
			methodName= prependQualifier(typeDeclaration.getName().toString(), methodName);
			CompilationUnit compilationUnit= getContainingCompilationUnit(typeDeclaration);
			if (compilationUnit != null) {
				Object structuralProperty= compilationUnit.getStructuralProperty(CompilationUnit.PACKAGE_PROPERTY);
				if (structuralProperty != null) {
					PackageDeclaration packageDeclaration= (PackageDeclaration)structuralProperty;
					methodName= prependQualifier(packageDeclaration.getName().toString(), methodName);
				}
			}
		}
		return methodName;
	}

	private static String prependQualifier(String qualifier, String baseName) {
		return qualifier + QUALIFICATION_DELIMITER + baseName;
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

	public static long getPersistentNodeID(String fileID, ASTNode node) {
		return getPersistentNodeID(getFilePersistentNodeIDs(fileID), node);
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

	public static void removePersistentNodeID(String fileID, ASTNode node) {
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(fileID);
		identifiedNodes.remove(getPersistentNodeID(filePersistentNodeIDs, node));
		filePersistentNodeIDs.remove(getPositionalNodeID(node));
	}

	private static Map<String, Long> getFilePersistentNodeIDs(String fileID) {
		Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.get(fileID);
		if (filePersistentNodeIDs == null) {
			filePersistentNodeIDs= new HashMap<String, Long>();
			persistentNodeIDs.put(fileID, filePersistentNodeIDs);
		}
		return filePersistentNodeIDs;
	}

	public static void updatePersistentNodeIDs(String fileID, Map<ASTNode, ASTNode> updatePositionalNodeIDsMap) {
		Map<String, Long> filePersistentNodeIDs= getFilePersistentNodeIDs(fileID);
		//Collect new mappings in a separate map and update the main map only in the end to avoid paths collisions.
		Map<String, Long> newPersistentNodeIDs= new HashMap<String, Long>();
		for (Entry<ASTNode, ASTNode> mapEntry : updatePositionalNodeIDsMap.entrySet()) {
			long persistentNodeID= getPersistentNodeID(filePersistentNodeIDs, mapEntry.getKey());
			//Do NOT call removePersistentNodeID since we do not want to modify identifiedNodes.
			filePersistentNodeIDs.remove(getPositionalNodeID(mapEntry.getKey()));
			newPersistentNodeIDs.put(getPositionalNodeID(mapEntry.getValue()), persistentNodeID);
		}
		filePersistentNodeIDs.putAll(newPersistentNodeIDs);
	}

	public static void updateFilePersistentNodeIDsMapping(String oldPrefix, String newPrefix) {
		for (String fileID : getFileIDsPrefixedBy(oldPrefix)) {
			String newFileID= fileID.replaceFirst(oldPrefix, newPrefix);
			Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.remove(fileID);
			persistentNodeIDs.put(newFileID, filePersistentNodeIDs);
		}
	}

	public static Map<String, Set<ASTNode>> getASTNodesFromAllDeletedFiles(String deletedResourceID) {
		Map<String, Set<ASTNode>> collectedASTNodes= new HashMap<String, Set<ASTNode>>();
		for (String fileID : getFileIDsPrefixedBy(deletedResourceID)) {
			Set<ASTNode> collectedFileASTNodes= new HashSet<ASTNode>();
			collectedASTNodes.put(fileID, collectedFileASTNodes);
			Map<String, Long> filePersistentNodeIDs= persistentNodeIDs.get(fileID);
			for (long astNodeID : filePersistentNodeIDs.values()) {
				collectedFileASTNodes.add(identifiedNodes.get(astNodeID));
			}
		}
		return collectedASTNodes;
	}

	private static Set<String> getFileIDsPrefixedBy(String prefix) {
		Set<String> fileIDs= new HashSet<String>();
		for (String fileID : persistentNodeIDs.keySet()) {
			if (fileID.equals(prefix) || fileID.startsWith(prefix + IPath.SEPARATOR)) {
				fileIDs.add(fileID);
			}
		}
		return fileIDs;
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
