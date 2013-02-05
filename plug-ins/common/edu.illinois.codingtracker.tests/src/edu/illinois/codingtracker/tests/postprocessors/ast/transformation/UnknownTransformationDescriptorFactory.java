/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor.OperationKind;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;



/**
 * This class constructs UnknownTransformationDescriptors.
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationDescriptorFactory {

	private static int count;

	private static final Map<String, String> nameMapping= new HashMap<String, String>();


	public static UnknownTransformationDescriptor createDescriptor(OperationKind operationKind, ASTNode affectedNode) {
		return new UnknownTransformationDescriptor(operationKind, getNodeType(affectedNode), affectedNode.toString(),
													getAbstractedNodeContent(affectedNode));
	}

	public static UnknownTransformationDescriptor createDescriptor(OperationKind operationKind, String affectedNodeType) {
		return new UnknownTransformationDescriptor(operationKind, affectedNodeType, "", "");
	}

	private static String getAbstractedNodeContent(ASTNode node) {
		resetMappings();
		ASTNode copyNode= ASTNode.copySubtree(AST.newAST(AST.JLS3), node);
		copyNode.accept(new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				node.setIdentifier(getAbstractName(node.getIdentifier()));
				return true;
			}

			@Override
			public boolean visit(StringLiteral node) {
				node.setLiteralValue("string");
				return true;
			}

			@Override
			public boolean visit(CharacterLiteral node) {
				node.setCharValue('c');
				return true;
			}

			@Override
			public boolean visit(NumberLiteral node) {
				node.setToken("0");
				return true;
			}

		});
		return copyNode.toString();
	}

	private static void resetMappings() {
		count= 1;
		nameMapping.clear();
	}

	private static String getAbstractName(String concreteName) {
		String abstractName= nameMapping.get(concreteName);
		if (abstractName == null) {
			abstractName= "id" + count++;
			nameMapping.put(concreteName, abstractName);
		}
		return abstractName;
	}

	private static String getNodeType(ASTNode node) {
		if (node == null) {
			return "";
		}
		return node.getClass().getSimpleName();
	}

}
