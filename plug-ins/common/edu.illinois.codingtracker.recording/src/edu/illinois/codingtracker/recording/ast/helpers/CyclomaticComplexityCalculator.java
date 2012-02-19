/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.helpers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;


/**
 * 
 * @author Stas Negara
 * 
 */
public class CyclomaticComplexityCalculator {

	private static final Map<MethodDeclaration, Integer> cyclomaticComplexityCache= new HashMap<MethodDeclaration, Integer>();


	public static void resetCache() {
		cyclomaticComplexityCache.clear();
	}

	public static int getCyclomaticComplexity(MethodDeclaration methodDeclaration) {
		Integer cyclomaticComplexity= cyclomaticComplexityCache.get(methodDeclaration);
		if (cyclomaticComplexity == null) {
			cyclomaticComplexity= computeCyclomaticComplexity(methodDeclaration);
			cyclomaticComplexityCache.put(methodDeclaration, cyclomaticComplexity);
		}
		return cyclomaticComplexity;
	}

	private static int computeCyclomaticComplexity(MethodDeclaration methodDeclaration) {
		int cyclomaticComplexity= 1;
		for (ASTNode childNode : ASTHelper.getAllChildren(methodDeclaration)) {
			cyclomaticComplexity+= getCyclomaticComplexityForNode(childNode);
		}
		return cyclomaticComplexity;
	}

	private static int getCyclomaticComplexityForNode(ASTNode node) {
		if (node instanceof SwitchStatement || node instanceof CatchClause || hasCondition(node)) {
			return 1;
		}
		if (isLogicalOperatorInsideCondition(node)) {
			return 1;
		}
		if (node instanceof SwitchCase && !((SwitchCase)node).isDefault()) {
			//Count each SwitchStatement and each SwitchCase except the default one, which is equivalent to counting 
			//each SwitchCase including the default one and adding 1 for each SwitchStatement without the default case.
			return 1;
		}
		return 0;
	}

	private static boolean isLogicalOperatorInsideCondition(ASTNode node) {
		if (node instanceof InfixExpression) {
			Operator infixOperator= ((InfixExpression)node).getOperator();
			if (infixOperator == Operator.XOR || infixOperator == Operator.AND || infixOperator == Operator.OR ||
					infixOperator == Operator.CONDITIONAL_AND || infixOperator == Operator.CONDITIONAL_OR) {
				while (node.getParent() != null) {
					ASTNode childNode= node;
					node= node.getParent();
					if (hasCondition(node)) {
						//The location ID of all nodes with a condition is "expression".
						return childNode == node.getStructuralProperty(ASTHelper.getLocationDescriptor(node, "expression"));
					}
				}
			}
		}
		return false;
	}

	private static boolean hasCondition(ASTNode node) {
		return node instanceof IfStatement || node instanceof ConditionalExpression || node instanceof ForStatement ||
				node instanceof WhileStatement;
	}

}
