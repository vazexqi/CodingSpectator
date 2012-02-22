/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.replaying;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import edu.illinois.codingtracker.helpers.ViewerHelper;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.files.RefactoredSavedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.RefactoringOperation;
import edu.illinois.codingtracker.operations.starts.StartedRefactoringOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationSequenceFilter extends ViewerFilter {

	private enum FilteredOperations {
		TEXT_CHANGES, REFACTORINGS, SNAPSHOTS, AST_OPERATIONS, INFERRED_REFACTORINGS, OTHERS
	}

	private final OperationSequenceView operationSequenceView;

	private boolean showTextChanges= true;

	private boolean showRefactorings= true;

	private boolean showSnapshots= true;

	private boolean showASTOperations= true;

	private boolean showInferredRefactorings= true;

	private boolean showOthers= true;

	public OperationSequenceFilter(OperationSequenceView operationSequenceView) {
		this.operationSequenceView= operationSequenceView;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return isShown(element);
	}

	boolean isShown(Object element) {
		if (element instanceof TextChangeOperation) {
			return showTextChanges;
		}
		if (element instanceof RefactoringOperation || element instanceof RefactoredSavedFileOperation || element instanceof StartedRefactoringOperation
				|| element instanceof NewStartedRefactoringOperation || element instanceof FinishedRefactoringOperation) {
			return showRefactorings;
		}
		if (element instanceof SnapshotedFileOperation) {
			return showSnapshots;
		}
		if (element instanceof ASTOperation || element instanceof ASTFileOperation) {
			return showASTOperations;
		}
		if (element instanceof InferredRefactoringOperation) {
			return showInferredRefactorings;
		}
		return showOthers;
	}

	void addToolBarActions() {
		IToolBarManager toolBarManager= operationSequenceView.getToolBarManager();
		toolBarManager.add(createFilterAction("Text Changes", "Display text change operations", FilteredOperations.TEXT_CHANGES));
		toolBarManager.add(createFilterAction("Refactorings", "Display refactoring operations", FilteredOperations.REFACTORINGS));
		toolBarManager.add(createFilterAction("Snapshots", "Display snapshot-producing operations", FilteredOperations.SNAPSHOTS));
		toolBarManager.add(createFilterAction("AST Operations", "Display AST node operations", FilteredOperations.AST_OPERATIONS));
		toolBarManager.add(createFilterAction("Inferred Refactorings", "Display inferred refactoring operations", FilteredOperations.INFERRED_REFACTORINGS));
		toolBarManager.add(createFilterAction("Others", "Display all other operations", FilteredOperations.OTHERS));
	}

	private IAction createFilterAction(String actionText, String actionToolTipText, final FilteredOperations filteredOperations) {
		IAction action= new Action() {
			@Override
			public void run() {
				toggleFilteredOperations(filteredOperations);
				operationSequenceView.refreshTableViewer();
			}
		};
		ViewerHelper.initAction(action, actionText, actionToolTipText, true, true, true);
		return action;
	}

	private void toggleFilteredOperations(FilteredOperations filteredOperations) {
		switch (filteredOperations) {
			case TEXT_CHANGES:
				showTextChanges= !showTextChanges;
				break;
			case REFACTORINGS:
				showRefactorings= !showRefactorings;
				break;
			case SNAPSHOTS:
				showSnapshots= !showSnapshots;
				break;
			case AST_OPERATIONS:
				showASTOperations= !showASTOperations;
				break;
			case INFERRED_REFACTORINGS:
				showInferredRefactorings= !showInferredRefactorings;
				break;
			case OTHERS:
				showOthers= !showOthers;
				break;
			default:
				throw new RuntimeException("Unsupported filtered operations: " + filteredOperations);
		}
	}

}
