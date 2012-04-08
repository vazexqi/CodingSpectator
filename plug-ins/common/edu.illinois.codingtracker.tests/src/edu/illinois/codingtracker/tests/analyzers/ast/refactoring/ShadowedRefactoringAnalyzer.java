/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;


/**
 * This analyzer computes for each kind of the refactorings that we infer how many refactorings are
 * completely not reaching VCS (i.e., completely shadowed by other changes) as well as percentage of
 * refactoring changes that do not reach VCS (i.e., how much of a refactoring is shadowed).
 * 
 * @author Stas Negara
 * 
 */
public class ShadowedRefactoringAnalyzer extends InferredRefactoringAnalyzer {

	private String currentASTFilePath;

	private final Map<Long, RefactoringDescriptor> inferredRefactorings= new HashMap<Long, RefactoringDescriptor>();

	private final Set<RefactoringDescriptor> automatedRefactorings= new HashSet<RefactoringDescriptor>();

	private RefactoringDescriptor currentAutomatedRefactoringDescriptor;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,refactoring kind,total committed refactorings,completely shadowed refactorings,remaining committed changes,remaining shadowed changes\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof ASTFileOperation) {
			currentASTFilePath= ((ASTFileOperation)userOperation).getResourcePath();
		} else if (userOperation instanceof ASTOperation) {
			handleASTOperation((ASTOperation)userOperation);
		} else if (userOperation instanceof MovedResourceOperation) {
			handleMovedResourceOperation((MovedResourceOperation)userOperation);
		} else if (userOperation instanceof InferredRefactoringOperation) {
			handleInferredRefactoringOperation((InferredRefactoringOperation)userOperation);
		} else if (userOperation instanceof CommittedFileOperation) {
			handleCommittedFileOperation((CommittedFileOperation)userOperation);
		} else if (userOperation instanceof NewStartedRefactoringOperation) {
			handleStartedRefactoringOperation();
		} else if (userOperation instanceof FinishedRefactoringOperation) {
			handleFinishedRefactoringOperation();
		}
	}

	private void handleFinishedRefactoringOperation() {
		currentAutomatedRefactoringDescriptor= null;
	}

	private void handleStartedRefactoringOperation() {
		if (getCurrentAutomatedRefactoringKind() != null) {
			currentAutomatedRefactoringDescriptor= new RefactoringDescriptor();
			currentAutomatedRefactoringDescriptor.setRefactoringKind(getCurrentAutomatedRefactoringKind());
			automatedRefactorings.add(currentAutomatedRefactoringDescriptor);
		}
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		handleCommittedFilePath(committedFilePath, inferredRefactorings.values());
		handleCommittedFilePath(committedFilePath, automatedRefactorings);
	}

	private void handleCommittedFilePath(String committedFilePath, Collection<RefactoringDescriptor> descriptors) {
		for (RefactoringDescriptor refactoringDescriptor : descriptors) {
			refactoringDescriptor.handleCommitPath(committedFilePath);
		}
	}

	private void handleInferredRefactoringOperation(InferredRefactoringOperation inferredRefactoringOperation) {
		RefactoringDescriptor refactoringDescriptor= inferredRefactorings.get(inferredRefactoringOperation.getRefactoringID());
		if (refactoringDescriptor != null) {
			refactoringDescriptor.setRefactoringKind(inferredRefactoringOperation.getRefactoringKind());
		}
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		handleMovedResource(oldPrefix, newPrefix, inferredRefactorings.values());
		handleMovedResource(oldPrefix, newPrefix, automatedRefactorings);
	}

	private void handleMovedResource(String oldPrefix, String newPrefix, Collection<RefactoringDescriptor> descriptors) {
		for (RefactoringDescriptor refactoringDescriptor : descriptors) {
			refactoringDescriptor.updatePath(oldPrefix, newPrefix);
		}
	}

	private void handleASTOperation(ASTOperation astOperation) {
		long affectedNodeID= astOperation.getNodeID();
		shadowNode(affectedNodeID);
		if (currentAutomatedRefactoringDescriptor != null) {
			currentAutomatedRefactoringDescriptor.addRefactoredNode(affectedNodeID);
		} else {
			//If not inside an automated refactoring, look for manual refactorings.
			long refactoringID= astOperation.getRefactoringID();
			if (refactoringID != -1) {
				RefactoringDescriptor refactoringDescriptor= getInferredRefactoringDescriptor(refactoringID);
				refactoringDescriptor.addRefactoredNode(affectedNodeID);
			}
		}
	}

	private RefactoringDescriptor getInferredRefactoringDescriptor(long refactoringID) {
		RefactoringDescriptor refactoringDescriptor= inferredRefactorings.get(refactoringID);
		if (refactoringDescriptor == null) {
			refactoringDescriptor= new RefactoringDescriptor();
			inferredRefactorings.put(refactoringID, refactoringDescriptor);
		}
		return refactoringDescriptor;
	}

	private void shadowNode(long nodeID) {
		shadowNode(nodeID, inferredRefactorings.values());
		shadowNode(nodeID, automatedRefactorings);
	}

	private void shadowNode(long nodeID, Collection<RefactoringDescriptor> descriptors) {
		for (RefactoringDescriptor refactoringDescriptor : descriptors) {
			refactoringDescriptor.addShadowedNode(nodeID);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		currentASTFilePath= null;
		inferredRefactorings.clear();
		automatedRefactorings.clear();
		currentAutomatedRefactoringDescriptor= null;
	}

	@Override
	protected void populateResults() {
		Map<RefactoringKind, Pair> completelyShadowedRefactorigs= new HashMap<RefactoringKind, Pair>();
		Map<RefactoringKind, Pair> partiallyShadowedRefactorigs= new HashMap<RefactoringKind, Pair>();
		populateStatisticalData(inferredRefactorings.values(), completelyShadowedRefactorigs, partiallyShadowedRefactorigs);
		populateStatisticalData(automatedRefactorings, completelyShadowedRefactorigs, partiallyShadowedRefactorigs);
		for (Entry<RefactoringKind, Pair> pairRefactoringCounter : completelyShadowedRefactorigs.entrySet()) {
			//The key set of completely shadowed refactorings map contains all committed refactoring kinds.
			RefactoringKind refactoringKind= pairRefactoringCounter.getKey();
			Pair pairChangesCounter= partiallyShadowedRefactorigs.get(refactoringKind);
			if (pairChangesCounter == null) {
				//Just an empty pair for convenience.
				pairChangesCounter= new Pair();
			}
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, refactoringKind,
					pairRefactoringCounter.getValue().num1, pairRefactoringCounter.getValue().num2, pairChangesCounter.num1,
					pairChangesCounter.num2);

		}
	}

	private void populateStatisticalData(Collection<RefactoringDescriptor> descriptors, Map<RefactoringKind, Pair> completelyShadowedRefactorigs,
											Map<RefactoringKind, Pair> partiallyShadowedRefactorigs) {
		for (RefactoringDescriptor refactoringDescriptor : descriptors) {
			Pair pairRefactoringCounter= completelyShadowedRefactorigs.get(refactoringDescriptor.refactoringKind);
			if (pairRefactoringCounter == null) {
				pairRefactoringCounter= new Pair();
				completelyShadowedRefactorigs.put(refactoringDescriptor.refactoringKind, pairRefactoringCounter);
			}
			if (refactoringDescriptor.totalCommittedNodes != 0) {
				pairRefactoringCounter.num1++; //Total committed refactorings counter.
				if (refactoringDescriptor.isCompletelyShadowed()) {
					pairRefactoringCounter.num2++;
				} else {
					Pair pairChangesCounter= partiallyShadowedRefactorigs.get(refactoringDescriptor.refactoringKind);
					if (pairChangesCounter == null) {
						pairChangesCounter= new Pair();
						partiallyShadowedRefactorigs.put(refactoringDescriptor.refactoringKind, pairChangesCounter);
					}
					pairChangesCounter.num1+= refactoringDescriptor.totalCommittedNodes;
					pairChangesCounter.num2+= refactoringDescriptor.shadowedCommittedNodes;
				}
			}
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".shadowed_refactorings";
	}

	private class Pair {

		int num1;

		int num2;

	}

	private class RefactoringDescriptor {

		private RefactoringKind refactoringKind;

		private final Map<String, Set<Long>> refactoredNodes= new HashMap<String, Set<Long>>();

		private final Set<Long> shadowedNodes= new HashSet<Long>();

		private int totalCommittedNodes= 0;

		private int shadowedCommittedNodes= 0;


		boolean isCompletelyShadowed() {
			return totalCommittedNodes != 0 && totalCommittedNodes == shadowedCommittedNodes;
		}

		void addRefactoredNode(long nodeID) {
			Set<Long> refactoredNodesSet= refactoredNodes.get(currentASTFilePath);
			if (refactoredNodesSet == null) {
				refactoredNodesSet= new HashSet<Long>();
				refactoredNodes.put(currentASTFilePath, refactoredNodesSet);
			}
			if (this == currentAutomatedRefactoringDescriptor && refactoredNodesSet.contains(nodeID)) {
				//This is a spurious node operation produced inside an automated refactoring, discard.
				refactoredNodesSet.remove(nodeID);
				shadowedNodes.remove(nodeID);
			} else {
				refactoredNodesSet.add(nodeID);
			}
		}

		void handleCommitPath(String committedFilePath) {
			Set<Long> refactoredNodesSet= refactoredNodes.remove(committedFilePath);
			if (refactoredNodesSet != null) {
				totalCommittedNodes+= refactoredNodesSet.size();
				for (Long nodeID : refactoredNodesSet) {
					if (shadowedNodes.contains(nodeID)) {
						shadowedCommittedNodes++;
					}
				}
			}
		}

		public void setRefactoringKind(RefactoringKind refactoringKind) {
			this.refactoringKind= refactoringKind;
		}

		void updatePath(String oldPrefix, String newPrefix) {
			for (String oldFilePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, refactoredNodes.keySet())) {
				String newFilePath= StringHelper.replacePrefix(oldFilePath, oldPrefix, newPrefix);
				Set<Long> refactoredNodesSet= refactoredNodes.remove(oldFilePath);
				refactoredNodes.put(newFilePath, refactoredNodesSet);
			}
		}

		void addShadowedNode(long nodeID) {
			for (Set<Long> refactoredNodesSet : refactoredNodes.values()) {
				if (refactoredNodesSet.contains(nodeID)) {
					shadowedNodes.add(nodeID);
					return;
				}
			}
		}

	}

}
