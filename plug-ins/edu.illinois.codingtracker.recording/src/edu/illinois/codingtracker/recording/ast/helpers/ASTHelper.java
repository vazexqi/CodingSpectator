/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.helpers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
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

	public static Set<ASTNode> getAllChildren(ASTNode node) {
		final Set<ASTNode> childrenNodes= new HashSet<ASTNode>();
		node.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode visitedNode) {
				childrenNodes.add(visitedNode);
			}
		});
		return childrenNodes;
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
		//Avoid resolving bindings to speed up the parsing.
		parser.setResolveBindings(false);
		parser.setBindingsRecovery(false);
		return parser;
	}

}
