/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.helpers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Document;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.ast.ASTMethodDescriptor;
import edu.illinois.codingtracker.operations.ast.ASTNodeDescriptor;
import edu.illinois.codingtracker.operations.ast.CompositeNodeDescriptor;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;
import edu.illinois.codingtracker.recording.ast.identification.IdentifiedNodeInfo;


/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("rawtypes")
public class ASTHelper {

	private static final String QUALIFICATION_DELIMITER= ".";


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

	/**
	 * Returns the first encountered parent of any of the given parentClasses (including the classes
	 * derived from parentClasses).
	 * 
	 * @param node
	 * @param parentClasses
	 * @return
	 */
	public static ASTNode getParent(ASTNode node, Class... parentClasses) {
		while (node != null) {
			for (Class parentClass : parentClasses) {
				if (parentClass.isInstance(node)) {
					return node;
				}
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

	public static boolean isProblematicAST(String text) {
		for (ASTNode node : getAllNodesFromText(text)) {
			if (isRecoveredOrMalformed(node)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRecoveredOrMalformed(ASTNode node) {
		return (node.getFlags() & ASTNode.RECOVERED) != 0 || (node.getFlags() & ASTNode.MALFORMED) != 0;
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

	public static Set<ASTNode> getAllNodesFromText(String text) {
		return getAllChildren(getRootNode(text));
	}

	/**
	 * Returns all children of the given node except those that are under nodes of the given
	 * ignoredClasses. Note that this method considers only the actual ignoredClasses and does not
	 * consider classes derived from them.
	 * 
	 * @param node
	 * @param ignoredClasses
	 * @return
	 */
	public static Set<ASTNode> getAllChildren(ASTNode node, Class... ignoredClasses) {
		final List<Class> ignoredClassesList= Arrays.asList(ignoredClasses);
		final Set<ASTNode> childrenNodes= new HashSet<ASTNode>();

		node.accept(new ASTVisitor() {
			@Override
			public boolean preVisit2(ASTNode visitedNode) {
				if (ignoredClassesList.contains(visitedNode.getClass())) {
					return false;
				}
				childrenNodes.add(visitedNode);
				return true;
			}
		});
		return childrenNodes;
	}

	public static boolean isChild(ASTNode checkedChild, ASTNode checkedParent) {
		while (checkedChild != null) {
			if (checkedChild == checkedParent) {
				return true;
			}
			checkedChild= checkedChild.getParent();
		}
		return false;
	}

	public static ASTNode getRootNode(String source) {
		ASTParser parser= createParser();
		parser.setSource(source.toCharArray());
		return parser.createAST(null);
	}

	//TODO: Should the parser be created once and then just reused?
	private static ASTParser createParser() {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setStatementsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		//To speed up the parsing, avoid resolving bindings unless it is necessary.
		boolean shouldResolveBindings= false;
		if (Configuration.isInRefactoringInferenceMode) {
			//TODO: Do we really use this?
			shouldResolveBindings= true;
		}
		parser.setResolveBindings(shouldResolveBindings);
		parser.setBindingsRecovery(shouldResolveBindings);
		return parser;
	}

	public static CompositeNodeDescriptor createCompositeNodeDescriptor(String filePath, ASTNode node, String nodeNewText) {
		ASTNodeDescriptor astNodeDescriptor= createASTNodeDescriptor(filePath, node, nodeNewText);
		ASTMethodDescriptor containingMethodDescriptor= createASTMethodDescriptor(filePath, getContainingMethod(node));
		Set<Long> clusterNodeIDs= collectClusterNodeIDs(filePath, node);
		return new CompositeNodeDescriptor(astNodeDescriptor, containingMethodDescriptor, clusterNodeIDs);
	}

	private static Set<Long> collectClusterNodeIDs(String filePath, ASTNode node) {
		Set<Long> clusterNodeIDs= new HashSet<Long>();
		for (ASTNode clusterNode : collectClusterNodes(node)) {
			clusterNodeIDs.add(ASTNodesIdentifier.getPersistentNodeID(filePath, clusterNode));
		}
		return clusterNodeIDs;
	}

	private static Set<ASTNode> collectClusterNodes(ASTNode node) {
		Set<ASTNode> clusterNodes= new HashSet<ASTNode>();
		clusterNodes.addAll(getAllChildren(node));
		if (node instanceof CompilationUnit) {
			//Nothing else could be added for a top level AST node.
			return clusterNodes;
		}
		ASTNode parentNode= getParent(node, Statement.class, BodyDeclaration.class, PackageDeclaration.class,
										ImportDeclaration.class);

		if (parentNode instanceof Statement) {
			//Do not consider other statements that might be in a statement's Block.
			clusterNodes.addAll(getAllChildren(parentNode, Block.class));
		} else {
			clusterNodes.addAll(getAllChildren(parentNode));
		}
		return clusterNodes;
	}

	private static ASTNodeDescriptor createASTNodeDescriptor(String filePath, ASTNode node, String nodeNewText) {
		long nodeID= ASTNodesIdentifier.getPersistentNodeID(filePath, node);
		return createASTNodeDescriptor(nodeID, node, nodeNewText);
	}

	public static ASTNodeDescriptor createASTNodeDescriptor(long nodeID, ASTNode node, String nodeNewText) {
		String nodeType= node.getClass().getSimpleName();
		String nodeText= node.toString();
		int nodeOffset= node.getStartPosition();
		int nodeLength= node.getLength();
		return new ASTNodeDescriptor(nodeID, ASTNodesIdentifier.getPositionalNodeID(node), nodeType, nodeText, nodeNewText, nodeOffset, nodeLength);
	}

	private static ASTMethodDescriptor createASTMethodDescriptor(String filePath, MethodDeclaration methodDeclaration) {
		if (methodDeclaration == null) {
			return createEmptyASTMethodDescriptor();
		}
		long methodID= ASTNodesIdentifier.getPersistentNodeID(filePath, methodDeclaration);
		return createASTMethodDescriptor(methodID, methodDeclaration);
	}

	public static ASTMethodDescriptor createEmptyASTMethodDescriptor() {
		return new ASTMethodDescriptor(-1, "", -1, -1);
	}

	public static ASTMethodDescriptor createASTMethodDescriptor(long methodID, MethodDeclaration methodDeclaration) {
		String methodName= getQualifiedMethodName(methodDeclaration);

		//Note that containingMethodLinesCount would not count lines with comments or white spaces, but would
		//count several statements on the same line as separate lines (i.e. AST node is normalized such that each statement
		//appears on a separate line, which is usually the case with the actual code as well).
		int methodLinesCount= (new Document(methodDeclaration.toString().trim())).getNumberOfLines();

		//Note that for added nodes we get lines count and cyclomatic complexity of the resulting containing method 
		//that already contains these added nodes.
		int methodCyclomaticComplexity= CyclomaticComplexityCalculator.getCyclomaticComplexity(methodDeclaration);

		return new ASTMethodDescriptor(methodID, methodName, methodLinesCount, methodCyclomaticComplexity);
	}

	public static CompositeNodeDescriptor createCompositeNodeDescriptor(IdentifiedNodeInfo nodeInfo) {
		ASTNodeDescriptor astNodeDescriptor= nodeInfo.getASTNodeDescriptor();
		ASTMethodDescriptor containingMethodDescriptor= nodeInfo.getContainingMethodDescriptor();
		if (containingMethodDescriptor == null) { //Could happen for orphan nodes.
			return null;
		}
		Set<Long> clusterNodeIDs= nodeInfo.getClusterNodeIDs();
		return new CompositeNodeDescriptor(astNodeDescriptor, containingMethodDescriptor, clusterNodeIDs);
	}

}
