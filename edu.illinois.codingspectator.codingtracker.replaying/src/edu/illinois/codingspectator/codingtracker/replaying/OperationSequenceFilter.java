/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.replaying;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import edu.illinois.codingspectator.codingtracker.operations.files.RefactoredSavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.SnapshotedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.RefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.StartedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.TextChangeOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationSequenceFilter extends ViewerFilter {

	public enum FilteredOperations {
		TEXT_CHANGES, REFACTORINGS, SNAPSHOTS, OTHERS
	}

	private boolean showTextChanges= true;

	private boolean showRefactorings= true;

	private boolean showSnapshots= true;

	private boolean showOthers= true;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof TextChangeOperation) {
			return showTextChanges;
		}
		if (element instanceof RefactoringOperation || element instanceof RefactoredSavedFileOperation
				|| element instanceof StartedRefactoringOperation) {
			return showRefactorings;
		}
		if (element instanceof SnapshotedFileOperation) {
			return showSnapshots;
		}
		return showOthers;
	}

	public void toggleFilteredOperations(FilteredOperations filteredOperations) {
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
			case OTHERS:
				showOthers= !showOthers;
				break;
			default:
				throw new RuntimeException("Unsupported filtered operations: " + filteredOperations);
		}
	}

}
