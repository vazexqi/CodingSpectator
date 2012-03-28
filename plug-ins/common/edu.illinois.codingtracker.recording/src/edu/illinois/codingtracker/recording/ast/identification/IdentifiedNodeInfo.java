/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.identification;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.ast.ASTMethodDescriptor;
import edu.illinois.codingtracker.operations.ast.ASTNodeDescriptor;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class IdentifiedNodeInfo {

	private final String positionalNodeID;

	private long containingMethodID= -1;

	//Note that nodeDescriptor.nodeOffset could have an old value if there are changes in methods that precede in code 
	//the containing method of this AST node. This is OK since nodeOffset is used only for debugging purposes.
	private final ASTNodeDescriptor nodeDescriptor;

	private final ASTMethodDescriptor methodDescriptor; //It is null if this identified node is not a MethodDeclaration.

	//TODO: In the current implementation it is OK that this set is empty since this set is used only in analyzers that
	//consider commit operations, while IdentifiedNodeInfos are used only when the whole file is deleted, and the current 
	//data does not track commits of the deleted resources.
	private final Set<Long> clusterNodeIDs= new HashSet<Long>();


	public IdentifiedNodeInfo(String filePath, ASTNode identifiedNode, long persistentNodeID) {
		positionalNodeID= ASTNodesIdentifier.getPositionalNodeID(identifiedNode);
		nodeDescriptor= ASTHelper.createASTNodeDescriptor(persistentNodeID, identifiedNode, "");
		if (identifiedNode instanceof MethodDeclaration) {
			containingMethodID= persistentNodeID; //A method contains itself.
			//Note that instances of IdentifiedNodeInfo are created only during recording of AST operations, and the cache of 
			//the cyclomatic complexity calculator is always reset before this recording, so no need to reset it here again.
			methodDescriptor= ASTHelper.createASTMethodDescriptor(persistentNodeID, (MethodDeclaration)identifiedNode);
		} else {
			methodDescriptor= null;
			MethodDeclaration containingMethod= ASTHelper.getContainingMethod(identifiedNode);
			if (containingMethod != null) {
				containingMethodID= ASTNodesIdentifier.getPersistentNodeID(filePath, containingMethod);
			}
		}
	}

	public long getNodeID() {
		return nodeDescriptor.getNodeID();
	}

	public String getPositionalNodeID() {
		return positionalNodeID;
	}

	public ASTNodeDescriptor getASTNodeDescriptor() {
		return nodeDescriptor;
	}

	public ASTMethodDescriptor getMethodDescriptor() {
		return methodDescriptor;
	}

	public ASTMethodDescriptor getContainingMethodDescriptor() {
		if (containingMethodID != -1) {
			IdentifiedNodeInfo containingMethodNodeInfo= ASTNodesIdentifier.getIdentifiedNodeInfo(containingMethodID);
			//TODO: An orphan node could appear as a result of parsing problems. For example, recovered nodes are matched,
			//IDs are assigned, then changes to other parts of the code affect the way the parser recovers the same nodes.
			//As a result, in the following matching the same recovered nodes could have different positional IDs and would
			//get new persistent IDs. Consequently, the previous persistent IDs start to identify orphan nodes.
			//As a temporary solution, orphan nodes are ignored at this stage. Later, the algorithm might handle this scenario.
			//See the data from cs-111 for an example of a sequence containing such scenario.
			if (containingMethodNodeInfo == null) {
				return null;
			}
			ASTMethodDescriptor containingMethodDecriptor= containingMethodNodeInfo.getMethodDescriptor();
			//TODO: Although this is not a problem during refactoring inference, since the resulting descriptors would not be 
			//recorded anyway, investigate the cause of this scenario, which does not happen during regular AST inference.
			if (containingMethodDecriptor == null && !Configuration.isInRefactoringInferenceMode) {
				throw new RuntimeException("Containing method's node info does not represent a method declaration!");
			}
			return containingMethodDecriptor;
		}
		return ASTHelper.createEmptyASTMethodDescriptor();
	}

	public Set<Long> getClusterNodeIDs() {
		return clusterNodeIDs;
	}

}
